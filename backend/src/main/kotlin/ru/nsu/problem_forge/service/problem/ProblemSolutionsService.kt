package ru.nsu.problem_forge.service.problem

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.SolutionDto
import ru.nsu.problem_forge.dto.problem.UpdateSolutionDto
import ru.nsu.problem_forge.dto.problem.SolutionResponse
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.ProblemSolution
import java.time.LocalDateTime
import java.util.*

@Service
class ProblemSolutionsService(
  private val problemRepository: ProblemRepository,
  private val problemUserRepository: ProblemUserRepository,
  private val fileRepository: FileRepository
) {

  private val logger = LoggerFactory.getLogger(ProblemSolutionsService::class.java)

  fun getSolutions(problemId: Long, userId: Long): List<SolutionResponse> {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    return problem.problemInfo.solutions.map { solution ->
      val file = fileRepository.findById(solution.file)
        .orElseThrow { IllegalArgumentException("File not found for solution ${solution.solutionId}") }

      SolutionResponse(
        id = solution.solutionId,
        name = solution.name,
        language = solution.language,
        author = solution.author,
        file = Base64.getEncoder().encodeToString(file.content),
        format = file.format,
        solutionType = solution.solutionType,
        createdAt = file.createdAt,
        modifiedAt = file.modifiedAt
      )
    }
  }

  @Transactional
  fun addSolution(problemId: Long, userId: Long, solutionDto: SolutionDto): SolutionResponse {
    try {
      val problem = problemRepository.findById(problemId)
        .orElseThrow { IllegalArgumentException("Problem with ID $problemId not found") }

      val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
        ?: throw SecurityException("User $userId has no access to problem $problemId")

      if (problemUser.role < Role.EDITOR) {
        throw SecurityException("Editor role required to add solutions")
      }

      // Create file entity
      val fileContent = try {
        Base64.getDecoder().decode(solutionDto.file)
      } catch (e: Exception) {
        throw IllegalArgumentException("Invalid file content: not valid Base64")
      }

      val file = File().apply {
        format = solutionDto.format
        content = fileContent
        createdAt = LocalDateTime.now()
        modifiedAt = LocalDateTime.now()
      }
      val savedFile = fileRepository.save(file)

      // Generate solution ID (you might want to use a sequence or other method)
      val solutionId = System.currentTimeMillis() // Simple ID generation

      // Create solution
      val solution = ProblemSolution(
        solutionId = solutionId,
        name = solutionDto.name,
        language = solutionDto.language,
        author = userId,
        file = savedFile.id,
        solutionType = solutionDto.solutionType,
      )

      // Update problem info
      val currentSolutions = problem.problemInfo.solutions.toMutableList()
      currentSolutions.add(solution)

      val updatedProblemInfo = problem.problemInfo.copy(solutions = currentSolutions)
      problem.problemInfo = updatedProblemInfo
      problem.modifiedAt = LocalDateTime.now()

      problemRepository.save(problem)

      return SolutionResponse(
        id = solutionId,
        name = solution.name,
        language = solution.language,
        author = solution.author,
        file = solutionDto.file,
        format = solutionDto.format,
        solutionType = solution.solutionType,
        createdAt = savedFile.createdAt,
        modifiedAt = savedFile.modifiedAt
      )
        } catch (e: Exception) {
            logger.error("Error in addSolution service: ${e.message}", e)
            throw e
        }
  }

  @Transactional
  fun updateSolution(problemId: Long, solutionId: Long, userId: Long, updateDto: UpdateSolutionDto): SolutionResponse {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    // Find existing solution
    val currentSolutions = problem.problemInfo.solutions.toMutableList()
    val solutionIndex = currentSolutions.indexOfFirst { it.solutionId == solutionId }
    if (solutionIndex == -1) {
      throw IllegalArgumentException("Solution not found")
    }

    val oldSolution = currentSolutions[solutionIndex]

    // Only update file if file content is provided (not null and not empty)
    val updatedFile = if (updateDto.file != null && updateDto.file.isNotBlank()) {
      val file = fileRepository.findById(oldSolution.file)
        .orElseThrow { IllegalArgumentException("File not found for solution $solutionId") }

      file.apply {
        format = updateDto.format ?: file.format
        content = Base64.getDecoder().decode(updateDto.file)
        modifiedAt = LocalDateTime.now()
      }
      fileRepository.save(file)
    } else {
      // Keep existing file unchanged
      fileRepository.findById(oldSolution.file)
        .orElseThrow { IllegalArgumentException("File not found for solution $solutionId") }
    }

    // Update solution metadata, keeping existing values if not provided
    val updatedSolution = oldSolution.copy(
      name = updateDto.name ?: oldSolution.name,
      language = updateDto.language ?: oldSolution.language,
      solutionType = updateDto.solutionType ?: oldSolution.solutionType
    )
    currentSolutions[solutionIndex] = updatedSolution

    // Update problem info
    val updatedProblemInfo = problem.problemInfo.copy(solutions = currentSolutions)
    problem.problemInfo = updatedProblemInfo
    problem.modifiedAt = LocalDateTime.now()

    problemRepository.save(problem)

    return SolutionResponse(
      id = solutionId,
      name = updatedSolution.name,
      language = updatedSolution.language,
      author = updatedSolution.author,
      file = if (updateDto.file != null && updateDto.file.isNotBlank()) updateDto.file else Base64.getEncoder().encodeToString(updatedFile.content),
      format = if (updateDto.file != null && updateDto.file.isNotBlank()) (updateDto.format ?: updatedFile.format) else updatedFile.format,
      solutionType = updatedSolution.solutionType,
      createdAt = updatedFile.createdAt,
      modifiedAt = updatedFile.modifiedAt
    )
  }

  @Transactional
  fun deleteSolution(problemId: Long, solutionId: Long, userId: Long) {
    val problem = problemRepository.findById(problemId)
      .orElseThrow { IllegalArgumentException("Problem not found") }

    val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
      ?: throw SecurityException("No access to problem")

    if (problemUser.role < Role.EDITOR) {
      throw SecurityException("Editor role required")
    }

    // Find solution
    val currentSolutions = problem.problemInfo.solutions.toMutableList()
    val solution = currentSolutions.find { it.solutionId == solutionId }
      ?: throw IllegalArgumentException("Solution not found")

    // Remove solution
    currentSolutions.removeIf { it.solutionId == solutionId }

    // Update problem info
    val updatedProblemInfo = problem.problemInfo.copy(solutions = currentSolutions)
    problem.problemInfo = updatedProblemInfo
    problem.modifiedAt = LocalDateTime.now()

    problemRepository.save(problem)

    // Delete file
    fileRepository.deleteById(solution.file)
  }
}
