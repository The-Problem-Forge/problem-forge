package ru.nsu.problem_forge.dto

data class TaskDto(
    val id: Long? = null,
    val title: String,
    val description: String? = null
)