package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.nsu.problem_forge.convert.ChangelogConverter
import ru.nsu.problem_forge.convert.ProblemInfoConverter
import ru.nsu.problem_forge.type.Changelog
import ru.nsu.problem_forge.type.ProblemInfo
import java.time.LocalDateTime

@Entity
@Table(name = "problems_branch")
class ProblemBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    var id: Long = 0

    @Column(name = "problem_info", nullable = false, columnDefinition = "JSONB")
//    @Convert(converter = ProblemInfoConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    var problemInfo: ProblemInfo = ProblemInfo()

    @Column(name = "current_changes", nullable = false, columnDefinition = "JSONB")
    // @Convert(converter = ChangelogConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    var currentChanges: Changelog = Changelog()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: ProblemMaster? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner", nullable = false)
    var owner: User? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProblemBranch
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ProblemBranch(id=$id, problemId=${problem?.id})"
}