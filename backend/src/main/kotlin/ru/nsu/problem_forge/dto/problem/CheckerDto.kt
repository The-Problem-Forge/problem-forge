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

// New DTOs for checker with source and tests
data class CheckerSourceDto(
    val source: String? = null,
    val language: String? = null
)

data class CheckerTestDto(
    val input: String,
    val output: String,
    val expected: String,
    val verdict: String = "OK"
)

data class CheckerTestResponse(
    val id: Long,
    val input: String,
    val output: String,
    val expected: String,
    val verdict: String
)

data class CheckerRunResult(
    val testId: Long,
    val verdict: String
)

data class CheckerFullResponse(
    val source: String,
    val language: String,
    val tests: List<CheckerTestResponse>,
    val runResults: Map<Long, String>
)

