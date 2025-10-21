package ru.nsu.problem_forge.entity

import jakarta.persistence.*
import ru.nsu.problem_forge.type.FileFormat
import ru.nsu.problem_forge.type.FileType
import java.time.LocalDateTime

@Entity
@Table(name = "files")
class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    var id: Long = 0

    @Column(nullable = false, length = 255)
    var checksum: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    var format: FileFormat = FileFormat.TEXT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    var type: FileType = FileType.ATTACHMENT

    @Column(nullable = false)
    @Lob
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

    override fun toString(): String = "File(id=$id, type=$type, format=$format)"
}