package ru.nsu.problem_forge.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.dto.ReorderTasksRequest
import ru.nsu.problem_forge.service.ContestService
import ru.nsu.problem_forge.service.ProblemService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/contests")
class ContestController(
    private val contestService: ContestService,
    private val userService: UserService,
    private val problemService: ProblemService
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

    @GetMapping("/{contestId}/problems")
    fun getProblems(
        @PathVariable contestId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<TaskDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tasks = contestService.getTasksForContest(contestId, user.id!!)
        return ResponseEntity.ok(tasks)
    }

    @PutMapping("/{contestId}/problems/reorder")
    fun reorderProblems(
        @PathVariable contestId: Long,
        @RequestBody request: ReorderTasksRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        contestService.reorderTasks(contestId, user.id!!, request.order)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{contestId}/problems/{problemId}")
    fun addProblemToContest(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        contestService.addProblemToContest(contestId, problemId, user.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{contestId}/problems/{problemId}")
    fun removeProblemFromContest(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        contestService.removeProblemFromContest(contestId, problemId, user.id!!)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{contestId}")
    fun deleteContest(
        @PathVariable contestId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        contestService.deleteContest(contestId, user.id!!)
        return ResponseEntity.ok().build()
    }
}