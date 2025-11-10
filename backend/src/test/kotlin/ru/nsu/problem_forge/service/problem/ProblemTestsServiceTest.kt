package ru.nsu.problem_forge.service.problem

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.nsu.problem_forge.dto.problem.JobStatus
import ru.nsu.problem_forge.dto.problem.TestPreview
import ru.nsu.problem_forge.dto.problem.TestPreviewResponse
import ru.nsu.problem_forge.dto.problem.TestPreviewStatus
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.runner.Runner
import ru.nsu.problem_forge.type.ProblemInfo
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.*
import java.time.LocalDateTime
import java.util.*

class ProblemTestsServiceTest {

  private lateinit var problemTestsService: ProblemTestsService

  private val problemRepository: ProblemRepository = mockk()
  private val problemUserRepository: ProblemUserRepository = mockk()
  private val fileRepository: FileRepository = mockk()
  private val runner: Runner = mockk()

  private val problemId = 1L
  private val userId = 1L
  private val user = ProblemUser(
    id = 1L,
    problem = mockk(), // Mock the Problem entity
    user = mockk(),    // Mock the User entity
    role = Role.EDITOR,
    modifiedAt = LocalDateTime.now()
  )

  @BeforeEach
  fun setUp() {
    problemTestsService = ProblemTestsService(
      problemRepository,
      problemUserRepository,
      fileRepository,
      runner
    )
  }

  @Test
  fun `getTestsPreview should throw when user has no access`() {
    // Given
    every { problemRepository.findById(problemId) } returns Optional.of(mockk())
    every { problemUserRepository.findByProblemIdAndUserId(problemId, userId) } returns null

    // When & Then
    assertThrows<SecurityException> {
      problemTestsService.getTestsPreview(problemId, userId)
    }
  }

  @Test
  fun `getTestsPreview should return cached results when available`() {
    // Given
    val problem = createProblemWithMainSolution()
    val cachedPreview = TestPreviewResponse(
      status = JobStatus.COMPLETED,
      tests = listOf(
        TestPreview(
          testNumber = 1,
          input = "cached input",
          output = "cached output",
          status = TestPreviewStatus.COMPLETED
        )
      )
    )

    // Mock file repository calls in calculateProblemChecksum
    val solutionFile = createFileWithContent("YOUR_SOLUTION_SOURCE_CODE_HERE")
    every { fileRepository.findById(1L) } returns Optional.of(solutionFile) // solution file ID

    every { problemRepository.findById(problemId) } returns Optional.of(problem)
    every { problemUserRepository.findByProblemIdAndUserId(problemId, userId) } returns user

    // Set up cache
    problemTestsService.previewGenerationCache[problemId] = cachedPreview
    problemTestsService.problemChecksumCache[problemId] = problemTestsService.calculateProblemChecksum(problem)

    // When
    val result = problemTestsService.getTestsPreview(problemId, userId)

    // Then
    assertEquals(JobStatus.COMPLETED, result.status)
    assertEquals(1, result.tests.size)
    assertEquals("cached input", result.tests[0].input)
    assertEquals("cached output", result.tests[0].output)
  }

  @Test
  fun `generatePreview should handle RAW tests correctly`() {
    // Given
    val problem = createProblemWithRawTests()
    val solutionFile = createFileWithContent("YOUR_SOLUTION_SOURCE_CODE_HERE")

    // Mock file repository for solution file
    every { fileRepository.findById(1L) } returns Optional.of(solutionFile) // solution file ID from createProblemWithRawTests

    // Mock runner for solution execution
    every { runner.run(any(), any()) } returns listOf(
      Runner.RunOutput(Runner.RunStatus.SUCCESS, "SOLUTION_OUTPUT_FOR_RAW_TEST")
    )

    every { problemRepository.findById(problemId) } returns Optional.of(problem)
    every { problemRepository.save(any()) } returns problem
    every { fileRepository.save(any()) } returnsArgument 0

    // When
    val result = problemTestsService.generatePreview(problemId, userId)

    // Then
    assertEquals(JobStatus.COMPLETED, result.status)
    assertEquals(1, result.tests.size)
    assertEquals(TestPreviewStatus.COMPLETED, result.tests[0].status)
    assertEquals("YOUR_RAW_TEST_CONTENT_HERE", result.tests[0].input) // RAW test uses test.content as input
    assertEquals("SOLUTION_OUTPUT_FOR_RAW_TEST", result.tests[0].output)

    // Verify runner was called with correct parameters
    verify { runner.run(any(), any()) }
  }

  @Test
  fun `generatePreview should handle generator failures gracefully`() {
    // Given
    val problem = createProblemWithGeneratedTests()
    val generatorFile = createFileWithContent("YOUR_GENERATOR_SOURCE_CODE_HERE")

    every { problemRepository.findById(problemId) } returns Optional.of(problem)
    every { fileRepository.findById(any()) } returns Optional.of(generatorFile)
    every { runner.run(any(), any(), any()) } returns listOf(
      Runner.RunOutput(Runner.RunStatus.RUNTIME_ERROR, "Generator crashed")
    )

    // When
    val result = problemTestsService.generatePreview(problemId, userId)

    // Then
    assertEquals(JobStatus.COMPLETED, result.status) // Overall completion
    assertEquals(TestPreviewStatus.ERROR, result.tests[0].status) // Individual test error
    assertTrue(result.tests[0].message!!.contains("Generator execution failed"))
  }

  private fun createProblemWithMainSolution(): Problem {
    return Problem().apply {
      id = problemId
      problemInfo = ProblemInfo(
        solutions = listOf(
          ProblemSolution(
            solutionId = 1L,
            name = "Test Solution 1",
            language = "cpp",
            author = userId,
            file = 1L,
            solutionType = SolutionType.MAIN_AC
          )
        ),
        tests = listOf(
          ProblemTest(
            testType = TestType.RAW,
            content = "YOUR_RAW_TEST_CONTENT_HERE"
          )
        ),
        generators = emptyList()
      )
    }
  }

  private fun createProblemWithRawTests(): Problem {
    return Problem().apply {
      id = problemId
      problemInfo = ProblemInfo(
        solutions = listOf(
          ProblemSolution(
            solutionId = 1L,
            name = "Test Solution 2",
            language = "cpp",
            author = userId,
            file = 1L,
            solutionType = SolutionType.MAIN_AC
          )
        ),
        tests = listOf(
          ProblemTest(
            testType = TestType.RAW,
            content = "YOUR_RAW_TEST_CONTENT_HERE"
          )
        ),
        generators = emptyList()
      )
    }
  }

  private fun createProblemWithGeneratedTests(): Problem {
    return Problem().apply {
      id = problemId
      problemInfo = ProblemInfo(
        solutions = listOf(
          ProblemSolution(
            solutionId = 1L,
            name = "Test Solution 3",
            language = "cpp",
            author = userId,
            file = 1L,
            solutionType = SolutionType.MAIN_AC
          )
        ),
        tests = listOf(
          ProblemTest(
            testType = TestType.GENERATED,
            content = "generator_alias",
            description = "Generated test"
          )
        ),
        generators = listOf(
          ProblemGenerator(
            generatorId = 1L,
            file = 2L,
            alias = "generator_alias"
          )
        )
      )
    }
  }

  private fun createValidCachedTest(): ProblemTest {
    return ProblemTest(
      testType = TestType.RAW,
      content = "test content",
      inputFileId = 1L,
      inputChecksum = "valid_checksum",
      outputFileId = 2L,
      outputChecksum = "valid_checksum"
    )
  }

  private fun createFileWithContent(content: String): File {
    return File().apply {
      id = 1L
      this.content = content.toByteArray()
      format = FileFormat.TEXT
      createdAt = LocalDateTime.now()
      modifiedAt = LocalDateTime.now()
    }
  }
}
