package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "contests")
class Contest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_id")
    var id: Long = 0

    @Column(nullable = false, length = 255)
    var title: String = ""

    @Column(nullable = true, length = 255)
    var description: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "contest", cascade = [CascadeType.ALL], orphanRemoval = true)
    var contestProblems: List<ContestProblem> = emptyList()

    @OneToMany(mappedBy = "contest", cascade = [CascadeType.ALL], orphanRemoval = true)
    var contestUsers: List<ContestUser> = emptyList()
}