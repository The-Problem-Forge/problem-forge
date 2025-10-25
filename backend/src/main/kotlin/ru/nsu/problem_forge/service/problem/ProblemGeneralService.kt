package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import ru.nsu.problem_forge.dto.GeneralDto
import ru.nsu.problem_forge.dto.GeneralResponse
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.problem.General
import java.time.LocalDateTime

@Service
class ProblemGeneralService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository
) {

    fun getGeneral(problemId: Long, userId: Long): GeneralResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        // Check permissions
        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

//        // Check if this is a branch (has parent)
//        if (problem.parentProblemId == null) {
//            throw IllegalArgumentException("Cannot edit main problem branch")
//        }

        val general = problem.problemInfo.general
        return GeneralResponse(
            inputFile = general.inputFile,
            outputFile = general.outputFile,
            timeLimit = general.timeLimit,
            memoryLimit = general.memoryLimit
        )
    }

    fun updateGeneral(problemId: Long, userId: Long, updateDto: GeneralDto): GeneralResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        // Check permissions - Editor+ required
        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

//        // Check if this is a branch (has parent)
//        if (problem.parentProblemId == null) {
//            throw IllegalStateException("Cannot edit main problem branch")
//        }

        // Validate input
        validateGeneral(updateDto)

        // Update general info
        val currentGeneral = problem.problemInfo.general
        val updatedGeneral = General(
            inputFile = updateDto.inputFile ?: currentGeneral.inputFile,
            outputFile = updateDto.outputFile ?: currentGeneral.outputFile,
            timeLimit = updateDto.timeLimit ?: currentGeneral.timeLimit,
            memoryLimit = updateDto.memoryLimit ?: currentGeneral.memoryLimit
        )

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(general = updatedGeneral)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        return GeneralResponse(
            inputFile = updatedGeneral.inputFile,
            outputFile = updatedGeneral.outputFile,
            timeLimit = updatedGeneral.timeLimit,
            memoryLimit = updatedGeneral.memoryLimit
        )
    }

    private fun validateGeneral(dto: GeneralDto) {
        dto.timeLimit?.let {
            require(it > 0) { "Time limit must be positive" }
            require(it <= 20000) { "Time limit must be less or equal to 20 seconds" }
        }

        dto.memoryLimit?.let {
            require(it > 0) { "Memory limit must be positive" }
            require(it <= 2 * 1024) { "Memory limit must be less or equal to 2GB" }
        }
    }
}

