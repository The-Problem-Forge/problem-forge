package ru.nsu.problem_forge.controller.problem

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.service.ContestService
import ru.nsu.problem_forge.service.ProblemService
import ru.nsu.problem_forge.service.export.LatexTemplateEngine
import ru.nsu.problem_forge.service.problem.LatexCompilationService
import ru.nsu.problem_forge.service.problem.LatexCompilationException
import ru.nsu.problem_forge.service.problem.ProblemTestsService

/**
 * Controller for exporting problem statements to LaTeX and PDF formats
 */
@RestController
@RequestMapping("/api/problems/{problemId}/statement/export")
class ProblemExportController(
    private val problemService: ProblemService,
    private val latexTemplateEngine: LatexTemplateEngine,
    private val latexCompilationService: LatexCompilationService,
    private val problemTestsService: ProblemTestsService,
    private val contestService: ContestService
) {
    
    private val logger = LoggerFactory.getLogger(ProblemExportController::class.java)
    
    /**
     * Exports problem statement as LaTeX source file
     * @param problemId - ID of the problem to export
     * @param user - Authenticated user
     * @return - LaTeX file as downloadable resource
     */
    @GetMapping("/tex")
    fun exportTex(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ByteArrayResource> {
        return try {
            logger.info("Exporting LaTeX for problemId: $problemId, user: ${user.username}")
            
            problemService.getProblem(problemId, user.id!!)
            val problem = problemService.getProblemEntity(problemId)
            
            logger.info("Generating LaTeX content for problem: ${problem.title}")
            val latexContent = latexTemplateEngine.generateLatex(problem)
            logger.debug("Generated LaTeX content length: ${latexContent.length} characters")
            
            val resource = ByteArrayResource(latexContent.toByteArray())
            
            val headers = HttpHeaders().apply {
                add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${problem.title}.tex\"")
                add(HttpHeaders.CONTENT_TYPE, "application/x-tex")
            }
            
            logger.info("Successfully exported LaTeX for problemId: $problemId")
            ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .body(resource)
                
        } catch (e: Exception) {
            logger.error("Failed to export LaTeX for problemId: $problemId", e)
            ResponseEntity.internalServerError().build()
        }
    }
    
    /**
     * Exports problem statement as PDF file
     * @param problemId - ID of the problem to export
     * @param user - Authenticated user
     * @return - PDF file as downloadable resource
     */
    @GetMapping("/pdf")
    fun exportPdf(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<ByteArrayResource> {
        return try {
            logger.info("Exporting PDF for problemId: $problemId, user: ${user.username}")

            problemService.getProblem(problemId, user.id!!)
            val problem = problemService.getProblemEntity(problemId)

            // Generate test previews for sample tests to ensure outputs are available
            logger.info("Generating test previews for sample tests")
            val previewResponse = problemTestsService.generatePreview(problemId, user.id!!)
            if (previewResponse.status != ru.nsu.problem_forge.dto.problem.JobStatus.COMPLETED) {
                logger.warn("Test preview generation failed or incomplete: ${previewResponse.message}")
                // Continue with export even if preview generation fails
            }

            // Reload the problem entity to get updated test data with outputs
            val updatedProblem = problemService.getProblemEntity(problemId)

            // Get contest information for the problem (use first contest if multiple)
            val contests = contestService.getContestsForProblem(problemId, user.id!!)
            val contest = contests.firstOrNull()

            logger.info("Generating LaTeX content for problem: ${updatedProblem.title}")
            val latexContent = latexTemplateEngine.generateLatex(updatedProblem, contest)
            logger.debug("Generated LaTeX content length: ${latexContent.length} characters")

            logger.info("Compiling LaTeX to PDF for problemId: $problemId")
            val pdfBytes = latexCompilationService.compileLatexToPdf(latexContent)
            logger.debug("Compiled PDF size: ${pdfBytes.size} bytes")

            val resource = ByteArrayResource(pdfBytes)

            val headers = HttpHeaders().apply {
                add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${updatedProblem.title}.pdf\"")
                add(HttpHeaders.CONTENT_TYPE, "application/pdf")
            }

            logger.info("Successfully exported PDF for problemId: $problemId")
            ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .body(resource)

        } catch (e: LatexCompilationException) {
            logger.error("LaTeX compilation failed for problemId: $problemId", e)
            ResponseEntity.badRequest().body(ByteArrayResource("LaTeX compilation failed: ${e.message}".toByteArray()))
        } catch (e: Exception) {
            logger.error("Failed to export PDF for problemId: $problemId", e)
            ResponseEntity.internalServerError().build()
        }
    }
}