package ru.nsu.problem_forge.service.problem

import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.PreviewStatus
import ru.nsu.problem_forge.dto.problem.ProblemPackageResponse
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.runner.Runner
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.TestType
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class ProblemPackageService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val fileRepository: FileRepository,
    private val problemTestsService: ProblemTestsService,
    private val runner: Runner
) {

    private val packageStatusCache = ConcurrentHashMap<Long, PreviewStatus>()
    private val packageGenerationCache = ConcurrentHashMap<Long, ByteArray>()
    private val problemChecksumCache = ConcurrentHashMap<Long, String>()

    @Transactional(readOnly = true)
    fun getProblemPackage(problemId: Long, userId: Long): ProblemPackageResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.VIEWER) {
            throw SecurityException("Viewer role required")
        }

        // Check if problem has a checker
        if (problem.problemInfo.checker == null) {
            return ProblemPackageResponse(
                status = PreviewStatus.ERROR,
                message = "Problem does not have a checker configured"
            )
        }

        // ... rest of the existing method
        val currentChecksum = problemTestsService.calculateProblemChecksum(problem)
        val cachedChecksum = problemChecksumCache[problemId]
        val hasChanges = cachedChecksum != currentChecksum

        // Check if generation is in progress
        val currentStatus = packageStatusCache[problemId]
        if (currentStatus == PreviewStatus.IN_PROGRESS) {
            return ProblemPackageResponse(
                status = PreviewStatus.IN_PROGRESS,
                message = "Problem package generation is in progress. Please wait..."
            )
        }

        // Check if we have valid cached package
        if (!hasChanges) {
            val cachedPackage = packageGenerationCache[problemId]
            if (cachedPackage != null) {
                return ProblemPackageResponse(
                    status = PreviewStatus.COMPLETED,
                    message = "Package is ready for download",
                    downloadUrl = "/api/problems/$problemId/package/download"
                )
            }
        }

        // Start async package generation
        startAsyncPackageGeneration(problemId, userId, currentChecksum)

        return ProblemPackageResponse(
            status = PreviewStatus.PENDING,
            message = if (hasChanges) {
                "Changes detected. Starting package generation..."
            } else {
                "Starting package generation..."
            }
        )
    }

    fun downloadProblemPackage(problemId: Long, userId: Long, response: HttpServletResponse) {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val packageData = packageGenerationCache[problemId]
            ?: throw IllegalStateException("Package not found or not generated")

        // Set response headers for file download
        response.contentType = "application/zip"
        response.setHeader("Content-Disposition", "attachment; filename=\"problem_${problemId}_package.zip\"")
        response.setContentLength(packageData.size)

        // Write ZIP data to response
        response.outputStream.use { outputStream ->
            outputStream.write(packageData)
            outputStream.flush()
        }
    }

    private fun startAsyncPackageGeneration(problemId: Long, userId: Long, currentChecksum: String) {
        packageStatusCache[problemId] = PreviewStatus.IN_PROGRESS

        Thread {
            try {
                val packageData = generatePackageSync(problemId, userId)

                // Update caches
                packageGenerationCache[problemId] = packageData
                problemChecksumCache[problemId] = currentChecksum
                packageStatusCache[problemId] = PreviewStatus.COMPLETED

            } catch (e: Exception) {
                packageStatusCache[problemId] = PreviewStatus.ERROR
                println("Error in async package generation for problem $problemId: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }

    @Transactional
    fun generatePackageSync(problemId: Long, userId: Long): ByteArray {
        val previewResponse = problemTestsService.getTestsPreview(problemId, userId)

        if (previewResponse.status != PreviewStatus.COMPLETED) {
            throw IllegalStateException("Failed to generate tests preview: ${previewResponse.message}")
        }

        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val checker = problem.problemInfo.checker
        if (checker == null) {
            throw IllegalStateException("Problem has no checker, can't generate package")
        }

        // Compile checker if exists
        val checkerBinary = problem.problemInfo.checker?.let { checkerFileId ->
            compileChecker(checkerFileId)
        }

        // Create ZIP package
        val byteArrayOutputStream = ByteArrayOutputStream()
        ZipOutputStream(byteArrayOutputStream).use { zipOutputStream ->
            problem.problemInfo.tests.forEachIndexed { index, test ->
                val testNumber = index + 1

                // Get input content
                val inputContent = when {
                    test.inputFileId != null -> {
                        String(fileRepository.findById(test.inputFileId).get().content)
                    }
                    test.testType == TestType.RAW -> test.content
                    else -> throw IllegalStateException("No input content for test $testNumber")
                }

                // Get output content
                val outputContent = if (test.outputFileId != null) {
                    String(fileRepository.findById(test.outputFileId).get().content)
                } else {
                    throw IllegalStateException("No output content for test $testNumber")
                }

                // Add input file to ZIP
                zipOutputStream.putNextEntry(ZipEntry("${testNumber}.in"))
                zipOutputStream.write(inputContent.toByteArray())
                zipOutputStream.closeEntry()

                // Add output file to ZIP
                zipOutputStream.putNextEntry(ZipEntry("${testNumber}.out"))
                zipOutputStream.write(outputContent.toByteArray())
                zipOutputStream.closeEntry()
            }

            // Add compiled checker to ZIP
            checkerBinary?.let { binary ->
                zipOutputStream.putNextEntry(ZipEntry("check.exe"))
                zipOutputStream.write(binary)
                zipOutputStream.closeEntry()
            }
        }

        return byteArrayOutputStream.toByteArray()
    }

    private fun compileChecker(checkerFileId: Long): ByteArray? {
        return try {
            val checkerFile = fileRepository.findById(checkerFileId)
                .orElseThrow { IllegalArgumentException("Checker file not found") }

            val checkerSource = String(checkerFile.content)

            // Use the new compileToBinary method
            runner.compileToBinary(checkerSource, testlibNeeded = true)
        } catch (e: Exception) {
            println("Failed to compile checker: ${e.message}")
            null
        }
    }
}