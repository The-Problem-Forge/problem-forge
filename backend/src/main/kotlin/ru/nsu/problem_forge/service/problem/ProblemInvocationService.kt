package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.*
import ru.nsu.problem_forge.entity.Invocation
import ru.nsu.problem_forge.entity.InvocationStatus
import ru.nsu.problem_forge.entity.InvocationTestResult
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.InvocationRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.service.nsuts.NsutsTestingService
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.ProblemTest
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class ProblemInvocationService(
  private val problemRepository: ProblemRepository,
  private val problemUserRepository: ProblemUserRepository,
  private val problemPackageService: ProblemPackageService,
  private val problemTestsService: ProblemTestsService,
  private val fileRepository: FileRepository,
  private val invocationRepository: InvocationRepository,
  private val nsutsTestingService: NsutsTestingService
) {

  private val invocationStatusCache = ConcurrentHashMap<Long, JobStatus>()
  private val invocationResultsCache = ConcurrentHashMap<Long, List<InvocationResponseDto>>()

  @Transactional(readOnly = true)
  fun runSolutions(
    problemId: Long,
    userId: Long
  ): InvocationStatusResponse {
    return runSolutionsSubset(problemId, userId, null, null)
  }

  @Transactional
  fun runSolutionsSubset(
    problemId: Long,
    userId: Long,
    solutionIds: List<Long>?,
    testIds: List<Long>?
  ): InvocationStatusResponse {
    // Validate problem access
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.VIEWER) {
      throw SecurityException("Viewer role required")
    }

    // Get all solutions and tests from problem info
    val allSolutions = problem.problemInfo.solutions
    val allTests = problem.problemInfo.tests

    // Filter by provided IDs if any
    val filteredSolutions = if (solutionIds != null) {
      allSolutions.filter { it.solutionId in solutionIds }
    } else {
      allSolutions
    }

    val filteredTests = if (testIds != null) {
      val result = mutableListOf<ProblemTest>()
      val testIdSet = testIds.toSet()
      for (i in allTests.indices) {
        val testId = (i + 1).toLong()
        if (testIdSet.contains(testId)) {
          result.add(allTests[i])
        }
      }
      result
    } else {
      allTests
    }

    if (filteredSolutions.isEmpty()) {
      throw IllegalArgumentException("No solutions found for the given criteria")
    }

    // Create invocation record
    val invocation = invocationRepository.save(
      Invocation(
        problemId = problemId,
        userId = userId,
        solutionIds = filteredSolutions.map { it.solutionId },
        testIds = filteredTests.mapIndexed { index, _ -> (index + 1).toLong() }
      )
    )

    // Check if invocation is already in progress
    val currentStatus = invocationStatusCache[invocation.id]
    if (currentStatus == JobStatus.IN_PROGRESS) {
      return InvocationStatusResponse(
        status = JobStatus.IN_PROGRESS,
        message = "Solutions are being tested. Please wait...",
        results = null
      )
    }

    // Check if we have cached results
    val cachedResults = invocationResultsCache[invocation.id]
    if (cachedResults != null) {
      return InvocationStatusResponse(
        status = JobStatus.COMPLETED,
        message = "Test results are ready",
        results = cachedResults
      )
    }

    // Start async invocation
    val general = problem.problemInfo.general
    val solutions = filteredSolutions.map { solution ->
      val file = fileRepository.findById(solution.file)
        .orElseThrow { IllegalArgumentException("File not found for solution ${solution.solutionId}") }

      file.content.toString(Charset.defaultCharset())
    }

    startAsyncInvocation(
      invocation.id, problemId, userId,
      general.timeLimit.toLong(), general.memoryLimit.toLong(),
      solutions, filteredTests.mapIndexed { index, _ -> (index + 1).toLong() }
    )

    return InvocationStatusResponse(
      status = JobStatus.IN_PROGRESS,
      message = "Starting solutions testing...",
      results = null
    )
  }

  private fun startAsyncInvocation(
    invocationId: Long,
    problemId: Long,
    userId: Long,
    timeLimit: Long,
    memoryLimit: Long,
    solutions: List<String>,
    testIds: List<Long>
  ) {
    invocationStatusCache[invocationId] = JobStatus.IN_PROGRESS

    // Update invocation status to IN_PROGRESS
    invocationRepository.findById(invocationId).ifPresent { invocation ->
      invocation.status = InvocationStatus.IN_PROGRESS
      invocationRepository.save(invocation)
    }

    Thread {
      try {
        // First, get the problem package
        // TODO: problem that we are not checking cache.
        val previewGenerationRes = problemTestsService.generatePreview(problemId, userId)
        val packageData = problemPackageService.generatePackageSync(problemId, userId)

        // Run solutions using NSUTS service
        val results = nsutsTestingService.runSolutions(
          packageData, problemId, timeLimit, memoryLimit, solutions, false
        )

        // Convert results to our format
        val resultsMap: Map<Long, List<InvocationTestResult>> = results.associate { response ->
          response.solutionId to response.testResults.map { testResult ->
            InvocationTestResult(
              testNumber = testResult.testNumber,
              verdict = testResult.resultCode,
              description = testResult.resultDescription,
              timeMs = testResult.usedTimeMs,
              memoryKb = testResult.usedMemoryKb
            )
          }
        }

        // Update invocation with results
        invocationRepository.findById(invocationId).ifPresent { invocation ->
          invocation.status = InvocationStatus.COMPLETED
          invocation.completedAt = LocalDateTime.now()
          invocation.results = resultsMap
          invocationRepository.save(invocation)
        }

        // Update caches
        invocationResultsCache[invocationId] = results
        invocationStatusCache[invocationId] = JobStatus.COMPLETED

      } catch (e: Exception) {
        // Update invocation with error
        invocationRepository.findById(invocationId).ifPresent { invocation ->
          invocation.status = InvocationStatus.ERROR
          invocation.completedAt = LocalDateTime.now()
          invocation.errorMessage = e.message
          invocationRepository.save(invocation)
        }

        invocationStatusCache[invocationId] = JobStatus.ERROR
        println("Error in async invocation for invocation $invocationId: ${e.message}")
        e.printStackTrace()
      }
    }.start()
  }

  @Transactional(readOnly = true)
  fun getInvocations(problemId: Long, userId: Long): List<InvocationDto> {
    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.VIEWER) {
      throw SecurityException("Viewer role required")
    }

    return invocationRepository.findByProblemIdOrderByCreatedAtDesc(problemId).map { invocation ->
      InvocationDto(
        id = invocation.id,
        problemId = invocation.problemId,
        status = invocation.status.name,
        createdAt = invocation.createdAt,
        completedAt = invocation.completedAt,
        solutionIds = invocation.solutionIds,
        testIds = invocation.testIds,
        errorMessage = invocation.errorMessage
      )
    }
  }

  @Transactional
  fun createInvocation(
    problemId: Long,
    userId: Long,
    request: CreateInvocationRequest
  ): InvocationDto {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    // Validate solution and test IDs
    val allSolutionIds = problem.problemInfo.solutions.map { it.solutionId }
    val allTestIds = problem.problemInfo.tests.mapIndexed { index, _ -> (index + 1).toLong() }

    val invalidSolutionIds = request.solutionIds.subtract(allSolutionIds.toSet())
    val invalidTestIds = request.testIds.subtract(allTestIds.toSet())

    if (invalidSolutionIds.isNotEmpty()) {
      throw IllegalArgumentException("Invalid solution IDs: $invalidSolutionIds")
    }

    if (invalidTestIds.isNotEmpty()) {
      throw IllegalArgumentException("Invalid test IDs: $invalidTestIds; Given test Ids: ${request.testIds.toSet()}; Available tests: ${allTestIds}")
    }

    val invocation = invocationRepository.save(
      Invocation(
        problemId = problemId,
        userId = userId,
        solutionIds = request.solutionIds,
        testIds = request.testIds
      )
    )

    // Start async invocation
    val general = problem.problemInfo.general
    val solutions = request.solutionIds.map { solutionId ->
      val solution = problem.problemInfo.solutions.find { it.solutionId == solutionId }
        ?: throw IllegalArgumentException("Solution not found: $solutionId")
      val file = fileRepository.findById(solution.file)
        .orElseThrow { IllegalArgumentException("File not found for solution $solutionId") }
      file.content.toString(Charset.defaultCharset())
    }

    startAsyncInvocation(
      invocation.id, problemId, userId,
      general.timeLimit.toLong(), general.memoryLimit.toLong(),
      solutions, request.testIds
    )

    return InvocationDto(
      id = invocation.id,
      problemId = invocation.problemId,
      status = invocation.status.name,
      createdAt = invocation.createdAt,
      completedAt = invocation.completedAt,
      solutionIds = invocation.solutionIds,
      testIds = invocation.testIds,
      errorMessage = invocation.errorMessage
    )
  }

  @Transactional(readOnly = true)
  fun getInvocationMatrix(
    problemId: Long,
    userId: Long,
    invocationId: Long
  ): InvocationMatrixResponse {
    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.VIEWER) {
      throw SecurityException("Viewer role required")
    }

    val invocation = invocationRepository.findById(invocationId)
      .orElseThrow { IllegalArgumentException("Invocation not found") }

    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val solutions = invocation.solutionIds.map { solutionId ->
      val solution = problem.problemInfo.solutions.find { it.solutionId == solutionId }
        ?: throw IllegalArgumentException("Solution not found: $solutionId")
      InvocationSolutionDto(
        id = solution.solutionId,
        name = solution.name
      )
    }

    val tests = invocation.testIds.map { testId ->
      val testIndex = (testId - 1).toInt() // Convert back to 0-based index
      if (testIndex < 0 || testIndex >= problem.problemInfo.tests.size) {
        throw IllegalArgumentException("Test not found: $testId")
      }
      val test = problem.problemInfo.tests[testIndex]
      InvocationTestDto(
        id = testId,
        testNumber = testId.toInt()
      )
    }

    val results = invocation.results?.mapValues { (_, testResults) ->
      testResults.map { result ->
        InvocationTestResultDto(
          testNumber = result.testNumber,
          verdict = result.verdict,
          description = result.description,
          timeMs = result.timeMs,
          memoryKb = result.memoryKb
        )
      }
    } ?: emptyMap()

    return InvocationMatrixResponse(
      invocationId = invocation.id,
      status = invocation.status.name,
      solutions = solutions,
      tests = tests,
      results = results
    )
  }
}
