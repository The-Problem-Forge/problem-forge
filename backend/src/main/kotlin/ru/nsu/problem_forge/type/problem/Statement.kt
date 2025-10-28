package ru.nsu.problem_forge.type.problem

data class Statement (
    val name: String = "",
    val legend: String = "",
    val inputFormat: String = "",
    val outputFormat: String = "",
    val scoring: String = "",
    val notes: String = "",
    val tutorial: String = ""
)