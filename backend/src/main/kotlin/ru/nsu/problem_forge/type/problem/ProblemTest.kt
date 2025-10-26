package ru.nsu.problem_forge.type.problem

data class ProblemTest(
    val number: Int,
    val testType: TestType,

    // Contains plain test data if TestType::RAW
    // Contains generator string if TestType::Generated
    val content: String = "",

    val description: String = "",
    val sample: Boolean = false,
    val points: Int = 1
)
