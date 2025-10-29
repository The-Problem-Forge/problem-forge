package ru.nsu.problem_forge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.AddProblemToContestRequest
import ru.nsu.problem_forge.dto.ContestProblemDto
import ru.nsu.problem_forge.dto.RemoveProblemFromContestRequest
import ru.nsu.problem_forge.service.ContestProblemService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/contests/{contestId}/problems")
class ContestProblemController(
    private val contestProblemService: ContestProblemService,
    private val userService: UserService
) {

    @PostMapping
    fun addProblemToContest(
        @PathVariable contestId: Long,
        @RequestBody request: AddProblemToContestRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ContestProblemDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val contestProblem = contestProblemService.addProblemToContest(contestId, request, user.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(contestProblem)
    }

    @DeleteMapping
    fun removeProblemFromContest(
        @PathVariable contestId: Long,
        @RequestBody request: RemoveProblemFromContestRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        contestProblemService.removeProblemFromContest(contestId, request, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getContestProblems(
        @PathVariable contestId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<ContestProblemDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val contestProblems = contestProblemService.getContestProblems(contestId, user.id!!)
        return ResponseEntity.ok(contestProblems)
    }
}