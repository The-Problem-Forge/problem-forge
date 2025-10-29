package ru.nsu.problem_forge.dto

data class AddProblemToContestRequest(
    val problemId: Long
)

data class ContestProblemDto(
    val contestId: Long,
    val problemId: Long,
    val problemTag: String
)

data class RemoveProblemFromContestRequest(
    val problemId: Long
)