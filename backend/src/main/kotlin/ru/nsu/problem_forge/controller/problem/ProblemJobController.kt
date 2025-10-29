package ru.nsu.problem_forge.controller.problem

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.problem.InvocationStatusResponse
import ru.nsu.problem_forge.dto.problem.ProblemPackageResponse
import ru.nsu.problem_forge.dto.problem.TestPreviewResponse
import ru.nsu.problem_forge.service.UserService
import ru.nsu.problem_forge.service.problem.ProblemInvocationService
import ru.nsu.problem_forge.service.problem.ProblemPackageService
import ru.nsu.problem_forge.service.problem.ProblemTestsService


@RestController
@RequestMapping("/api/problems/{problemId}")
class ProblemJobController(
    private val problemTestsService: ProblemTestsService,
    private val problemPackageService: ProblemPackageService,
    private val problemInvocationService: ProblemInvocationService,
    private val userService: UserService
) {

    @GetMapping("/tests/preview")
    fun getTestsPreview(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TestPreviewResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val preview = problemTestsService.getTestsPreview(problemId, user.id!!)
        return ResponseEntity.ok(preview)
    }

    @GetMapping("/package")
    fun getProblemPackage(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ProblemPackageResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val packageResponse = problemPackageService.getProblemPackage(problemId, user.id!!)
        return ResponseEntity.ok(packageResponse)
    }

    @GetMapping("/package/download")
    fun downloadProblemPackage(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        response: HttpServletResponse
    ) {
        val user = userService.findUserByHandle(userDetails.username)
        problemPackageService.downloadProblemPackage(problemId, user.id!!, response)
    }

    @GetMapping("/invocation")
    fun runSolutions(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<InvocationStatusResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val statusResponse = problemInvocationService.runSolutions(problemId, user.id!!)
        return ResponseEntity.ok(statusResponse)
    }
}