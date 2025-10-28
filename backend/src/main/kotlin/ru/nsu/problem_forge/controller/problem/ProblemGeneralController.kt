package ru.nsu.problem_forge.controller.problem

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.GeneralDto
import ru.nsu.problem_forge.dto.GeneralResponse
import ru.nsu.problem_forge.service.problem.ProblemGeneralService
import ru.nsu.problem_forge.service.UserService

@RestController
@RequestMapping("/api/problems/{problemId}/general")
class ProblemGeneralController(
    private val problemGeneralService: ProblemGeneralService,
    private val userService: UserService
) {

    @GetMapping
    fun getGeneral(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GeneralResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val general = problemGeneralService.getGeneral(problemId, user.id!!)
        return ResponseEntity.ok(general)
    }

    @PutMapping
    fun updateGeneral(
        @PathVariable problemId: Long,
        @RequestBody generalDto: GeneralDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GeneralResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val updatedGeneral = problemGeneralService.updateGeneral(problemId, user.id!!, generalDto)
        return ResponseEntity.ok(updatedGeneral)
    }
}

