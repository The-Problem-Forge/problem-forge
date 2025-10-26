package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.CheckerDto
import ru.nsu.problem_forge.dto.problem.CheckerResponse
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.problem.FileFormat
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime
import java.util.*

@Service
class ProblemCheckerService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val fileRepository: FileRepository
) {

    fun getChecker(problemId: Long, userId: Long): CheckerResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val checkerId = problem.problemInfo.checker ?: return CheckerResponse("", FileFormat.FILE_NOT_FOUND)

        val file = fileRepository.findById(checkerId)
            .orElseThrow { IllegalArgumentException("Checker file not found") }

        return CheckerResponse(
            file = Base64.getEncoder().encodeToString(file.content),
            format = file.format
        )
    }

    @Transactional
    fun setChecker(problemId: Long, userId: Long, checkerDto: CheckerDto): CheckerResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        // Create or update file
        val file = if (problem.problemInfo.checker != null) {
            val existingFile = fileRepository.findById(problem.problemInfo.checker!!)
                .orElseThrow { IllegalArgumentException("Checker file not found") }

            existingFile.apply {
                format = checkerDto.format
                content = Base64.getDecoder().decode(checkerDto.file)
                modifiedAt = LocalDateTime.now()
            }
        } else {
            File().apply {
                format = checkerDto.format
                content = Base64.getDecoder().decode(checkerDto.file)
                createdAt = LocalDateTime.now()
                modifiedAt = LocalDateTime.now()
            }
        }

        val savedFile = fileRepository.save(file)

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(checker = savedFile.id)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        return CheckerResponse(
            file = checkerDto.file,
            format = checkerDto.format
        )
    }

    @Transactional
    fun removeChecker(problemId: Long, userId: Long) {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val checkerId = problem.problemInfo.checker ?: return

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(checker = null)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        // Delete file
        fileRepository.deleteById(checkerId)
    }
}