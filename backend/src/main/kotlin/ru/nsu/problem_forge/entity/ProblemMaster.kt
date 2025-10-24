package ru.nsu.problem_forge.entity


import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.nsu.problem_forge.type.Changelog
import ru.nsu.problem_forge.type.ProblemInfo
import java.time.LocalDateTime

@Entity
@Table(name = "problems_master")
class ProblemMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    var id: Long = 0

    @Column(nullable = false, length = 40)
    var tag: String = ""

    @Column(name = "problem_info", nullable = false, columnDefinition = "JSON")
//    @Convert(converter = ProblemInfoConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    var problemInfo: ProblemInfo = ProblemInfo()

    @Column(name = "changelog", nullable = false, columnDefinition = "JSON")
    //@Convert(converter = ChangelogConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    var changelog: Changelog = Changelog()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var branches: List<ProblemBranch> = emptyList()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var contestProblems: List<ContestProblem> = emptyList()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], orphanRemoval = true)
    var problemUsers: List<ProblemUser> = emptyList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ProblemMaster
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ProblemMaster(id=$id, tag='$tag')"
}