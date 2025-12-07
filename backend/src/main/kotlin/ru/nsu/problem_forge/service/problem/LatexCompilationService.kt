package ru.nsu.problem_forge.service.problem

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class LatexCompilationService {
    
    private val logger = LoggerFactory.getLogger(LatexCompilationService::class.java)

    fun compileLatexToPdf(latexSource: String): ByteArray {
        val tempDir = Files.createTempDirectory("latex_${System.currentTimeMillis()}")
        val texFile = tempDir.resolve("document.tex")

        return try {
            logger.info("Starting LaTeX compilation in directory: $tempDir")
            
            // Write LaTeX source to file
            Files.write(texFile, latexSource.toByteArray())
            logger.debug("Wrote LaTeX source to: $texFile")
            
            // Copy olymp.sty to temp directory if needed
            copyOlympStyIfNeeded(tempDir)

            // Run pdflatex twice to resolve references and TOC
            logger.info("Running first pdflatex pass")
            runPdflatex(tempDir, texFile)
            logger.info("Running second pdflatex pass")
            runPdflatex(tempDir, texFile)

            // Read the generated PDF
            val pdfFile = tempDir.resolve("document.pdf")
            if (!Files.exists(pdfFile)) {
                val logFile = tempDir.resolve("document.log")
                val logContent = if (Files.exists(logFile)) Files.readString(logFile) else "No log file found"
                logger.error("PDF file was not generated. LaTeX log: $logContent")
                throw LatexCompilationException("PDF file was not generated. Log: $logContent")
            }

            val pdfBytes = Files.readAllBytes(pdfFile)
            logger.info("Successfully compiled PDF, size: ${pdfBytes.size} bytes")
            pdfBytes

        } catch (e: Exception) {
            logger.error("LaTeX compilation failed", e)
            if (e is LatexCompilationException) {
                throw e
            } else {
                throw LatexCompilationException("Compilation failed: ${e.message}", e)
            }
        } finally {
            // Clean up temporary files
            cleanupTempDirectory(tempDir)
        }
    }

    private fun runPdflatex(workingDir: Path, texFile: Path) {
        try {
            val command = listOf(
                "xelatex",
                "-interaction=nonstopmode",
                "-halt-on-error",
                "-no-shell-escape",
                "-output-directory", workingDir.toString(),
                texFile.fileName.toString()
            )

            logger.debug("Running pdflatex command: ${command.joinToString(" ")}")

            val process = ProcessBuilder(command)
                .directory(workingDir.toFile())
                .start()

            // Capture both output streams
            val output = process.inputStream.bufferedReader().readText()
            val errorOutput = process.errorStream.bufferedReader().readText()

            logger.debug("pdflatex stdout: $output")
            if (errorOutput.isNotEmpty()) {
                logger.debug("pdflatex stderr: $errorOutput")
            }

            // Wait for process with timeout (30 seconds)
            val completed = process.waitFor(30, TimeUnit.SECONDS)
            if (!completed) {
                process.destroy()
                throw LatexCompilationException("LaTeX compilation timed out after 30 seconds")
            }

            val exitCode = process.exitValue()
            logger.debug("pdflatex exit code: $exitCode")

            if (exitCode != 0) {
                val logFile = workingDir.resolve("document.log")
                val logContent = if (Files.exists(logFile)) Files.readString(logFile) else "No log file"
                logger.error("pdflatex failed with exit code $exitCode. Log: $logContent")
                throw LatexCompilationException("LaTeX compilation failed with exit code $exitCode: $errorOutput")
            }

        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw LatexCompilationException("LaTeX compilation was interrupted", e)
        } catch (e: Exception) {
            throw LatexCompilationException("Failed to run pdflatex: ${e.message}", e)
        }
    }

    private fun cleanupTempDirectory(directory: Path) {
        try {
            logger.debug("Cleaning up temporary directory: $directory")
            Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map { it.toFile() }
                .forEach { it.delete() }
            logger.debug("Successfully cleaned up temporary directory")
        } catch (e: Exception) {
            logger.warn("Failed to clean up temporary directory: ${e.message}", e)
        }
    }
    
    private fun copyOlympStyIfNeeded(tempDir: Path) {
        try {
            val olympStyStream = this::class.java.classLoader.getResourceAsStream("latex/olymp.sty")
                ?: return // Skip if olymp.sty not found
            
            val olympStyFile = tempDir.resolve("olymp.sty")
            Files.copy(olympStyStream, olympStyFile)
            olympStyStream.close()
            logger.debug("Successfully copied olymp.sty to $tempDir")
        } catch (e: Exception) {
            logger.warn("Failed to copy olymp.sty: ${e.message}", e)
        }
    }
}

class LatexCompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)