package ru.nsu.problem_forge.dto

import java.time.LocalDateTime

data class TaskDto(
    val id: Long,
    val title: String,
    val description: String?,
    val orderIndex: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)