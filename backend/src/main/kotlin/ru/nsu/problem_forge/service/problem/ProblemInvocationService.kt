package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.InvocationResponseDto
import ru.nsu.problem_forge.dto.problem.InvocationStatusResponse
import ru.nsu.problem_forge.dto.problem.JobStatus
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.service.nsuts.NsutsTestingService
import ru.nsu.problem_forge.type.Role
import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap

@Service
class ProblemInvocationService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val problemPackageService: ProblemPackageService,
    private val fileRepository: FileRepository,
    private val nsutsTestingService: NsutsTestingService
) {

    private val invocationStatusCache = ConcurrentHashMap<Long, JobStatus>()
    private val invocationResultsCache = ConcurrentHashMap<Long, List<InvocationResponseDto>>()

    @Transactional(readOnly = true)
    fun runSolutions(
        problemId: Long,
        userId: Long
    ): InvocationStatusResponse {
        // Validate problem access
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.VIEWER) {
            throw SecurityException("Viewer role required")
        }

        // Check if invocation is already in progress
        val currentStatus = invocationStatusCache[problemId]
        if (currentStatus == JobStatus.IN_PROGRESS) {
            return InvocationStatusResponse(
                status = JobStatus.IN_PROGRESS,
                message = "Solutions are being tested. Please wait...",
                results = null
            )
        }

        // Check if we have cached results
        val cachedResults = invocationResultsCache[problemId]
        if (cachedResults != null) {
            return InvocationStatusResponse(
                status = JobStatus.COMPLETED,
                message = "Test results are ready",
                results = cachedResults
            )
        }

        // Start async invocation
        val general = problem.problemInfo.general
        val solutions = problem.problemInfo.solutions.map { solution ->
            val file = fileRepository.findById(solution.file)
                .orElseThrow { IllegalArgumentException("File not found for solution ${solution.solutionId}") }

            file.content.toString(Charset.defaultCharset())
        }

        startAsyncInvocation(
            problemId, userId,
            general.timeLimit.toLong(), general.memoryLimit.toLong(),
            solutions
        )

        return InvocationStatusResponse(
            status = JobStatus.IN_PROGRESS,
            message = "Starting solutions testing...",
            results = null
        )
    }

    private fun startAsyncInvocation(
        problemId: Long,
        userId: Long,
        timeLimit: Long,
        memoryLimit: Long,
        solutions: List<String>
    ) {
        invocationStatusCache[problemId] = JobStatus.IN_PROGRESS

        Thread {
            try {
                // First, get the problem package
                // TODO: problem that we are not checking cache.
                val packageData = problemPackageService.generatePackageSync(problemId, userId)

                // Run solutions using NSUTS service
                val results = nsutsTestingService.runSolutions(
                    packageData, problemId, timeLimit, memoryLimit, solutions, false
                )

                // Update caches
                invocationResultsCache[problemId] = results
                invocationStatusCache[problemId] = JobStatus.COMPLETED

            } catch (e: Exception) {
                invocationStatusCache[problemId] = JobStatus.ERROR
                println("Error in async invocation for problem $problemId: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }
}