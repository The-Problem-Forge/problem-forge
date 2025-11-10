package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "invocations")
class Invocation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "problem_id", nullable = false)
    val problemId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: InvocationStatus,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(name = "solution_ids", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = ListLongConverter::class)
    val solutionIds: List<Long>,

    @Column(name = "test_ids", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = ListLongConverter::class)
    val testIds: List<Long>,

    @Column(name = "results", columnDefinition = "TEXT")
    @Convert(converter = InvocationResultsConverter::class)
    var results: Map<Long, List<InvocationTestResult>>? = null,

    @Column(name = "error_message")
    var errorMessage: String? = null
) {
    // Constructor for creating new invocations
    constructor(
        problemId: Long,
        userId: Long,
        solutionIds: List<Long>,
        testIds: List<Long>
    ) : this(
        id = 0,
        problemId = problemId,
        userId = userId,
        status = InvocationStatus.PENDING,
        createdAt = LocalDateTime.now(),
        completedAt = null,
        solutionIds = solutionIds,
        testIds = testIds,
        results = null,
        errorMessage = null
    )
}

enum class InvocationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ERROR
}

data class InvocationTestResult(
    val testNumber: Int,
    val verdict: String,
    val description: String,
    val timeMs: Int,
    val memoryKb: Int
)