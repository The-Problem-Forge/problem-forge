package ru.nsu.problem_forge.controller.problem

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.nsu.problem_forge.dto.problem.SolutionDto
import ru.nsu.problem_forge.dto.problem.SolutionResponse
import ru.nsu.problem_forge.dto.problem.UpdateSolutionDto
import ru.nsu.problem_forge.service.ProblemService
import ru.nsu.problem_forge.service.UserService
import ru.nsu.problem_forge.service.problem.ProblemSolutionsService
import ru.nsu.problem_forge.type.problem.FileFormat
import ru.nsu.problem_forge.type.problem.SolutionType
import java.util.*

@RestController
@RequestMapping("/api/problems/{problemId}/solutions")
class ProblemSolutionsController(
  private val problemSolutionsService: ProblemSolutionsService,
  private val problemService: ProblemService,
  private val userService: UserService
) {

  private val logger = LoggerFactory.getLogger(ProblemSolutionsController::class.java)

  @GetMapping
  fun getSolutions(
    @PathVariable problemId: Long,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<List<SolutionResponse>> {
    val user = userService.findUserByHandle(userDetails.username)
    val solutions = problemSolutionsService.getSolutions(problemId, user.id!!)
    return ResponseEntity.ok(solutions)
  }

  @PostMapping
  fun createSolution(
    @PathVariable problemId: Long,
    @RequestParam("file") file: MultipartFile,
    @RequestParam("name") name: String,
    @RequestParam("language") language: String,
    @RequestParam("solutionType") solutionType: String,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<SolutionResponse> {
    try {
      // Validate input parameters
      require(!name.isBlank()) { "Name cannot be blank" }
      require(!language.isBlank()) { "Language cannot be blank" }
      require(!solutionType.isBlank()) { "Solution type cannot be blank" }
      require(!file.isEmpty) { "File cannot be empty" }

      val user = userService.findUserByHandle(userDetails.username)

      // Determine format from language
      val format = when (language.lowercase()) {
        "cpp", "c++" -> FileFormat.CPP_17
        "c" -> FileFormat.C
        "java" -> FileFormat.JAVA_17
        "python" -> FileFormat.PYTHON
        "javascript", "js" -> FileFormat.JSON // Using JSON as placeholder for JS
        else -> FileFormat.TEXT
      }

      // Validate solution type
      val solutionTypeEnum = try {
        SolutionType.fromValue(solutionType)
      } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid solution type: $solutionType")
      }

      val solutionDto = SolutionDto(
        name = name.trim(),
        language = language.trim(),
        file = Base64.getEncoder().encodeToString(file.bytes),
        format = format,
        solutionType = solutionTypeEnum
      )

      val solution = problemSolutionsService.addSolution(problemId, user.id!!, solutionDto)
      return ResponseEntity.status(HttpStatus.CREATED).body(solution)
    } catch (e: SecurityException) {
      // Re-throw security exceptions as-is
      throw e
    } catch (e: IllegalArgumentException) {
      // Re-throw validation exceptions as-is
      throw e
    } catch (e: Exception) {
      // Log unexpected errors
      logger.error("Unexpected error creating solution: ${e.message}", e)
      throw RuntimeException("Failed to create solution: ${e.message}", e)
    }
  }

  @PutMapping("/{solutionId}")
  fun updateSolution(
    @PathVariable problemId: Long,
    @PathVariable solutionId: Long,
    @RequestBody updateDto: UpdateSolutionDto,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<SolutionResponse> {
    try {
      val user = userService.findUserByHandle(userDetails.username)
      val solution = problemSolutionsService.updateSolution(problemId, solutionId, user.id!!, updateDto)
      return ResponseEntity.ok(solution)
    } catch (e: Exception) {
      logger.error("Unexpected error: ${e.message}", e)
      throw e
    }
  }

  @DeleteMapping("/{solutionId}")
  fun deleteSolution(
    @PathVariable problemId: Long,
    @PathVariable solutionId: Long,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<Void> {
    val user = userService.findUserByHandle(userDetails.username)
    problemSolutionsService.deleteSolution(problemId, solutionId, user.id!!)
    return ResponseEntity.noContent().build()
  }

  @GetMapping("/{solutionId}/source")
  fun getSolutionSource(
    @PathVariable problemId: Long,
    @PathVariable solutionId: Long,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<Map<String, String>> {
    val user = userService.findUserByHandle(userDetails.username)
    val solutions = problemSolutionsService.getSolutions(problemId, user.id!!)
    val solution = solutions.find { it.id == solutionId }
      ?: return ResponseEntity.notFound().build()

    return ResponseEntity.ok(
      mapOf(
        "source" to String(Base64.getDecoder().decode(solution.file)),
        "language" to solution.language
      )
    )
  }

  @PostMapping("/{solutionId}/compile")
  fun compileSolution(
    @PathVariable problemId: Long,
    @PathVariable solutionId: Long,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<Map<String, Any>> {
    // TODO: Implement compilation logic
    // For now, return a mock response
    return ResponseEntity.ok(
      mapOf(
        "verdict" to "OK",
        "stdout" to "Compilation successful",
        "stderr" to ""
      )
    )
  }

  @GetMapping("/{solutionId}/download")
  fun downloadSolution(
    @PathVariable problemId: Long,
    @PathVariable solutionId: Long,
    @AuthenticationPrincipal userDetails: UserDetails
  ): ResponseEntity<ByteArray> {
    val user = userService.findUserByHandle(userDetails.username)
    val solutions = problemSolutionsService.getSolutions(problemId, user.id!!)
    val solution = solutions.find { it.id == solutionId }
      ?: return ResponseEntity.notFound().build()

    val fileName = "${solution.name}.${getFileExtension(solution.language)}"
    val content = Base64.getDecoder().decode(solution.file)

    return ResponseEntity.ok()
      .header("Content-Disposition", "attachment; filename=\"$fileName\"")
      .header("Content-Type", "text/plain")
      .body(content)
  }

  private fun getFileExtension(language: String): String {
    return when (language.lowercase()) {
      "cpp", "c++" -> "cpp"
      "c" -> "c"
      "java" -> "java"
      "python" -> "py"
      "javascript", "js" -> "js"
      else -> "txt"
    }
  }
}
