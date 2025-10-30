package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import ru.nsu.problem_forge.dto.StatementDto
import ru.nsu.problem_forge.dto.StatementResponse
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.type.problem.Statement
import javax.naming.ServiceUnavailableException

@Service
class ProblemStatementService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val pdfExportService: PdfExportService
) {

    fun getStatement(problemId: Long, userId: Long): StatementResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found exception") }

        // Check permissions
        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

//        // Check if this is a branch (has parent)
//        if (problem.parentProblemId == null) {
//            throw IllegalStateException("Cannot edit main problem branch")
//        }

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

//        // Check if this is a branch (has parent)
//        if (problem.parentProblemId == null) {
//            throw IllegalStateException("Cannot edit main problem branch")
//        }

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

        val texContent = generateLatexStatement(problem)

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"problem-${problem.title}-statement.tex\""
            )
            .body(texContent)
    }

    fun exportStatementToPdf(problemId: Long, userId: Long): ResponseEntity<ByteArray> {
        val problem = getProblemWithAccessCheck(problemId, userId)

        val pdfBytes = pdfExportService.generateStatementPdf(problem)
            ?: throw ServiceUnavailableException("PDF generation not available")

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"problem-${problem.id}-statement.pdf\""
            )
            .body(pdfBytes)
    }

    fun exportTutorialToTex(problemId: Long, userId: Long): ResponseEntity<String> {
        val problem = getProblemWithAccessCheck(problemId, userId)

        val texContent = generateLatexTutorial(problem)

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"problem-${problem.title}-tutorial.tex\""
            )
            .body(texContent)
    }

    fun exportTutorialToPdf(problemId: Long, userId: Long): ResponseEntity<ByteArray> {
        val problem = getProblemWithAccessCheck(problemId, userId)

        val pdfBytes = pdfExportService.generateTutorialPdf(problem)
            ?: throw ServiceUnavailableException("PDF generation not available")

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"problem-${problem.id}-tutorial.pdf\""
            )
            .body(pdfBytes)
    }

    private fun getProblemWithAccessCheck(problemId: Long, userId: Long): Problem {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        return problem
    }

    private fun generateLatexStatement(problem: Problem): String {
        val statement = problem.problemInfo.statement
        // Simple LaTeX template - you can enhance this as needed
        return """
            \documentclass[12pt]{article}
            \usepackage[utf8]{inputenc}
            \usepackage{amsmath}
            \usepackage{amssymb}
            \usepackage{graphicx}
            
            \title{${statement.name}}
            \author{}
            \date{}
            
            \begin{document}
            
            \maketitle
            
            \section*{Problem Statement}
            ${statement.legend}
            
            \section*{Input Format}
            ${statement.inputFormat}
            
            \section*{Output Format}
            ${statement.outputFormat}
            
            \section*{Scoring}
            ${statement.scoring}
            
            \section*{Notes}
            ${statement.notes}
            
            \end{document}
        """.trimIndent()
    }

    private fun generateLatexTutorial(problem: Problem): String {
        val statement = problem.problemInfo.statement
        return """
            \documentclass[12pt]{article}
            \usepackage[utf8]{inputenc}
            \usepackage{amsmath}
            \usepackage{amssymb}
            \usepackage{graphicx}
            
            \title{Tutorial: ${statement.name}}
            \author{}
            \date{}
            
            \begin{document}
            
            \maketitle
            
            ${statement.tutorial}
            
            \end{document}
        """.trimIndent()
    }
}