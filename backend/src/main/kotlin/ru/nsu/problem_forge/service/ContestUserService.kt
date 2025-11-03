package ru.nsu.problem_forge.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.AddUserToContestRequest
import ru.nsu.problem_forge.dto.ContestUserDto
import ru.nsu.problem_forge.dto.UpdateUserPermissionRequest
import ru.nsu.problem_forge.entity.ContestUser
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.repository.ContestProblemRepository
import ru.nsu.problem_forge.repository.ContestRepository
import ru.nsu.problem_forge.repository.ContestUserRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.repository.UserRepository
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ContestUserService(
    private val contestRepository: ContestRepository,
    private val userRepository: UserRepository,
    private val contestUserRepository: ContestUserRepository,
    private val contestProblemRepository: ContestProblemRepository,
    private val problemUserRepository: ProblemUserRepository
) {

    @Transactional
    fun addUserToContest(contestId: Long, request: AddUserToContestRequest, currentUserId: Long): ContestUserDto {
        if (request.role == Role.OWNER) {
            throw SecurityException("Contest can have only one OWNER role")
        }

        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        val userToAdd = userRepository.findByHandle(request.handle)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Check if current user has permission to add users
        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        if (currentUserPermission.role == Role.VIEWER) {
            throw SecurityException("Only owners and editors can add users")
        }

        // Check if user is already in contest
        if (contestUserRepository.existsByContestIdAndUserId(contestId, userToAdd.id!!)) {
            throw IllegalArgumentException("User is already in this contest")
        }

        val contestUser = ContestUser(
            contest = contest,
            user = userToAdd,
            role = request.role
        )

        val saved = contestUserRepository.save(contestUser)

        // Give VIEWER permissions to all contest problems for the new user
        grantContestProblemsAccessToUser(contestId, userToAdd.id)

        return toDto(saved)
    }

    private fun grantContestProblemsAccessToUser(contestId: Long, userId: Long) {
        val contestProblems = contestProblemRepository.findByContestId(contestId)

        contestProblems.forEach { contestProblem ->
            val problemId = contestProblem.problem!!.id
            // Check if user already has access to the problem
            val existingAccess = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            if (existingAccess == null) {
                // Grant VIEWER permission
                val problemUser = ProblemUser(
                    problem = contestProblem.problem!!,
                    user = userRepository.findById(userId).get(), // We know user exists
                    role = Role.VIEWER,
                    modifiedAt = LocalDateTime.now()
                )
                problemUserRepository.save(problemUser)
            }
        }
    }

    @Transactional
    fun removeUserFromContest(contestId: Long, userId: Long, currentUserId: Long) {
        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        // Check if current user has permission to remove users
        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        if (contestUserRepository.findByContestIdAndUserId(contestId, userId) == null) {
            throw SecurityException("User not found in contest")
        }

        if (currentUserPermission.role == Role.VIEWER) {
            throw SecurityException("Only owners and editors can remove users")
        }

        // Prevent owners from removing themselves
        if (currentUserId == userId && currentUserPermission.role == Role.OWNER) {
            throw IllegalArgumentException("Owner cannot remove themselves from the contest")
        }

        contestUserRepository.deleteByContestIdAndUserId(contestId, userId)
    }

    @Transactional
    fun updateUserPermission(contestId: Long, request: UpdateUserPermissionRequest, currentUserId: Long): ContestUserDto {
        if (request.role == Role.OWNER) {
            throw SecurityException("Contest can have only one OWNER role")
        }

        val contest = contestRepository.findById(contestId)
            .orElseThrow { IllegalArgumentException("Contest not found") }

        // Check if current user has permission to update permissions
        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        if (currentUserPermission.role == Role.VIEWER) {
            throw SecurityException("Viewers can't can change permissions")
        }

        val contestUser = contestUserRepository.findByContestIdAndUserId(contestId, request.userId)
            ?: throw IllegalArgumentException("User not found in contest")

        // Prevent changing owner's permission
        if (contestUser.role  == Role.OWNER) {
            throw IllegalArgumentException("Cannot change owner's permission")
        }

        contestUser.role = request.role
        val updatedContestUser = contestUserRepository.save(contestUser)
        return toDto(updatedContestUser)
    }

    fun getContestUsers(contestId: Long, currentUserId: Long): List<ContestUserDto> {
        // Check if current user has access to the contest
        val currentUserPermission = contestUserRepository.findByContestIdAndUserId(contestId, currentUserId)
            ?: throw SecurityException("You don't have access to this contest")

        val contestUsers = contestUserRepository.findAllByContestId(contestId)
        return contestUsers.map { toDto(it) }
    }

    private fun toDto(contestUser: ContestUser): ContestUserDto {
        return ContestUserDto(
            userId = contestUser.user.id!!,
            userHandle = contestUser.user.handle,
            userEmail = contestUser.user.email,
            role = contestUser.role
        )
    }
}