package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.*
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.runner.Runner
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.*
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ProblemTestsService(
  private val problemRepository: ProblemRepository,
  private val problemUserRepository: ProblemUserRepository,
  private val fileRepository: FileRepository,
  private val runner: Runner
) {

  val jobStatusCache = ConcurrentHashMap<Long, JobStatus>()
  val previewGenerationCache = ConcurrentHashMap<Long, TestPreviewResponse>()
  val problemChecksumCache = ConcurrentHashMap<Long, String>()

  @Transactional(readOnly = true)
  fun getTestsPreview(problemId: Long, userId: Long): TestPreviewResponse {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.VIEWER) {
      throw SecurityException("Viewer role required")
    }

    // Check for main solution
    if (problem.problemInfo.solutions.none { it.solutionType == SolutionType.MAIN_AC }) {
      throw IllegalArgumentException("No main correct solution found")
    }

    // Check if generation is in progress
    if (jobStatusCache[problemId] == JobStatus.IN_PROGRESS) {
      return TestPreviewResponse(
        status = JobStatus.IN_PROGRESS,
        message = "Test preview generation is in progress. Please wait..."
      )
    }

    // Calculate current problem checksum
    val currentChecksum = calculateProblemChecksum(problem)

    // Check if we have valid cached preview
    val cachedPreview = previewGenerationCache[problemId]
    val cachedChecksum = problemChecksumCache[problemId]

    if (cachedPreview != null && cachedChecksum == currentChecksum) {
      return cachedPreview
    }

    // Check if database has valid cached data
    if (isAllTestsCachedAndValid(problem)) {
      val preview = buildPreviewFromCache(problem.problemInfo.tests)
      previewGenerationCache[problemId] = preview
      problemChecksumCache[problemId] = currentChecksum
      return preview
    }

    // Try to generate a preview from scratch
    try {
      val result = generatePreview(problemId, userId)
      previewGenerationCache[problemId] = result
      problemChecksumCache[problemId] = currentChecksum
      jobStatusCache[problemId] = JobStatus.COMPLETED

      return result
    } catch (e: Exception) {
      jobStatusCache[problemId] = JobStatus.ERROR
      println("Error in async preview generation for problem $problemId: ${e.message}")

      return TestPreviewResponse(
        status = JobStatus.ERROR,
        message = "Failed to generate a preview: ${e.message}"
      )
    }
  }

  fun calculateProblemChecksum(problem: Problem): String {
    val checksumData = StringBuilder()

    // Include problem ID and modification time
    checksumData.append(problem.id)

    // Include tests data
    problem.problemInfo.tests.forEach { test ->
      checksumData.append(test.testType)
      checksumData.append(test.content)
    }

    // Include generators data
    problem.problemInfo.generators.forEach { generator ->
      val generatorFile = fileRepository.findById(generator.file).orElse(null)
      if (generatorFile != null) {
        checksumData.append(generator.generatorId)
        checksumData.append(generator.alias)
        checksumData.append(String(generatorFile.content))
      }
    }

    // Include main solution data
    val mainSolution = problem.problemInfo.solutions.find { it.solutionType == SolutionType.MAIN_AC }
    if (mainSolution != null) {
      val solutionFile = fileRepository.findById(mainSolution.file).orElse(null)
      if (solutionFile != null) {
        checksumData.append(mainSolution.solutionId)
        checksumData.append(String(solutionFile.content))
      }
    }

    return calculateChecksum(checksumData.toString())
  }

  fun calculateOutputChecksum(solutionContent: String, solutionFormat: FileFormat): String {
    return calculateChecksum(solutionContent + solutionFormat)
  }

  fun calculateRawInputChecksum(inputContent: String): String {
    return calculateChecksum(inputContent)
  }

  fun calculateGeneratedInputChecksum(generatorSource: String, callerString: String): String {
    return calculateChecksum(generatorSource + callerString)
  }

  private fun isAllTestsCachedAndValid(problem: Problem): Boolean {
    return problem.problemInfo.tests.all { test ->
      test.inputFileId != null &&
              test.outputFileId != null &&
              test.inputChecksum != null &&
              test.outputChecksum != null &&
              isTestInputValid(test, problem) &&
              isTestOutputValid(test, problem)
    }
  }

  private fun isTestInputValid(test: ProblemTest, problem: Problem): Boolean {
    if (test.inputFileId == null || test.inputChecksum == null) return false

    when (test.testType) {
      TestType.RAW -> {
        // For RAW tests: checksum = inputContent + test.content
        val currentChecksum = calculateRawInputChecksum(test.content)
        return currentChecksum == test.inputChecksum
      }

      TestType.GENERATED -> {
        // For GENERATED tests: checksum = inputContent + generator_source + test.content
        val generator = problem.problemInfo.generators.find { it.alias == test.content.split(" ")[0] }
          ?: return false // Generator not found

        val generatorFile = fileRepository.findById(generator.file).orElse(null)
          ?: return false // Generator file not found

        val generatorSource = String(generatorFile.content)
        val currentChecksum = calculateGeneratedInputChecksum(generatorSource, test.content)
        return currentChecksum == test.inputChecksum
      }
    }
  }

  private fun isTestOutputValid(test: ProblemTest, problem: Problem): Boolean {
    // Here we know that input did not changed...
    if (test.outputFileId == null || test.outputChecksum == null) return false

    val mainSolution = problem.problemInfo.solutions.find { it.solutionType == SolutionType.MAIN_AC }
    if (mainSolution == null) return false

    val solutionFile = fileRepository.findById(mainSolution.file).orElse(null) ?: return false


    val solutionSource = String(solutionFile.content)

    val currentChecksum = calculateOutputChecksum(solutionSource, solutionFile.format)
    return currentChecksum == test.outputChecksum
  }

  private fun startAsyncPreviewGeneration(problemId: Long, userId: Long, checksum: String) {
    jobStatusCache[problemId] = JobStatus.IN_PROGRESS

    Thread {
      try {
        val result = generatePreview(problemId, userId)
        previewGenerationCache[problemId] = result
        problemChecksumCache[problemId] = checksum
        jobStatusCache[problemId] = JobStatus.COMPLETED
      } catch (e: Exception) {
        jobStatusCache[problemId] = JobStatus.ERROR
        println("Error in async preview generation for problem $problemId: ${e.message}")
      }
    }.start()
  }

  @Transactional
  fun generatePreview(problemId: Long, userId: Long): TestPreviewResponse {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val mainSolution = problem.problemInfo.solutions.find { it.solutionType == SolutionType.MAIN_AC }
      ?: throw IllegalArgumentException("No main correct solution found")

    val tests = problem.problemInfo.tests
    val generators = problem.problemInfo.generators

    val mainSolutionFile = fileRepository.findById(mainSolution.file)
      .orElseThrow { IllegalArgumentException("Main solution file not found") }

    val mainSolutionSource = String(mainSolutionFile.content)

    val previewTests = mutableListOf<TestPreview>()
    val updatedTests = mutableListOf<ProblemTest>()

    tests.forEachIndexed { index, test ->
      val testNumber = index + 1

      try {
        val needsRegeneration = needsTestRegeneration(test, problem)

        if (needsRegeneration) {
          val (inputContent, inputChecksum) = generateInput(test, generators)
          val (outputContent, outputChecksum) = generateOutput(
            mainSolutionSource,
            mainSolutionFile.format,
            inputContent,
          )

          val (inputFileId, outputFileId) = saveTestFiles(
            inputContent,
            outputContent,
            inputChecksum,
            outputChecksum
          )

          val updatedTest = test.copy(
            inputFileId = inputFileId,
            inputChecksum = inputChecksum,
            outputFileId = outputFileId,
            outputChecksum = outputChecksum
          )
          updatedTests.add(updatedTest)

          previewTests.add(
            TestPreview(
              testNumber = testNumber,
              input = inputContent,
              output = outputContent,
              status = TestPreviewStatus.COMPLETED
            )
          )
        } else {
          val inputContent = test.inputFileId?.let { fileId ->
            fileRepository.findById(fileId).map { String(it.content) }.orElse(null)
          }

          val outputContent = test.outputFileId?.let { fileId ->
            fileRepository.findById(fileId).map { String(it.content) }.orElse(null)
          }

          previewTests.add(
            TestPreview(
              testNumber = testNumber,
              input = inputContent,
              output = outputContent,
              status = TestPreviewStatus.COMPLETED
            )
          )
          updatedTests.add(test)
        }

      } catch (e: Exception) {
        throw Exception("Failed to regenerate a test: ${e}")
        previewTests.add(
          TestPreview(
            testNumber = testNumber,
            input = null,
            output = null,
            status = TestPreviewStatus.ERROR,
            message = e.message
          )
        )
        updatedTests.add(test)
      }
    }

    // Save changes to database
    if (updatedTests != tests) {
      val updatedProblemInfo = problem.problemInfo.copy(tests = updatedTests)
      problem.problemInfo = updatedProblemInfo
      problemRepository.save(problem)
    }

    return TestPreviewResponse(
      status = JobStatus.COMPLETED,
      tests = previewTests
    )
  }


  private fun needsTestRegeneration(test: ProblemTest, problem: Problem): Boolean {
    // If test has no cached data, it needs regeneration
    if (test.inputFileId == null || test.outputFileId == null) {
      return true
    }

    // Check if input is still valid
    if (!isTestInputValid(test, problem)) {
      return true
    }

    // Check if output is still valid
    if (!isTestOutputValid(test, problem)) {
      return true
    }

    return false
  }

  private fun generateInput(test: ProblemTest, generators: List<ProblemGenerator>): Pair<String, String> {
    val inputContent = when (test.testType) {
      TestType.RAW -> test.content
      TestType.GENERATED -> {
        val alias = test.content.split(" ")[0]
        val args = test.content.split(" ").subList(1, test.content.split(" ").size)
        val generator = generators.find { it.alias == alias }
          ?: throw IllegalArgumentException("Generator with alias '${alias}' not found")

        val generatorFile = fileRepository.findById(generator.file)
          .orElseThrow { IllegalArgumentException("Generator file not found") }

        val generatorSource = String(generatorFile.content)

        // Run generator to produce input
        val runInputs = listOf(Runner.RunInput(inputContent = "", args = args))
        val runOutputs = runner.run(generatorSource, runInputs, true)

        if (runOutputs.isEmpty() || runOutputs[0].status != Runner.RunStatus.SUCCESS) {
          throw IllegalArgumentException("Generator execution failed: ${runOutputs.getOrNull(0)?.status}")
        }

        runOutputs[0].outputContent
      }
    }

    // Calculate appropriate checksum based on test type
    val checksum = when (test.testType) {
      TestType.RAW -> calculateRawInputChecksum(inputContent)
      TestType.GENERATED -> {
        val generator = generators.find { it.alias == test.content.split(" ")[0] }!!
        val generatorFile = fileRepository.findById(generator.file).get()
        val generatorSource = String(generatorFile.content)
        calculateGeneratedInputChecksum(generatorSource, test.content)
      }
    }

    return Pair(inputContent, checksum)
  }

  private fun generateOutput(
    solutionSource: String,
    solutionFormat: FileFormat,
    inputContent: String
  ): Pair<String, String> {
    val runInputs = listOf(Runner.RunInput(inputContent = inputContent))
    val runOutputs = runner.run(solutionSource, runInputs)

    if (runOutputs.isEmpty() || runOutputs[0].status != Runner.RunStatus.SUCCESS) {
      throw IllegalArgumentException(
        "Solution execution failed: ${runOutputs.getOrNull(0)?.status} with output: ${
          runOutputs.getOrNull(
            0
          )?.outputContent
        }"
      )
    }

    val outputContent = runOutputs[0].outputContent
    val checksum = calculateOutputChecksum(solutionSource, solutionFormat)

    return Pair(outputContent, checksum)
  }

  private fun saveTestFiles(
    inputContent: String,
    outputContent: String,
    inputChecksum: String,
    outputChecksum: String
  ): Pair<Long, Long> {
    // Always create new files to ensure consistency
    val inputFile = File().apply {
      format = FileFormat.TEXT
      content = inputContent.toByteArray()
      createdAt = LocalDateTime.now()
      modifiedAt = LocalDateTime.now()
    }
    val savedInputFile = fileRepository.save(inputFile)

    val outputFile = File().apply {
      format = FileFormat.TEXT
      content = outputContent.toByteArray()
      createdAt = LocalDateTime.now()
      modifiedAt = LocalDateTime.now()
    }
    val savedOutputFile = fileRepository.save(outputFile)

    return Pair(savedInputFile.id, savedOutputFile.id)
  }

  private fun buildPreviewFromCache(tests: List<ProblemTest>): TestPreviewResponse {
    val previewTests = tests.mapIndexed { index, test ->
      val inputContent = test.inputFileId?.let { fileId ->
        fileRepository.findById(fileId).map { String(it.content) }.orElse(null)
      }

      val outputContent = test.outputFileId?.let { fileId ->
        fileRepository.findById(fileId).map { String(it.content) }.orElse(null)
      }

      TestPreview(
        testNumber = index + 1,
        input = inputContent,
        output = outputContent,
        status = if (inputContent != null && outputContent != null)
          TestPreviewStatus.COMPLETED else TestPreviewStatus.ERROR
      )
    }

    return TestPreviewResponse(
      status = JobStatus.COMPLETED,
      tests = previewTests
    )
  }

  private fun calculateChecksum(data: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
    return Base64.getEncoder().encodeToString(bytes)
  }

  fun getTests(problemId: Long, userId: Long): List<TestResponse> {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    return problem.problemInfo.tests.mapIndexed { index, test ->
      TestResponse(
        testId = index + 1, // 1-based numbering
        testType = test.testType,
        content = test.content,
        description = test.description,
        sample = test.sample,
        points = test.points
      )
    }
  }

  @Transactional
  fun addTest(problemId: Long, userId: Long, testDto: TestDto): TestResponse {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    // Validate test data
    validateTest(testDto)

    // Create test
    val test = ProblemTest(
      testType = testDto.testType,
      content = testDto.content,
      description = testDto.description,
      sample = testDto.sample,
      points = testDto.points
    )

    // Update problem info - add to end of list
    val currentTests = problem.problemInfo.tests.toMutableList()
    currentTests.add(test)

    val updatedProblemInfo = problem.problemInfo.copy(tests = currentTests)
    problem.problemInfo = updatedProblemInfo
    problem.modifiedAt = LocalDateTime.now()

    problemRepository.save(problem)

    return TestResponse(
      testId = currentTests.size, // New test is at the end
      testType = test.testType,
      content = test.content,
      description = test.description,
      sample = test.sample,
      points = test.points
    )
  }

  @Transactional
  fun updateTest(problemId: Long, testNumber: Int, userId: Long, testDto: TestDto): TestResponse {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    // Validate test number exists
    val currentTests = problem.problemInfo.tests.toMutableList()
    if (testNumber < 1 || testNumber > currentTests.size) {
      throw IllegalArgumentException("Test number $testNumber not found. Available tests: 1-${currentTests.size}")
    }

    // Validate test data
    validateTest(testDto)

    // Update test at position (testNumber - 1) since it's 1-based
    val testIndex = testNumber - 1
    val oldTest = currentTests[testIndex]

    val updatedTest = oldTest.copy(
      testType = testDto.testType,
      content = testDto.content,
      description = testDto.description,
      sample = testDto.sample,
      points = testDto.points
    )
    currentTests[testIndex] = updatedTest

    // Update problem info
    val updatedProblemInfo = problem.problemInfo.copy(tests = currentTests)
    problem.problemInfo = updatedProblemInfo
    problem.modifiedAt = LocalDateTime.now()

    problemRepository.save(problem)

    return TestResponse(
      testId = testNumber,
      testType = updatedTest.testType,
      content = updatedTest.content,
      description = updatedTest.description,
      sample = updatedTest.sample,
      points = updatedTest.points
    )
  }

  @Transactional
  fun deleteTest(problemId: Long, testNumber: Int, userId: Long) {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    // Validate test number exists
    val currentTests = problem.problemInfo.tests.toMutableList()
    if (testNumber < 1 || testNumber > currentTests.size) {
      throw IllegalArgumentException("Test number $testNumber not found. Available tests: 1-${currentTests.size}")
    }

    // Remove test at position (testNumber - 1)
    currentTests.removeAt(testNumber - 1)

    // Update problem info
    val updatedProblemInfo = problem.problemInfo.copy(tests = currentTests)
    problem.problemInfo = updatedProblemInfo
    problem.modifiedAt = LocalDateTime.now()

    problemRepository.save(problem)
  }

  @Transactional
  fun reorderTests(problemId: Long, userId: Long, newOrder: List<Int>): List<TestResponse> {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    val currentTests = problem.problemInfo.tests.toMutableList()

    // Validate new order
    if (newOrder.size != currentTests.size) {
      throw IllegalArgumentException("New order must contain exactly ${currentTests.size} test numbers")
    }

    if (newOrder.toSet().size != newOrder.size) {
      throw IllegalArgumentException("New order contains duplicate test numbers")
    }

    if (newOrder.any { it < 1 || it > currentTests.size }) {
      throw IllegalArgumentException("New order contains invalid test numbers. Must be between 1 and ${currentTests.size}")
    }

    // Reorder tests
    val reorderedTests = newOrder.map { newPosition ->
      val oldIndex = newPosition - 1
      currentTests[oldIndex]
    }

    // Update problem info
    val updatedProblemInfo = problem.problemInfo.copy(tests = reorderedTests)
    problem.problemInfo = updatedProblemInfo
    problem.modifiedAt = LocalDateTime.now()

    problemRepository.save(problem)

    // Return updated list with new numbers
    return reorderedTests.mapIndexed { index, test ->
      TestResponse(
        testId = index + 1,
        testType = test.testType,
        content = test.content,
        description = test.description,
        sample = test.sample,
        points = test.points
      )
    }
  }

  private fun validateTest(testDto: TestDto) {
    require(testDto.points >= 0) { "Points must be non-negative" }

    when (testDto.testType) {
      TestType.RAW -> {
        require(testDto.content.isNotBlank()) { "Content is required for RAW tests" }
      }

      TestType.GENERATED -> {
        require(testDto.content.isNotBlank()) { "Generator alias is required for GENERATED tests" }
      }
    }
  }
}
