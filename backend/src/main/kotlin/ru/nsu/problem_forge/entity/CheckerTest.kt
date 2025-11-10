package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "checker_tests")
data class CheckerTest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    val id: Long? = null,

    @Column(name = "problem_id", nullable = false)
    val problemId: Long,

    @Column(name = "input", nullable = false, columnDefinition = "TEXT")
    val input: String,

    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    val output: String,

    @Column(name = "expected", nullable = false, columnDefinition = "TEXT")
    val expected: String,

    @Column(name = "verdict", nullable = false)
    val verdict: String = "OK",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "modified_at", nullable = false)
    val modifiedAt: LocalDateTime = LocalDateTime.now()
)