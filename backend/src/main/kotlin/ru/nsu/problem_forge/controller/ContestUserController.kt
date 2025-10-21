package ru.nsu.problem_forge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.AddUserToContestRequest
import ru.nsu.problem_forge.dto.ContestUserDto
import ru.nsu.problem_forge.dto.UpdateUserPermissionRequest
import ru.nsu.problem_forge.service.ContestUserService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/contests/{contestId}/users")
class ContestUserController(
    private val contestUserService: ContestUserService,
    private val userService: UserService
) {

    @PostMapping
    fun addUserToContest(
        @PathVariable contestId: Long,
        @RequestBody request: AddUserToContestRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ContestUserDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val contestUser = contestUserService.addUserToContest(contestId, request, user.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(contestUser)
    }

    @DeleteMapping("/{userId}")
    fun removeUserFromContest(
        @PathVariable contestId: Long,
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        contestUserService.removeUserFromContest(contestId, userId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/role")
    fun updateUserPermission(
        @PathVariable contestId: Long,
        @RequestBody request: UpdateUserPermissionRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ContestUserDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val contestUser = contestUserService.updateUserPermission(contestId, request, user.id!!)
        return ResponseEntity.ok(contestUser)
    }

    @GetMapping
    fun getContestUsers(
        @PathVariable contestId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<ContestUserDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val contestUsers = contestUserService.getContestUsers(contestId, user.id!!)
        return ResponseEntity.ok(contestUsers)
    }
}