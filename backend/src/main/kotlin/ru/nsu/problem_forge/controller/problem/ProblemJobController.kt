package ru.nsu.problem_forge.controller.problem

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.problem.*
import ru.nsu.problem_forge.entity.InvocationStatus
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

    @PostMapping("/invocation/subset")
    fun runSolutionsSubset(
        @PathVariable problemId: Long,
        @RequestBody request: CreateInvocationRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<InvocationStatusResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val statusResponse = problemInvocationService.runSolutionsSubset(
            problemId, 
            user.id!!, 
            request.solutionIds, 
            request.testIds
        )
        return ResponseEntity.ok(statusResponse)
    }

    @GetMapping("/invocations")
    fun getInvocations(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<InvocationDto>> {
        val user = userService.findUserByHandle(userDetails.username)
        val invocations = problemInvocationService.getInvocations(problemId, user.id!!)
        return ResponseEntity.ok(invocations)
    }

    @PostMapping("/invocations")
    fun createInvocation(
        @PathVariable problemId: Long,
        @RequestBody request: CreateInvocationRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<InvocationDto> {
        val user = userService.findUserByHandle(userDetails.username)
        val invocation = problemInvocationService.createInvocation(problemId, user.id!!, request)
        return ResponseEntity.ok(invocation)
    }

    @GetMapping("/invocations/{invocationId}/matrix")
    fun getInvocationMatrix(
        @PathVariable problemId: Long,
        @PathVariable invocationId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<InvocationMatrixResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val matrix = problemInvocationService.getInvocationMatrix(problemId, user.id!!, invocationId)
        return ResponseEntity.ok(matrix)
    }
}