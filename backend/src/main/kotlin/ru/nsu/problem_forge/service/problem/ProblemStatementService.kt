package ru.nsu.problem_forge.service.problem

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.nsu.problem_forge.dto.StatementDto
import ru.nsu.problem_forge.dto.StatementResponse
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.Statement
import java.time.LocalDateTime
import javax.naming.ServiceUnavailableException

@Service
class ProblemStatementService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val latexGenerationService: LatexGenerationService,
    private val latexCompilationService: LatexCompilationService
) {

    fun getStatement(problemId: Long, userId: Long): StatementResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found exception") }

        // Check permissions
        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val statement = problem.problemInfo.statement
        return StatementResponse(
            name = statement.name,
            legend = statement.legend,
            inputFormat = statement.inputFormat,
            outputFormat = statement.outputFormat,
            scoring = statement.scoring,
            notes = statement.notes,
            tutorial = statement.tutorial
        )
    }

    fun updateStatement(problemId: Long, userId: Long, updateDto: StatementDto): StatementResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found exception") }

        // Check permissions - Editor+ required
        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        // Update statement
        val currentStatement = problem.problemInfo.statement
        val updatedStatement = Statement(
            name = updateDto.name ?: currentStatement.name,
            legend = updateDto.legend ?: currentStatement.legend,
            inputFormat = updateDto.inputFormat ?: currentStatement.inputFormat,
            outputFormat = updateDto.outputFormat ?: currentStatement.outputFormat,
            scoring = updateDto.scoring ?: currentStatement.scoring,
            notes = updateDto.notes ?: currentStatement.notes,
            tutorial = updateDto.tutorial ?: currentStatement.tutorial
        )

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(statement = updatedStatement)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        return StatementResponse(
            name = updatedStatement.name,
            legend = updatedStatement.legend,
            inputFormat = updatedStatement.inputFormat,
            outputFormat = updatedStatement.outputFormat,
            scoring = updatedStatement.scoring,
            notes = updatedStatement.notes,
            tutorial = updatedStatement.tutorial
        )
    }

    fun exportStatementToTex(problemId: Long, userId: Long): ResponseEntity<String> {
        val problem = getProblemWithAccessCheck(problemId, userId)
        val texContent = latexGenerationService.generateLatexStatement(problem) // Reuse here

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"problem-${problem.title}-statement.tex\""
            )
            .body(texContent)
    }

    fun exportTutorialToTex(problemId: Long, userId: Long): ResponseEntity<String> {
        val problem = getProblemWithAccessCheck(problemId, userId)
        val texContent = latexGenerationService.generateLatexTutorial(problem) // Reuse here

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"problem-${problem.title}-tutorial.tex\""
            )
            .body(texContent)
    }

    private fun getProblemWithAccessCheck(problemId: Long, userId: Long): Problem {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        return problem
    }

    fun downloadStatementPdf(problemId: Long, userId: Long, response: HttpServletResponse) {
        val problem = getProblemWithAccessCheck(problemId, userId)

        val pdfData = try {
            val latexContent = latexGenerationService.generateLatexStatement(problem)
            latexCompilationService.compileLatexToPdf(latexContent)
        } catch (e: Exception) {
            throw ServiceUnavailableException("PDF generation failed")
        }

        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "attachment; filename=\"problem_${problem.id}_statement.pdf\"")
        response.setContentLength(pdfData.size)

        response.outputStream.use { outputStream ->
            outputStream.write(pdfData)
            outputStream.flush()
        }
    }

    fun downloadTutorialPdf(problemId: Long, userId: Long, response: HttpServletResponse) {
        val problem = getProblemWithAccessCheck(problemId, userId)

        val pdfData = try {
            val latexContent = latexGenerationService.generateLatexTutorial(problem)
            latexCompilationService.compileLatexToPdf(latexContent)
        } catch (e: Exception) {
            throw ServiceUnavailableException("PDF generation failed")
        }

        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "attachment; filename=\"problem_${problem.id}_tutorial.pdf\"")
        response.setContentLength(pdfData.size)

        response.outputStream.use { outputStream ->
            outputStream.write(pdfData)
            outputStream.flush()
        }
    }
}