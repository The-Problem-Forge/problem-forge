package ru.nsu.problem_forge.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.entity.ProblemMaster
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.repository.ProblemMasterRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Changelog
import ru.nsu.problem_forge.type.ProblemInfo
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ProblemService(
    private val problemMasterRepository: ProblemMasterRepository,
    private val problemUserRepository: ProblemUserRepository
) {

    fun getProblemsForUser(userId: Long): List<ProblemDto> {
        val problemUsers = problemUserRepository.findByUserId(userId)
        return problemUsers.map { it.problem.toDto() }
    }

    @Transactional
    fun createProblem(request: CreateProblemRequest, user: User): ProblemDto {
        val problem = ProblemMaster().apply {
            tag = request.tag
            problemInfo = ProblemInfo()
            changelog = Changelog()
            createdAt = LocalDateTime.now()
            modifiedAt = LocalDateTime.now()
        }

        val savedProblem = problemMasterRepository.save(problem)

        // Создаем запись о владельце проблемы
        problemUserRepository.save(ProblemUser(
            problem = savedProblem,
            user = user,
            role = Role.OWNER,
            modifiedAt = LocalDateTime.now()
        ))

        return savedProblem.toDto()
    }

    fun getProblem(problemId: Long, userId: Long): ProblemDto {
        val problem = problemMasterRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found with id: $problemId") }

        // Проверяем доступ пользователя к проблеме
        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("User does not have access to this problem")

        return problem.toDto()
    }

    private fun ProblemMaster.toDto(): ProblemDto {
        return ProblemDto(
            id = this.id,
            tag = this.tag,
            createdAt = this.createdAt,
            modifiedAt = this.modifiedAt
        )
    }
}