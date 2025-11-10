package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.*
import ru.nsu.problem_forge.entity.CheckerTest
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.repository.CheckerTestRepository
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.runner.Runner
import ru.nsu.problem_forge.type.problem.FileFormat
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime
import java.util.*

@Service
class ProblemCheckerService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val fileRepository: FileRepository,
    private val checkerTestRepository: CheckerTestRepository,
    private val runner: Runner
) {

    fun getChecker(problemId: Long, userId: Long): CheckerResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val checkerId = problem.problemInfo.checker ?: return CheckerResponse("", FileFormat.FILE_NOT_FOUND)

        val file = fileRepository.findById(checkerId)
            .orElseThrow { IllegalArgumentException("Checker file not found") }

        return CheckerResponse(
            file = Base64.getEncoder().encodeToString(file.content),
            format = file.format
        )
    }

    @Transactional
    fun setChecker(problemId: Long, userId: Long, checkerDto: CheckerDto): CheckerResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        // Create or update file
        val file = if (problem.problemInfo.checker != null) {
            val existingFile = fileRepository.findById(problem.problemInfo.checker!!)
                .orElseThrow { IllegalArgumentException("Checker file not found") }

            existingFile.apply {
                format = checkerDto.format
                content = Base64.getDecoder().decode(checkerDto.file)
                modifiedAt = LocalDateTime.now()
            }
        } else {
            File().apply {
                format = checkerDto.format
                content = Base64.getDecoder().decode(checkerDto.file)
                createdAt = LocalDateTime.now()
                modifiedAt = LocalDateTime.now()
            }
        }

        val savedFile = fileRepository.save(file)

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(checker = savedFile.id)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        return CheckerResponse(
            file = checkerDto.file,
            format = checkerDto.format
        )
    }

    @Transactional
    fun removeChecker(problemId: Long, userId: Long) {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val checkerId = problem.problemInfo.checker ?: return

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(checker = null, checkerLanguage = null)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        // Delete file
        fileRepository.deleteById(checkerId)

        // Delete all checker tests
        checkerTestRepository.findByProblemId(problemId).forEach {
            checkerTestRepository.delete(it)
        }
    }

    fun getCheckerSource(problemId: Long, userId: Long): CheckerFullResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val source = if (problem.problemInfo.checker != null) {
            val file = fileRepository.findById(problem.problemInfo.checker!!)
                .orElseThrow { IllegalArgumentException("Checker file not found") }
            String(file.content)
        } else ""

        val language = problem.problemInfo.checkerLanguage ?: "cpp"

        val tests = checkerTestRepository.findByProblemId(problemId).map {
            CheckerTestResponse(it.id!!, it.input, it.output, it.expected, it.verdict)
        }

        // For now, runResults is empty; could be cached or computed on demand
        val runResults = emptyMap<Long, String>()

        return CheckerFullResponse(source, language, tests, runResults)
    }

    @Transactional
    fun updateCheckerSource(problemId: Long, userId: Long, dto: CheckerSourceDto): CheckerFullResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        var fileId = problem.problemInfo.checker
        var language = problem.problemInfo.checkerLanguage ?: "cpp"

        // Update source if provided
        if (dto.source != null) {
            val file = if (fileId != null) {
                val existingFile = fileRepository.findById(fileId)
                    .orElseThrow { IllegalArgumentException("Checker file not found") }

                existingFile.apply {
                    content = dto.source.toByteArray()
                    modifiedAt = LocalDateTime.now()
                }
            } else {
                File().apply {
                    format = FileFormat.CPP_17 // Default, but could be determined from language
                    content = dto.source.toByteArray()
                    createdAt = LocalDateTime.now()
                    modifiedAt = LocalDateTime.now()
                }
            }

            val savedFile = fileRepository.save(file)
            fileId = savedFile.id
        }

        // Update language if provided
        if (dto.language != null) {
            language = dto.language
        }

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(
            checker = fileId,
            checkerLanguage = language
        )
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        // Return full response
        return getCheckerSource(problemId, userId)
    }

    fun getCheckerTests(problemId: Long, userId: Long): List<CheckerTestResponse> {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        return checkerTestRepository.findByProblemId(problemId).map {
            CheckerTestResponse(it.id!!, it.input, it.output, it.expected, it.verdict)
        }
    }

    @Transactional
    fun addCheckerTest(problemId: Long, userId: Long, dto: CheckerTestDto): CheckerTestResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val test = CheckerTest(
            problemId = problemId,
            input = dto.input,
            output = dto.output,
            expected = dto.expected,
            verdict = dto.verdict
        )

        val savedTest = checkerTestRepository.save(test)

        return CheckerTestResponse(
            savedTest.id!!,
            savedTest.input,
            savedTest.output,
            savedTest.expected,
            savedTest.verdict
        )
    }

    @Transactional
    fun updateCheckerTest(problemId: Long, testId: Long, userId: Long, dto: CheckerTestDto): CheckerTestResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val test = checkerTestRepository.findByProblemIdAndId(problemId, testId)
            ?: throw IllegalArgumentException("Test not found")

        val updatedTest = test.copy(
            input = dto.input,
            output = dto.output,
            expected = dto.expected,
            verdict = dto.verdict,
            modifiedAt = LocalDateTime.now()
        )

        val savedTest = checkerTestRepository.save(updatedTest)

        return CheckerTestResponse(
            savedTest.id!!,
            savedTest.input,
            savedTest.output,
            savedTest.expected,
            savedTest.verdict
        )
    }

    @Transactional
    fun deleteCheckerTest(problemId: Long, testId: Long, userId: Long) {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val test = checkerTestRepository.findByProblemIdAndId(problemId, testId)
            ?: throw IllegalArgumentException("Test not found")

        checkerTestRepository.delete(test)
    }

    fun runCheckerTests(problemId: Long, userId: Long): Map<Long, String> {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val checkerId = problem.problemInfo.checker
            ?: throw IllegalArgumentException("No checker configured")

        val file = fileRepository.findById(checkerId)
            .orElseThrow { IllegalArgumentException("Checker file not found") }

        val checkerSource = String(file.content)
        val language = problem.problemInfo.checkerLanguage ?: "cpp"

        val tests = checkerTestRepository.findByProblemId(problemId)

        val results = mutableMapOf<Long, String>()

        for (test in tests) {
            try {
                // Compile and run checker with test input/output
                val verdict = runner.runCheckerTest(checkerSource, language, test.input, test.output, test.expected)
                results[test.id!!] = verdict
            } catch (e: Exception) {
                results[test.id!!] = "CRASHED"
            }
        }

        return results
    }
}