package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class LatexCompilationService {

    fun compileLatexToPdf(latexSource: String): ByteArray {
        val tempDir = Files.createTempDirectory("latex_${System.currentTimeMillis()}")
        val texFile = tempDir.resolve("document.tex")

        try {
            // Write LaTeX source to file
            Files.write(texFile, latexSource.toByteArray())

            // Run pdflatex twice to resolve references and TOC
            runPdflatex(tempDir, texFile)
            runPdflatex(tempDir, texFile)

            // Read the generated PDF
            val pdfFile = tempDir.resolve("document.pdf")
            if (!Files.exists(pdfFile)) {
                throw LatexCompilationException("PDF file was not generated")
            }

            return Files.readAllBytes(pdfFile)

        } finally {
            // Clean up temporary files
            cleanupTempDirectory(tempDir)
        }
    }

    private fun runPdflatex(workingDir: Path, texFile: Path) {
        val process = ProcessBuilder(
            "pdflatex",
            "-interaction=nonstopmode",
            "-halt-on-error",
            "-output-directory", workingDir.toString(),
            texFile.fileName.toString()
        )
            .directory(workingDir.toFile())
            .start()

        // Wait for process with timeout (30 seconds)
        val completed = process.waitFor(30, TimeUnit.SECONDS)
        if (!completed) {
            process.destroy()
            throw LatexCompilationException("LaTeX compilation timed out")
        }

        if (process.exitValue() != 0) {
            val errorOutput = process.errorStream.bufferedReader().readText()
            throw LatexCompilationException("LaTeX compilation failed: $errorOutput")
        }
    }

    private fun cleanupTempDirectory(directory: Path) {
        try {
            Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map { it.toFile() }
                .forEach { it.delete() }
        } catch (e: Exception) {
            println("Warning: Failed to clean up temporary directory: ${e.message}")
        }
    }
}

class LatexCompilationException(message: String) : Exception(message)