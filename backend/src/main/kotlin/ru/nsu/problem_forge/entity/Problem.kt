package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.nsu.problem_forge.type.Changelog
import ru.nsu.problem_forge.type.problem.Invocations
import ru.nsu.problem_forge.type.ProblemInfo
import java.time.LocalDateTime

@Entity
@Table(name = "problems")
class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    var id: Long = 0

    @Column(name = "tag", nullable = false, length = 40)
    var title: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "problem_info", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    var problemInfo: ProblemInfo = ProblemInfo()

    @Column(name = "changelog", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    var changelog: Changelog = Changelog()

    @Column(name = "invocations", nullable = true, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    var invocations: Invocations = Invocations()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var contestProblems: List<ContestProblem> = emptyList()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var problemUsers: List<ProblemUser> = emptyList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Problem
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ProblemMaster(id=$id, title='$title')"
}

/*
Features to support branching:

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "parent_problem_id", referencedColumnName = "problem_id")
//    var parentProblem: Problem? = null
OR
//    @JoinColumn(name = "parent_problem_id")
//    var parentProblemId: Long? = null

//    @OneToMany(mappedBy = "parentProblemId", fetch = FetchType.LAZY)
//    var childBranches: List<Problem> = emptyList()

 */
