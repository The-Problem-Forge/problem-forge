package ru.nsu.problem_forge.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.repository.UserRepository
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ProblemUserService(
    private val problemUserRepository: ProblemUserRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun addUserToProblem(problemId: Long, request: AddUserToProblemRequest, currentUserId: Long): ProblemUserDto {
        // Проверяем права текущего пользователя (только OWNER может добавлять пользователей)
        val currentUserPermission = problemUserRepository.findByProblemIdAndUserId(problemId, currentUserId)
            ?: throw SecurityException("User does not have access to this problem")

        if (currentUserPermission.role != Role.OWNER) {
            throw SecurityException("Only owner can add users to problem")
        }

        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found with id: $problemId") }

        val userToAdd = userRepository.findById(request.userId)
            .orElseThrow { IllegalArgumentException("User not found with id: ${request.userId}") }

        // Проверяем, не добавлен ли пользователь уже
        val existingProblemUser = problemUserRepository.findByProblemIdAndUserId(problemId, request.userId)
        if (existingProblemUser != null) {
            throw IllegalArgumentException("User is already added to this problem")
        }

        val problemUser = problemUserRepository.save(ProblemUser(
            problem = problem,
            user = userToAdd,
            role = request.role,
            modifiedAt = LocalDateTime.now()
        ))

        return problemUser.toDto()
    }

    @Transactional
    fun removeUserFromProblem(problemId: Long, userId: Long, currentUserId: Long) {
        // Проверяем права текущего пользователя (только OWNER может удалять пользователей)
        val currentUserPermission = problemUserRepository.findByProblemIdAndUserId(problemId, currentUserId)
            ?: throw SecurityException("User does not have access to this problem")

        if (currentUserPermission.role != Role.OWNER) {
            throw SecurityException("Only owner can remove users from problem")
        }

        // Нельзя удалить самого себя (владельца)
        if (userId == currentUserId) {
            throw IllegalArgumentException("Owner cannot remove themselves from problem")
        }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw IllegalArgumentException("User not found in this problem")

        problemUserRepository.delete(problemUser)
    }

    @Transactional
    fun updateUserPermission(problemId: Long, request: UpdateUserPermissionRequest, currentUserId: Long): ProblemUserDto {
        // Проверяем права текущего пользователя (только OWNER может изменять права)
        val currentUserPermission = problemUserRepository.findByProblemIdAndUserId(problemId, currentUserId)
            ?: throw SecurityException("User does not have access to this problem")

        if (currentUserPermission.role != Role.OWNER) {
            throw SecurityException("Only owner can update user permissions")
        }

        // Нельзя изменить свои собственные права
        if (request.userId == currentUserId) {
            throw IllegalArgumentException("Owner cannot change their own role")
        }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, request.userId)
            ?: throw IllegalArgumentException("User not found in this problem")

        problemUser.role = request.role
        val updatedProblemUser = problemUserRepository.save(problemUser)

        return updatedProblemUser.toDto()
    }

    fun getProblemUsers(problemId: Long, currentUserId: Long): List<ProblemUserDto> {
        // Проверяем, что пользователь имеет доступ к проблеме
        val currentUserPermission = problemUserRepository.findByProblemIdAndUserId(problemId, currentUserId)
            ?: throw SecurityException("User does not have access to this problem")

        val problemUsers = problemUserRepository.findByProblemId(problemId)
        return problemUsers.map { it.toDto() }
    }

    private fun ProblemUser.toDto(): ProblemUserDto {
        return ProblemUserDto(
            userId = this.user.id!!,
            userHandle = this.user.handle,
            role = this.role,
            modifiedAt = this.modifiedAt
        )
    }
}