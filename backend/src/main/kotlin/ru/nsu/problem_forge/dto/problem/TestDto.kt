package ru.nsu.problem_forge.dto.problem

import ru.nsu.problem_forge.type.problem.TestType

data class TestDto(
    val testType: TestType,
    val content: String = "",
    val description: String = "",
    val sample: Boolean = false,
    val points: Int = 1
)

data class TestResponse(
    val testId: Int,  // Derived from position in list
    val testType: TestType,
    val content: String = "",
    val description: String = "",
    val sample: Boolean = false,
    val points: Int = 1
)