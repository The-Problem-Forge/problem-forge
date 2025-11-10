package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import ru.nsu.problem_forge.type.Role

@Entity
@Table(name = "contest_users", uniqueConstraints = [
    UniqueConstraint(columnNames = ["contest_id", "user_id"])
])
data class ContestUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    val contest: Contest,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.VIEWER
)