package ru.nsu.problem_forge.dto

import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

data class ContestDto(
    val id: Long,
    val name: String,
    val description: String?,
    val location: String?,
    val contestDate: LocalDateTime?,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateContestRequest(
    val name: String,
    val description: String?,
    val location: String?,
    val contestDate: LocalDateTime?
)

data class UpdateContestRequest(
    val name: String?,
    val description: String?,
    val location: String?,
    val contestDate: LocalDateTime?
)

data class AddUserToContestRequest(
    val handle: String,
    val role: Role = Role.VIEWER,
)

data class UpdateUserPermissionRequest(
    val userId: Long,
    val role: Role = Role.VIEWER,
)

data class ContestUserDto (
    val userId: Long,
    val userHandle: String,
    val userEmail: String,
    val role: Role
)