package ru.nsu.problem_forge.dto

import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

data class ProblemDto(
    val id: Long,
    val tag: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
)

data class CreateProblemRequest(
    val tag: String,
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