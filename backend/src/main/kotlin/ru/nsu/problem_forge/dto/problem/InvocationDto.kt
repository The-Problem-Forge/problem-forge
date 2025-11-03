package ru.nsu.problem_forge.dto.problem


data class InvocationResponseDto(
    val solutionId: Long,
    val testResults: List<InvocationResponseTestDto>
)

data class InvocationResponseTestDto(
    val testNumber: Int,
    val resultCode: String,
    val resultDescription: String,
    val usedTimeMs: Int,
    val usedMemoryKb: Int
)

data class InvocationStatusResponse(
    val status: JobStatus,
    val message: String? = null,
    val results: List<InvocationResponseDto>? = null
)