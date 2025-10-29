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

//    fun compileLatexToPdf(latexSource: String): ByteArray {
//        // Create temporary directory
//        val tempDir = Files.createTempDirectory("latex_compile")
//        val texFile = tempDir.resolve("document.tex")
//
//        try {
//            // Write LaTeX source to file
//            Files.write(texFile, latexSource.toByteArray())
//
//            // Run pdflatex command
//            val processBuilder = ProcessBuilder(
//                "pdflatex",
//                "-interaction=nonstopmode",
//                "-halt-on-error",
//                texFile.toString()
//            ).directory(tempDir.toFile())
//
//            val process = processBuilder.start()
//            val exitCode = process.waitFor()
//
//            if (exitCode != 0) {
//                val errorOutput = process.errorStream.bufferedReader().readText()
//                throw LatexCompilationException("LaTeX compilation failed with exit code $exitCode: $errorOutput")
//            }
//
//            // Read the generated PDF
//            val pdfFile = tempDir.resolve("document.pdf")
//            if (!Files.exists(pdfFile)) {
//                throw LatexCompilationException("PDF file was not generated")
//            }
//
//            return Files.readAllBytes(pdfFile)
//
//        } finally {
//            // Clean up temporary files
//            deleteDirectory(tempDir)
//        }
//    }
//
//    class LatexCompilationException(message: String) : Exception(message)
//
//    private fun deleteDirectory(directory: Path) {
//        Files.walk(directory)
//            .sorted(Comparator.reverseOrder())
//            .map { it.toFile() }
//            .forEach { it.delete() }
//    }
}