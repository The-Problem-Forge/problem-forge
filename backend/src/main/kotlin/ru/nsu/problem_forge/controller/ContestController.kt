package ru.nsu.problem_forge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.service.ContestService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/contests")
class ContestController(
    private val contestService: ContestService,
    private val userService: UserService
) {

    @GetMapping
    fun getContests(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<ContestDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val contests = contestService.getContestsForUser(user.id!!)
        return ResponseEntity.ok(contests)
    }

    @PostMapping
    fun createContest(
        @RequestBody request: CreateContestRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ContestDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val contest = contestService.createContest(request, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(contest)
    }

    @GetMapping("/{contestId}")
    fun getContest(
        @PathVariable contestId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ContestDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val contest = contestService.getContest(contestId, user.id!!)
        return ResponseEntity.ok(contest)
    }

    @PutMapping("/{contestId}")
    fun updateContest(
        @PathVariable contestId: Long,
        @RequestBody request: UpdateContestRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ContestDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val contest = contestService.updateContest(contestId, request, user.id!!)
        return ResponseEntity.ok(contest)
    }

//    @DeleteMapping("/{contestId}")
//    fun deleteContest(
//        @PathVariable contestId: Long,
//        @AuthenticationPrincipal userDetails: UserDetails
//    ): ResponseEntity<Void> {
//        val user = userService.findUserByHandle(userDetails.username)
//        contestService.deleteContest(contestId, user.id!!)
//        return ResponseEntity.noContent().build()
//    }
}