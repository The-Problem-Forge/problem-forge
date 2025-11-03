package ru.nsu.problem_forge.dto

import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

data class ProblemDto(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
)

data class CreateProblemRequest(
    val title: String,
    val contestId: Long? = null
)

data class AddUserToProblemRequest(
    val userId: Long,
    val role: Role
)

data class ProblemUserDto(
    val userId: Long,
    val userHandle: String,
    val role: Role,
    val modifiedAt: LocalDateTime
)

data class GeneralDto(
    val title: String? = null,
    val inputFile: String? = null,
    val outputFile: String? = null,
    val timeLimit: Int? = null,
    val memoryLimit: Int? = null
)

data class GeneralResponse(
    val title: String,
    val inputFile: String,
    val outputFile: String,
    val timeLimit: Int,
    val memoryLimit: Int
)

data class StatementDto(
    val name: String? = null,
    val legend: String? = null,
    val inputFormat: String? = null,
    val outputFormat: String? = null,
    val scoring: String? = null,
    val notes: String? = null,
    val tutorial: String? = null
)

data class StatementResponse(
    val name: String,
    val legend: String,
    val inputFormat: String,
    val outputFormat: String,
    val scoring: String,
    val notes: String,
    val tutorial: String
)
