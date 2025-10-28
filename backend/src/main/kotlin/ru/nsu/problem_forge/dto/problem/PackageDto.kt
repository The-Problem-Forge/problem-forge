package ru.nsu.problem_forge.dto.problem

data class ProblemPackageResponse(
    val status: PreviewStatus,
    val message: String? = null,
    val downloadUrl: String? = null
)