package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import ru.nsu.problem_forge.type.problem.FileFormat
import java.time.LocalDateTime

@Entity
@Table(name = "files")
class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    var id: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    var format: FileFormat = FileFormat.TEXT

    @Column(nullable = false, columnDefinition = "bytea")
    var content: ByteArray = byteArrayOf()

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "modified_at", nullable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as File
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "File(id=$id, format=$format)"
}