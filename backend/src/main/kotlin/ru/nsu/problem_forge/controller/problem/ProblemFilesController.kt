package ru.nsu.problem_forge.controller.problem

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.problem.CheckerDto
import ru.nsu.problem_forge.dto.problem.CheckerResponse
import ru.nsu.problem_forge.dto.problem.GeneratorDto
import ru.nsu.problem_forge.dto.problem.GeneratorResponse
import ru.nsu.problem_forge.dto.problem.ProblemPackageResponse
import ru.nsu.problem_forge.dto.problem.SolutionDto
import ru.nsu.problem_forge.dto.problem.SolutionResponse
import ru.nsu.problem_forge.dto.problem.TestDto
import ru.nsu.problem_forge.dto.problem.TestPreviewResponse
import ru.nsu.problem_forge.dto.problem.TestResponse
import ru.nsu.problem_forge.service.problem.ProblemCheckerService
import ru.nsu.problem_forge.service.problem.ProblemSolutionsService
import ru.nsu.problem_forge.service.UserService
import ru.nsu.problem_forge.service.problem.ProblemGeneratorService
import ru.nsu.problem_forge.service.problem.ProblemPackageService
import ru.nsu.problem_forge.service.problem.ProblemTestsService

@RestController
@RequestMapping("/api/problems/{problemId}")
class ProblemFilesController(
    private val problemSolutionsService: ProblemSolutionsService,
    private val problemCheckerService: ProblemCheckerService,
    private val problemGeneratorService: ProblemGeneratorService,
    private val problemTestsService: ProblemTestsService,
    private val problemPackageService: ProblemPackageService,
    private val userService: UserService
) {

    // Solutions endpoints
    @GetMapping("/solutions")
    fun getSolutions(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<SolutionResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val solutions = problemSolutionsService.getSolutions(problemId, user.id!!)
        return ResponseEntity.ok(solutions)
    }

    @PostMapping("/solutions")
    fun addSolution(
        @PathVariable problemId: Long,
        @RequestBody solutionDto: SolutionDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<SolutionResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val solution = problemSolutionsService.addSolution(problemId, user.id!!, solutionDto)
        return ResponseEntity.ok(solution)
    }

    @PutMapping("/solutions/{solutionId}")
    fun updateSolution(
        @PathVariable problemId: Long,
        @PathVariable solutionId: Long,
        @RequestBody solutionDto: SolutionDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<SolutionResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val solution = problemSolutionsService.updateSolution(problemId, solutionId, user.id!!, solutionDto)
        return ResponseEntity.ok(solution)
    }

    @DeleteMapping("/solutions/{solutionId}")
    fun deleteSolution(
        @PathVariable problemId: Long,
        @PathVariable solutionId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemSolutionsService.deleteSolution(problemId, solutionId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    // Checker endpoints
    @GetMapping("/checker")
    fun getChecker(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val checker = problemCheckerService.getChecker(problemId, user.id!!)
        return ResponseEntity.ok(checker)
    }

    @PutMapping("/checker")
    fun setChecker(
        @PathVariable problemId: Long,
        @RequestBody checkerDto: CheckerDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val checker = problemCheckerService.setChecker(problemId, user.id!!, checkerDto)
        return ResponseEntity.ok(checker)
    }

    @DeleteMapping("/checker")
    fun removeChecker(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemCheckerService.removeChecker(problemId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    // Generator endpoints (NEW)
    @GetMapping("/generators")
    fun getGenerators(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<GeneratorResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val generators = problemGeneratorService.getGenerators(problemId, user.id!!)
        return ResponseEntity.ok(generators)
    }

    @PostMapping("/generators")
    fun addGenerator(
        @PathVariable problemId: Long,
        @RequestBody generatorDto: GeneratorDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GeneratorResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val generator = problemGeneratorService.addGenerator(problemId, user.id!!, generatorDto)
        return ResponseEntity.ok(generator)
    }

    @PutMapping("/generators/{generatorId}")
    fun updateGenerator(
        @PathVariable problemId: Long,
        @PathVariable generatorId: Long,
        @RequestBody generatorDto: GeneratorDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GeneratorResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val generator = problemGeneratorService.updateGenerator(problemId, generatorId, user.id!!, generatorDto)
        return ResponseEntity.ok(generator)
    }

    @DeleteMapping("/generators/{generatorId}")
    fun deleteGenerator(
        @PathVariable problemId: Long,
        @PathVariable generatorId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemGeneratorService.deleteGenerator(problemId, generatorId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    // Test endpoints (NEW)
    @GetMapping("/tests")
    fun getTests(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<TestResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tests = problemTestsService.getTests(problemId, user.id!!)
        return ResponseEntity.ok(tests)
    }

    @PostMapping("/tests")
    fun addTest(
        @PathVariable problemId: Long,
        @RequestBody testDto: TestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemTestsService.addTest(problemId, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @PutMapping("/tests/{testNumber}")
    fun updateTest(
        @PathVariable problemId: Long,
        @PathVariable testNumber: Int,
        @RequestBody testDto: TestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemTestsService.updateTest(problemId, testNumber, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/tests/{testNumber}")
    fun deleteTest(
        @PathVariable problemId: Long,
        @PathVariable testNumber: Int,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemTestsService.deleteTest(problemId, testNumber, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/tests/reorder")
    fun reorderTests(
        @PathVariable problemId: Long,
        @RequestBody newOrder: List<Int>, // List of test numbers in new order
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<TestResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tests = problemTestsService.reorderTests(problemId, user.id!!, newOrder)
        return ResponseEntity.ok(tests)
    }

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
}