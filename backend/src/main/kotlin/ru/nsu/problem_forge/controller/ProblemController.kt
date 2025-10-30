package ru.nsu.problem_forge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.service.ContestService
import ru.nsu.problem_forge.service.ProblemService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/problems")
class ProblemController(
    private val problemService: ProblemService,
    private val contestService: ContestService,
    private val userService: UserService
) {

    @GetMapping
    fun getProblems(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<ProblemDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val problems = problemService.getProblemsForUser(user.id!!)
        return ResponseEntity.ok(problems)
    }

    @PostMapping
    fun createProblem(
        @RequestBody request: CreateProblemRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ProblemDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val problem = problemService.createProblem(request, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(problem)
    }

    @GetMapping("/{problemId}")
    fun getProblem(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ProblemDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val problem = problemService.getProblem(problemId, user.id!!)
        return ResponseEntity.ok(problem)
    }

    @GetMapping("/{problemId}/contests")
    fun getContestsForProblem(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<ContestDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val contests = contestService.getContestsForProblem(problemId, user.id!!)
        return ResponseEntity.ok(contests)
    }
}