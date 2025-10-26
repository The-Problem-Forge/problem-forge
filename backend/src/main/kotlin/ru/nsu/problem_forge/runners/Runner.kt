package ru.nsu.problem_forge.runners

interface Runner {

    data class RunInput (
        val testlibNeeded: Boolean = false,
        val inputContent: String
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

    fun run(programSource: String, runs: List<RunInput>) : List<RunOutput>
}