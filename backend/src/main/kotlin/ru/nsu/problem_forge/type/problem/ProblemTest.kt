package ru.nsu.problem_forge.type.problem

// TODO: add groups, points etc...
data class ProblemTest(
    val number: Int,
    val testType: TestType,

    // Contains plain test data if TestType::RAW
    // Contains generator string if TestType::Generated
    // Contains fileId string if TestType::FromFile
    val content: String = "",

    val description: String = "",
    val sample: Boolean = false,
    val points: Int,
    val group: Int,
)
