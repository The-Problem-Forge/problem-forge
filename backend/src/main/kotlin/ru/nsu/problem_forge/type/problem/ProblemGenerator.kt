package ru.nsu.problem_forge.type.problem

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ProblemGenerator @JsonCreator constructor(
    @JsonProperty("generatorId")
    val generatorId: Long,

    @JsonProperty("file")
    val file: Long,

    @JsonProperty("alias")
    val alias: String
)
