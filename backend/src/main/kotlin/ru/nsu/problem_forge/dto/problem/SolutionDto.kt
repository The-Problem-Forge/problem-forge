package ru.nsu.problem_forge.dto.problem

import ru.nsu.problem_forge.type.problem.FileFormat
import ru.nsu.problem_forge.type.problem.SolutionType
import java.time.LocalDateTime

data class SolutionDto(
     val name: String,
     val language: String,
     val file: String, // Base64 encoded file content
     val format: FileFormat,
     val solutionType: SolutionType
 )

data class UpdateSolutionDto(
     val name: String? = null,
     val language: String? = null,
     val file: String? = null, // Base64 encoded file content, optional
     val format: FileFormat? = null,
     val solutionType: SolutionType? = null
 )

data class SolutionResponse (
     val id: Long,
     val name: String,
     val language: String,
     val author: Long,
     val file: String, // Base64 encoded file content
     val format: FileFormat,
     val solutionType: SolutionType,
     val createdAt: LocalDateTime,
     val modifiedAt: LocalDateTime
 )


