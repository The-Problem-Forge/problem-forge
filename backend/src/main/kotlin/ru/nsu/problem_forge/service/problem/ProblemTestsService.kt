package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.TestDto
import ru.nsu.problem_forge.dto.problem.TestResponse
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.ProblemTest
import ru.nsu.problem_forge.type.problem.TestType
import java.time.LocalDateTime

@Service
class ProblemTestsService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository
) {

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