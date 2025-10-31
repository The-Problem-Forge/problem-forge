package ru.nsu.problem_forge.dto.problem

// DTOs for validator with source and tests
data class ValidatorSourceDto(
    val source: String? = null,
    val language: String? = null
)

data class ValidatorTestDto(
    val input: String,
    val verdict: String = "VALID"
)

data class ValidatorTestResponse(
    val id: Long,
    val input: String,
    val verdict: String
)

data class ValidatorRunResult(
    val testId: Long,
    val verdict: String
)

data class ValidatorFullResponse(
    val source: String,
    val language: String,
    val tests: List<ValidatorTestResponse>,
    val runResults: Map<Long, String>
)