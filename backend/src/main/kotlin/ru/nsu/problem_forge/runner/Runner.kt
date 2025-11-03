package ru.nsu.problem_forge.runner

interface Runner {

    data class RunInput (
        val args: List<String> = emptyList(),
        val inputContent: String,
        val timeLimit: Long = 5000,
        val memoryLimit: Long = 256
    )

    enum class RunStatus {
        COMPILE_ERROR,
        RUNTIME_ERROR,
        SUCCESS;
    }

    data class RunOutput (
        val status: RunStatus,
        val outputContent: String
    )

    fun run(programSource: String, runs: List<RunInput>, testlibNeeded: Boolean = false) : List<RunOutput>

    fun compileToBinary(programSource: String, testlibNeeded: Boolean = false): ByteArray?

    fun runCheckerTest(checkerSource: String, language: String, input: String, output: String, expected: String): String

    fun runValidatorTest(validatorSource: String, language: String, input: String): String
}