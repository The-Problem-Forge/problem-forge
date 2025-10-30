package ru.nsu.problem_forge.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.entity.Contest
import ru.nsu.problem_forge.entity.ContestUser
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.repository.ContestRepository
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ContestService(
    private val contestRepository: ContestRepository
) {

    fun getContestsForUser(userId: Long): List<ContestDto> {
        return contestRepository.findContestsByUserId(userId).map { contest ->
            val role = contestRepository.findUserRoleInContest(contest.id, userId) ?: "VIEWER"
            ContestDto(
                id = contest.id,
                name = contest.title,
                description = contest.description,
                role = role,
                createdAt = contest.createdAt,
                updatedAt = contest.modifiedAt
            )
        }
    }

    @Transactional
    fun createContest(request: CreateContestRequest, owner: User): ContestDto {
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Contest name is required")
        }

        val contest = Contest().apply {
            title = request.name
            request.description?.let { desc ->
                description = desc
            }
            createdAt = LocalDateTime.now()
            modifiedAt = LocalDateTime.now()
        }

        val savedContest = contestRepository.save(contest)

        // Add owner as contest member
        val contestUser = ContestUser(
            contest = contest,
            user = owner,
            role = Role.OWNER
        )
        savedContest.contestUsers = listOf(contestUser)

        return ContestDto(
            id = savedContest.id,
            name = savedContest.title,
            description = savedContest.description,
            role = Role.OWNER.toString(),
            createdAt = savedContest.createdAt,
            updatedAt = savedContest.modifiedAt
        )
    }

    fun getContest(contestId: Long, userId: Long): ContestDto {
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val role = contestRepository.findUserRoleInContest(contestId, userId)
            ?: throw SecurityException("No access to contest")

        return ContestDto(
            id = contest.id,
            name = contest.title,
            description = contest.description,
            role = role,
            createdAt = contest.createdAt,
            updatedAt = contest.modifiedAt
        )
    }

    @Transactional
    fun updateContest(contestId: Long, request: UpdateContestRequest, userId: Long): ContestDto {
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val role = contestRepository.findUserRoleInContest(contestId, userId)
            ?: throw SecurityException("No access to contest")

        if (role != "OWNER" && role != "EDITOR") {
            throw SecurityException("Insufficient permissions")
        }

        request.name?.takeIf { it.isNotBlank() }?.let { contest.title = it }
        request.description?.takeIf { it.isNotBlank() }?.let { contest.description = it }
        contest.modifiedAt = LocalDateTime.now()

        val updatedContest = contestRepository.save(contest)

        return ContestDto(
            id = updatedContest.id,
            name = updatedContest.title,
            description = updatedContest.description,
            role = role,
            createdAt = updatedContest.createdAt,
            updatedAt = updatedContest.modifiedAt
        )
    }

    @Transactional
    fun deleteContest(contestId: Long, userId: Long) {
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val role = contestRepository.findUserRoleInContest(contestId, userId)
            ?: throw SecurityException("No access to contest")

        if (role != "OWNER") {
            throw SecurityException("Only owner can delete contest")
        }

        contestRepository.delete(contest)
    }

    fun getTasksForContest(contestId: Long, userId: Long): List<TaskDto> {
        // Check access
        val role = contestRepository.findUserRoleInContest(contestId, userId)
            ?: throw SecurityException("No access to contest")

        val contestProblems = contestRepository.findContestProblemsByContestId(contestId)

        return contestProblems.map { cp ->
            val problem = cp.problem!!
            TaskDto(
                id = problem.id,
                title = problem.title,
                description = problem.problemInfo.statement.legend.takeIf { it.isNotBlank() },
                orderIndex = cp.orderIndex,
                createdAt = problem.createdAt,
                updatedAt = problem.modifiedAt
            )
        }
    }
}