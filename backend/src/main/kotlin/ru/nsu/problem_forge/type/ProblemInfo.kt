package ru.nsu.problem_forge.type

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.nsu.problem_forge.type.problem.General
import ru.nsu.problem_forge.type.problem.ProblemGenerator
import ru.nsu.problem_forge.type.problem.ProblemSolution
import ru.nsu.problem_forge.type.problem.ProblemTest
import ru.nsu.problem_forge.type.problem.Statement

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProblemInfo(
    val general: General = General(),
    val statement: Statement = Statement(),

    val generators: List<ProblemGenerator> = emptyList(),
    var checker:   Long? = null, // file_id actually
    var checkerLanguage: String? = null, // language for checker source
    var validator: Long? = null, // file_id for validator source
    var validatorLanguage: String? = null, // language for validator source

    val tests:     List<ProblemTest> = emptyList(),
    var solutions: List<ProblemSolution> = emptyList()
)