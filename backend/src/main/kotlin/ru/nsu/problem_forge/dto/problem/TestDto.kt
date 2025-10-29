package ru.nsu.problem_forge.dto.problem

import ru.nsu.problem_forge.type.problem.TestType

data class TestDto(
    val testType: TestType,
    val content: String = "",
    val description: String = "",
    val sample: Boolean = false,
    val points: Int = 1
)

data class TestResponse(
    val testId: Int,  // Derived from position in list
    val testType: TestType,
    val content: String = "",
    val description: String = "",
    val sample: Boolean = false,
    val points: Int = 1
)

enum class JobStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ERROR
}

enum class TestPreviewStatus {
    PENDING,
    GENERATING_INPUT,
    GENERATING_OUTPUT,
    COMPLETED,
    ERROR
}

data class TestPreview(
    val testNumber: Int,
    val input: String? = null,
    val output: String? = null,
    val status: TestPreviewStatus,
    val message: String? = null  // Added for error messages
)

data class TestPreviewRequest(
    val forceRegenerate: Boolean = false
)

data class TestPreviewResponse(
    val status: JobStatus,
    val message: String? = null,
    val tests: List<TestPreview> = emptyList()
)
