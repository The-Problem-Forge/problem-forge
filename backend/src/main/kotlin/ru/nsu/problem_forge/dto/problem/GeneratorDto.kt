package ru.nsu.problem_forge.dto.problem

import ru.nsu.problem_forge.type.FileFormat
import java.time.LocalDateTime

data class GeneratorDto(
    val file: String, // Base64 encoded file content
    val format: FileFormat,
    val alias: String
)

data class GeneratorResponse(
    val generatorId: Long,
    val file: String, // Base64 encoded file content
    val format: FileFormat,
    val alias: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
)