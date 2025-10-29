package ru.nsu.problem_forge.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.AddProblemToContestRequest
import ru.nsu.problem_forge.dto.ContestProblemDto
import ru.nsu.problem_forge.dto.RemoveProblemFromContestRequest
import ru.nsu.problem_forge.entity.ContestProblem
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.repository.ContestProblemRepository
import ru.nsu.problem_forge.repository.ContestRepository
import ru.nsu.problem_forge.repository.ContestUserRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ContestProblemService(
    private val contestProblemRepository: ContestProblemRepository,
    private val contestRepository: ContestRepository,
    private val problemRepository: ProblemRepository,
    private val contestUserRepository: ContestUserRepository,
    private val problemUserRepository: ProblemUserRepository
) {

    @Transactional
    fun addProblemToContest(
        contestId: Long,
        request: AddProblemToContestRequest,
        currentUserId: Long
    ): ContestProblemDto {
        // Validate contest exists and user has access
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        if (currentUserPermission.role == Role.VIEWER) {
            throw SecurityException("Only owners and editors can add problems to contest")
        }

        // Validate problem exists and user has access
        val problem = problemRepository.findById(request.problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUserPermission = problemUserRepository.findByProblemIdAndUserId(request.problemId, currentUserId)
            ?: throw SecurityException("You don't have access to this problem")

        if (problemUserPermission.role == Role.VIEWER) {
            throw SecurityException("Only problem owners and editors can add problems to contest")
        }

        // Check if problem is already in contest
        if (contestProblemRepository.existsByContestIdAndProblemId(contestId, request.problemId)) {
            throw IllegalArgumentException("Problem is already in this contest")
        }

        // Create contest-problem relationship
        val contestProblem = ContestProblem().apply {
            this.contest = contest
            this.problem = problem
        }

        val savedContestProblem = contestProblemRepository.save(contestProblem)

        // Give VIEWER permissions to all contest users for this problem
        grantProblemAccessToContestUsers(contestId, request.problemId)

        return ContestProblemDto(
            contestId = contestId,
            problemId = request.problemId,
            problemTag = problem.tag
        )
    }

    @Transactional
    fun removeProblemFromContest(
        contestId: Long,
        request: RemoveProblemFromContestRequest,
        currentUserId: Long
    ) {
        // Validate contest exists and user has access
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        if (currentUserPermission.role == Role.VIEWER) {
            throw SecurityException("Only owners and editors can remove problems from contest")
        }

        // Validate problem exists in contest
        val contestProblem = contestProblemRepository.findByContestIdAndProblemId(contestId, request.problemId)
            ?: throw IllegalArgumentException("Problem not found in contest")

        // Remove contest-problem relationship
        contestProblemRepository.delete(contestProblem)

        // Remove problem access from contest users (only if they don't have other access)
        revokeProblemAccessFromContestUsers(contestId, request.problemId)
    }

    fun getContestProblems(contestId: Long, currentUserId: Long): List<ContestProblemDto> {
        // Validate contest exists and user has access
        contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        val contestProblems = contestProblemRepository.findAllByContestId(contestId)

        return contestProblems.map { contestProblem ->
            ContestProblemDto(
                contestId = contestId,
                problemId = contestProblem.problem!!.id,
                problemTag = contestProblem.problem!!.tag
            )
        }
    }

    private fun grantProblemAccessToContestUsers(contestId: Long, problemId: Long) {
        val contestUsers = contestUserRepository.findAllByContestId(contestId)
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        contestUsers.forEach { contestUser ->
            // Check if user already has access to the problem
            val existingAccess = problemUserRepository.findByProblemIdAndUserId(problemId, contestUser.user.id!!)
            if (existingAccess == null) {
                // Grant VIEWER permission
                val problemUser = ProblemUser(
                    problem = problem,
                    user = contestUser.user,
                    role = Role.VIEWER,
                    modifiedAt = LocalDateTime.now()
                )
                problemUserRepository.save(problemUser)
            }
        }
    }

    private fun revokeProblemAccessFromContestUsers(contestId: Long, problemId: Long) {
        val contestUsers = contestUserRepository.findAllByContestId(contestId)

        contestUsers.forEach { contestUser ->
            val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, contestUser.user.id!!)
            // Only remove access if it was granted through contest (VIEWER role)
            // and user doesn't have other access to the problem
            if (problemUser != null && problemUser.role == Role.VIEWER) {
                // Check if user has other access to this problem (through other contests or direct access)
                val otherContestAccess = contestProblemRepository.existsByProblemIdAndUserIdExcludingContest(
                    problemId, contestUser.user.id, contestId
                )
                if (!otherContestAccess) {
                    problemUserRepository.delete(problemUser)
                }
            }
        }
    }
}