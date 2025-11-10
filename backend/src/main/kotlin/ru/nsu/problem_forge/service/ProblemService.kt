package ru.nsu.problem_forge.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.entity.ContestProblem
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.repository.ContestProblemRepository
import ru.nsu.problem_forge.repository.ContestRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Changelog
import ru.nsu.problem_forge.type.ProblemInfo
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val contestRepository: ContestRepository,
    private val contestProblemRepository: ContestProblemRepository
) {
    private val logger = LoggerFactory.getLogger(ProblemService::class.java)

    fun getProblemsForUser(userId: Long): List<ProblemDto> {
        val problemUsers = problemUserRepository.findByUserId(userId)
        return problemUsers.map { it.problem.toDto() }
    }

    @Transactional
    fun createProblem(request: CreateProblemRequest, user: User): ProblemDto {
        val problem = Problem().apply {
            title = request.title
            problemInfo = ProblemInfo()
            changelog = Changelog()
            createdAt = LocalDateTime.now()
            modifiedAt = LocalDateTime.now()
        }

        val savedProblem = problemRepository.save(problem)

        // Создаем запись о владельце проблемы
        problemUserRepository.save(ProblemUser(
            problem = savedProblem,
            user = user,
            role = Role.OWNER,
            modifiedAt = LocalDateTime.now()
        ))

        // Если указан contestId, добавляем проблему в конкурс
        request.contestId?.let { contestId ->
            logger.info("Adding problem ${savedProblem.id} to contest $contestId")
            val contest = contestRepository.findById(contestId)
                .orElseThrow { IllegalArgumentException("Contest not found with id: $contestId") }

            // Проверяем, что пользователь имеет доступ к конкурсу
            val userRole = contestRepository.findUserRoleInContest(contestId, user.id!!)
            if (userRole == null) {
                logger.warn("User ${user.id} does not have access to contest $contestId")
                throw SecurityException("User does not have access to contest $contestId")
            }
            logger.info("User role in contest: $userRole")

            // Находим максимальный orderIndex для конкурса
            val existingProblems = contestProblemRepository.findByContestId(contestId)
            val maxOrderIndex = existingProblems.maxOfOrNull { it.orderIndex } ?: -1
            logger.info("Max orderIndex for contest $contestId: $maxOrderIndex")

            val contestProblem = ContestProblem().apply {
                this.contest = contest
                this.problem = savedProblem
                orderIndex = maxOrderIndex + 1
            }

            try {
                contestProblemRepository.save(contestProblem)
                logger.info("Successfully added problem ${savedProblem.id} to contest $contestId")
            } catch (e: Exception) {
                logger.error("Failed to save ContestProblem for problem ${savedProblem.id} and contest $contestId", e)
                throw RuntimeException("Failed to add problem to contest: ${e.message}", e)
            }
        }

        return savedProblem.toDto()
    }

    fun getProblem(problemId: Long, userId: Long): ProblemDto {
        val problem = getProblemEntity(problemId)

        // Проверяем доступ пользователя к проблеме
        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("User does not have access to this problem")

        return problem.toDto()
    }

    fun getProblemEntity(problemId: Long): Problem {
        return problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found with id: $problemId") }
    }

    private fun Problem.toDto(): ProblemDto {
        return ProblemDto(
            id = this.id,
            title = this.title,
            createdAt = this.createdAt,
            modifiedAt = this.modifiedAt
        )
    }
}