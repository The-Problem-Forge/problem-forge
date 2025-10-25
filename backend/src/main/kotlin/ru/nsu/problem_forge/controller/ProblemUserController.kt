package ru.nsu.problem_forge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.service.ProblemUserService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/problems/{problemId}/users")
class ProblemUserController(
    private val problemUserService: ProblemUserService,
    private val userService: UserService
) {

    @PostMapping
    fun addUserToProblem(
        @PathVariable problemId: Long,
        @RequestBody request: AddUserToProblemRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ProblemUserDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val problemUser = problemUserService.addUserToProblem(problemId, request, user.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(problemUser)
    }

    @DeleteMapping("/{userId}")
    fun removeUserFromProblem(
        @PathVariable problemId: Long,
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemUserService.removeUserFromProblem(problemId, userId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/role")
    fun updateUserPermission(
        @PathVariable problemId: Long,
        @RequestBody request: UpdateUserPermissionRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ProblemUserDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val problemUser = problemUserService.updateUserPermission(problemId, request, user.id!!)
        return ResponseEntity.ok(problemUser)
    }

    @GetMapping
    fun getProblemUsers(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<ProblemUserDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val problemUsers = problemUserService.getProblemUsers(problemId, user.id!!)
        return ResponseEntity.ok(problemUsers)
    }
}