package ru.nsu.problem_forge.type.problem

data class General (
    val inputFile: String = "stdin",
    val outputFile: String = "stdout",
    val timeLimit: Int = 1000, // TODO: validate input from dtos...
    val memoryLimit: Int = 256
)
