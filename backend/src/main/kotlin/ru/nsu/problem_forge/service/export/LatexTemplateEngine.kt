package ru.nsu.problem_forge.service.export

import org.springframework.stereotype.Service
import ru.nsu.problem_forge.dto.ContestDto
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.type.problem.General
import java.time.format.DateTimeFormatter

/**
 * Service for generating LaTeX content from Problem entities
 */
@Service
class LatexTemplateEngine(
    private val fileRepository: FileRepository
) {

  /**
   * Generates LaTeX content for a problem statement
   * @param problem - Problem entity containing statement data
   * @param contest - Contest information (optional)
   * @return - Complete LaTeX document as string
   */
  fun generateLatex(problem: Problem, contest: ContestDto? = null): String {
    val statement = problem.problemInfo.statement
    val general = problem.problemInfo.general

    return buildString {
       // LaTeX document header with olymp.sty package
       appendLine("\\documentclass[12pt]{article}")
       appendLine("\\usepackage[utf8]{inputenc}")
       appendLine("\\usepackage{olymp}")
       appendLine()

       // Set contest information if available
       contest?.let { c ->
         val contestName = c.name
         val contestLocation = c.location ?: "Unknown Location"
         val contestDate = c.contestDate?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) ?: "TBD"
         appendLine("\\contest{$contestName}{$contestLocation}{$contestDate}")
         appendLine()
       }

       appendLine("\\begin{document}")
       appendLine()

      // Problem environment with olymp.sty formatting
      appendLine("\\begin{problem}{${problem.title}}{${general.inputFile}}{${general.outputFile}}{${general.timeLimit / 1000.0} seconds}{${general.memoryLimit} megabytes}")

      // Statement legend
      if (statement.legend.isNotBlank()) {
        appendLine()
        appendLine(statement.legend)
        appendLine()
      }

      // Input format section
      appendLine("\\InputFile")
      if (statement.inputFormat.isNotBlank()) {
        appendLine(statement.inputFormat)
      } else {
        appendLine("Input format not specified")
      }
      appendLine()

      // Output format section
      appendLine("\\OutputFile")
      if (statement.outputFormat.isNotBlank()) {
        appendLine(statement.outputFormat)
      } else {
        appendLine("Output format not specified")
      }
      appendLine()

       // Examples section (if any tests with sample flag exist)
       val sampleTests = problem.problemInfo.tests.filter { it.sample }
       if (sampleTests.isNotEmpty()) {
         appendLine("\\Examples")
         appendLine()
         appendLine("\\begin{example}")
         sampleTests.forEach { test ->
           val input = test.content
           // For sample tests, fetch output from outputFileId if available
           val output = test.outputFileId?.let { fileId ->
             fileRepository.findById(fileId).map { String(it.content) }.orElse("")
           } ?: ""
           appendLine("\\exmp{$input}{$output}%")
         }
         appendLine("\\end{example}")
         appendLine()
       }

      // Notes section
      if (statement.notes.isNotBlank()) {
        appendLine("\\Note")
        appendLine(statement.notes)
        appendLine()
      }

      appendLine("\\end{problem}")
      appendLine()
      appendLine("\\end{document}")
    }
  }
}
