package ru.nsu.problem_forge.dto.problem

import ru.nsu.problem_forge.type.problem.FileFormat

data class CheckerDto(
    val file: String, // Base64 encoded file content
    val format: FileFormat
)

data class CheckerResponse(
    val file: String,
    val format: FileFormat // Base64 encoded file content
)

