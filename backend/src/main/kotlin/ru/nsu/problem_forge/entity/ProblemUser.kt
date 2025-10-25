package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Entity
@Table(name = "problem_users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["problem_id", "user_id"])
])
data class ProblemUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    val problem: Problem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.VIEWER
)