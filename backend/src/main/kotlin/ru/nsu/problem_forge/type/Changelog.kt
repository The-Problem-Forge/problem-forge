package ru.nsu.problem_forge.type

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Changelog(
    val entries: List<ChangeEntry> = emptyList()
) {
    data class ChangeEntry(
        val version: String = "",
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val author: String = "",
        val changes: List<Change> = emptyList()
    )

    data class Change(
        val type: ChangeType = ChangeType.MODIFIED,
        val field: String = "",
        val oldValue: String? = null,
        val newValue: String? = null,
        val description: String = ""
    )

    enum class ChangeType {
        CREATED,
        MODIFIED,
        DELETED,
        ADDED
    }
}