package ru.nsu.problem_forge.entity

import jakarta.persistence.*

@Entity
@Table(name = "contest_problems")
class ContestProblem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    var contest: Contest? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem? = null

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 0
}