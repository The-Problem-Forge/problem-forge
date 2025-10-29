package ru.nsu.problem_forge.dto.problem

data class ProblemPackageResponse(
    val status: JobStatus,
    val message: String? = null,
    val downloadUrl: String? = null
)