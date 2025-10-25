package ru.nsu.problem_forge.service.problem

import jakarta.transaction.NotSupportedException
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpServerErrorException
import ru.nsu.problem_forge.entity.Problem

// Currently unsupported...
@Service
class PdfExportService {

    fun generateStatementPdf(problem: Problem): ByteArray? {
        // Implement PDF generation logic here
        // This could use LaTeX compilation or a PDF library
        // Return null if not available
        throw NotSupportedException("Pdf generation currently not supported")
    }

    fun generateTutorialPdf(problem: Problem): ByteArray? {
        // Implement PDF generation logic here
        throw NotSupportedException("Pdf generation currently not supported")
    }
}