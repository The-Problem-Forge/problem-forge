package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "validator_tests")
data class ValidatorTest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    val id: Long? = null,

    @Column(name = "problem_id", nullable = false)
    val problemId: Long,

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    val input: String,

    @Column(name = "verdict", nullable = false)
    val verdict: String = "VALID",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "modified_at", nullable = false)
    val modifiedAt: LocalDateTime = LocalDateTime.now()
)