package ru.nsu.problem_forge.type

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.nsu.problem_forge.type.problem.General
import ru.nsu.problem_forge.type.problem.ProblemSolution
import ru.nsu.problem_forge.type.problem.ProblemTest
import ru.nsu.problem_forge.type.problem.Statement

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProblemInfo(
    val general: General = General(),
    val statement: Statement = Statement(),

    // Currently without attachments
    val attachments: List<Long> = emptyList(), // list of file_id

    val generators: List<Long> = emptyList(),  // list of file_id
    val checker:   Long? = null, // file_id actually

    // Currently without validator
    // val validator: Long? = null, // file_id actually
    val tests:     List<ProblemTest> = emptyList(),
    val solutions: List<ProblemSolution> = emptyList()// path to files with additional info
)