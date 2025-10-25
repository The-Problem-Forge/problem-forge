package ru.nsu.problem_forge.controller.problem

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.StatementDto
import ru.nsu.problem_forge.dto.StatementResponse
import ru.nsu.problem_forge.service.UserService
import ru.nsu.problem_forge.service.problem.ProblemStatementService


@RestController
@RequestMapping("/api/problems/{problemId}/statement")
class ProblemStatementController(
    private val problemStatementService: ProblemStatementService,
    private val userService: UserService
) {

    @GetMapping
    fun getStatement(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<StatementResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val statement = problemStatementService.getStatement(problemId, user.id!!)
        return ResponseEntity.ok(statement)
    }

    @PutMapping
    fun updateStatement(
        @PathVariable problemId: Long,
        @RequestBody statementDto: StatementDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<StatementResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val updatedStatement = problemStatementService.updateStatement(problemId, user.id!!, statementDto)
        return ResponseEntity.ok(updatedStatement)
    }

    @GetMapping("/export/tex")
    fun exportStatementToTex(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val user = userService.findUserByHandle(userDetails.username)
        return problemStatementService.exportStatementToTex(problemId, user.id!!)
    }

    @GetMapping("/export/pdf")
    fun exportStatementToPdf(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ByteArray> {
        val user = userService.findUserByHandle(userDetails.username)
        return problemStatementService.exportStatementToPdf(problemId, user.id!!)
    }

    @GetMapping("/tutorial/export/tex")
    fun exportTutorialToTex(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val user = userService.findUserByHandle(userDetails.username)
        return problemStatementService.exportTutorialToTex(problemId, user.id!!)
    }

    @GetMapping("/tutorial/export/pdf")
    fun exportTutorialToPdf(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ByteArray> {
        val user = userService.findUserByHandle(userDetails.username)
        return problemStatementService.exportTutorialToPdf(problemId, user.id!!)
    }
}