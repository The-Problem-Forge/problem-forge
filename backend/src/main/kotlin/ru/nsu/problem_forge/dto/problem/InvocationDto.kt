package ru.nsu.problem_forge.dto.problem

import java.time.LocalDateTime

data class InvocationDto(
    val id: Long,
    val problemId: Long,
    val status: String,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val solutionIds: List<Long>,
    val testIds: List<Long>,
    val errorMessage: String?
)

data class CreateInvocationRequest(
    val solutionIds: List<Long>,
    val testIds: List<Long>
)

data class InvocationMatrixResponse(
    val invocationId: Long,
    val status: String,
    val solutions: List<InvocationSolutionDto>,
    val tests: List<InvocationTestDto>,
    val results: Map<Long, List<InvocationTestResultDto>>
)

data class InvocationSolutionDto(
    val id: Long,
    val name: String
)

data class InvocationTestDto(
    val id: Long,
    val testNumber: Int
)

data class InvocationTestResultDto(
    val testNumber: Int,
    val verdict: String,
    val description: String,
    val timeMs: Int,
    val memoryKb: Int
)

data class InvocationStatusResponse(
    val status: JobStatus,
    val message: String,
    val results: List<InvocationResponseDto>?
)

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