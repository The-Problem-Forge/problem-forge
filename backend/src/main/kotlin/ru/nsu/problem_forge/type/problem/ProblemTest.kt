package ru.nsu.problem_forge.type.problem

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ProblemTest @JsonCreator constructor(
    @JsonProperty("testType")
    val testType: TestType,

    @JsonProperty("content")
    val content: String = "",

    @JsonProperty("description")
    val description: String = "",

    @JsonProperty("sample")
    val sample: Boolean = false,

    @JsonProperty("points")
    val points: Int = 1
)