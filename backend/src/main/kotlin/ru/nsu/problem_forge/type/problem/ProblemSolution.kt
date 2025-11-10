package ru.nsu.problem_forge.type.problem

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ProblemSolution @JsonCreator constructor(
     @JsonProperty("solutionId")
     val solutionId: Long,

     @JsonProperty("name")
     val name: String,

     @JsonProperty("language")
     val language: String,

     @JsonProperty("author")
     val author: Long,

     @JsonProperty("file")
     val file: Long,

     @JsonProperty("solutionType")
     val solutionType: SolutionType
 )