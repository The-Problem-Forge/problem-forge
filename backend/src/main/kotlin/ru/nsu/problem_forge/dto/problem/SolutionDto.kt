package ru.nsu.problem_forge.dto.problem

import ru.nsu.problem_forge.type.FileFormat
import ru.nsu.problem_forge.type.problem.SolutionType
import java.time.LocalDateTime

data class SolutionDto(
    val file: String, // Base64 encoded file content
    val format: FileFormat,
    val solutionType: SolutionType
)

data class SolutionResponse (
    val id: Long,
    val author: Long,
    val file: String, // Base64 encoded file content
    val format: FileFormat,
    val solutionType: SolutionType,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
)


