package ru.nsu.problem_forge.type

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProblemInfo(
    val title: String = "",
    val statement: String = "",
    val inputFormat: String = "",
    val outputFormat: String = "",
    val constraints: String = "",
    val examples: List<Example> = emptyList(),
    val timeLimit: Int = 1000,
    val memoryLimit: Int = 256,
    val tags: List<String> = emptyList(),
    val difficulty: String = "EASY"
) {
    data class Example(
        val input: String = "",
        val output: String = "",
        val explanation: String? = null
    )
}