package ru.nsu.problem_forge.service.nsuts.dto

data class NsutsLoginRequest(
    val email: String,
    val password: String,
    val method: String = "internal"
)

data class NsutsEnterRequest(
    val olympiad: String
)

data class NsutsTaskListResponse(
    val tasks: List<NsutsTask>
)

// Update NsutsTask to be a data class with copy method
data class NsutsTask(
    val id: Long,
    val tourId: Long,
    val title: String,
    val model: String,
    val inputFile: String,
    val outputFile: String,
    val timeLimit: Long,
    val memoryLimit: Long,
    val jvmTimeLimit: Long,
    val jvmMemoryLimit: Long,
    val position: Long,
    val testsMd5: String?,
    val rateTotalLimit: Int?,
    val rateTimeInterval: Int?,
    val rateSubmitsInTime: Int?
)


// SubmissionResponse.kt
data class SubmissionResponse(
    val submissions: List<Submission>,
    val statistics: Statistics
)

data class Submission(
    val id: String,
    val teamId: String,
    val smtime: String,
    val status: String,
    val res: String?, // Make this nullable
    val host: String,
    val checker_output: String?,
    val time_and_memory: String?, // This can also be null
    val points: String?,
    val priority: String,
    val total: String?,
    val langName: String,
    val teamName: String,
    val taskName: String,
    val taskId: String,
    val taskModel: String,
    val testNumber: String?
)

data class Statistics(
    val filtered: Int,
    val enqueued: String,
    val testing: String,
    val total: String
)