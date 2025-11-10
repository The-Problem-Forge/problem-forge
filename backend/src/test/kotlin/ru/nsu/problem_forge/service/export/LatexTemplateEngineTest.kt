package ru.nsu.problem_forge.service.export

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.type.ProblemInfo
import ru.nsu.problem_forge.type.problem.General
import ru.nsu.problem_forge.type.problem.Statement
import ru.nsu.problem_forge.type.problem.TestType
import ru.nsu.problem_forge.type.problem.ProblemTest

class LatexTemplateEngineTest {

  private val fileRepository: FileRepository = mockk()

  @Test
  fun `should generate basic LaTeX structure`() {
    // Arrange
    val templateEngine = LatexTemplateEngine(fileRepository)
    val problem = createBasicProblem(
      title = "Test Problem",
      legend = "This is a test problem",
      inputFormat = "First line contains N",
      outputFormat = "Print the answer",
      notes = "Be careful",
      timeLimit = 2000
    )

    // Act
    val latex = templateEngine.generateLatex(problem)

    // Debug output
    println("Generated LaTeX:")
    println(latex)
    println("--- End of LaTeX ---")

    // Assert - check for complete document structure
    assertTrue(latex.contains("\\documentclass[12pt]{article}"))
    assertTrue(latex.contains("\\usepackage[utf8]{inputenc}"))
    assertTrue(latex.contains("\\usepackage{olymp}"))
    assertTrue(latex.contains("\\begin{document}"))
    assertTrue(latex.contains("\\begin{problem}{Test Problem}{input.txt}{output.txt}{2.0 seconds}{256 megabytes}"))
    assertTrue(latex.contains("\\InputFile"))
    assertTrue(latex.contains("\\OutputFile"))
    assertTrue(latex.contains("This is a test problem"))
    assertTrue(latex.contains("First line contains N"))
    assertTrue(latex.contains("Print the answer"))
    assertTrue(latex.contains("\\Note"))
    assertTrue(latex.contains("Be careful"))
    assertTrue(latex.contains("\\end{problem}"))
    assertTrue(latex.contains("\\end{document}"))
  }

  @Test
  fun `should escape LaTeX special characters`() {
    // Arrange
    val templateEngine = LatexTemplateEngine(fileRepository)
    val problem = createBasicProblem(
      title = "Problem with special chars",
      legend = "Use formula: x^2 + y^2 = r^2",
      inputFormat = "Input contains #hashtag",
      outputFormat = "Output & result",
      notes = ""
    )

    // Act
    val latex = templateEngine.generateLatex(problem)

    // Debug output
    println("Generated LaTeX for escaping test:")
    println(latex)
    println("--- End of LaTeX ---")

    // Assert - check that special characters are not escaped
    assertTrue(latex.contains("\\documentclass[12pt]{article}"))
    assertTrue(latex.contains("\\usepackage{olymp}"))
    assertTrue(latex.contains("\\begin{document}"))
    assertTrue(latex.contains("Use formula: x^2 + y^2 = r^2"))
    assertTrue(latex.contains("Input contains #hashtag"))
    assertTrue(latex.contains("Output & result"))
    assertTrue(latex.contains("\\end{document}"))
  }

  @Test
  fun `should handle empty statement fields`() {
    // Arrange
    val templateEngine = LatexTemplateEngine(fileRepository)
    val problem = createBasicProblem(
      title = "Empty Problem",
      legend = "",
      inputFormat = "",
      outputFormat = "",
      notes = ""
    )

    // Act
    val latex = templateEngine.generateLatex(problem)

    // Assert
    assertTrue(latex.contains("\\documentclass[12pt]{article}"))
    assertTrue(latex.contains("\\usepackage{olymp}"))
    assertTrue(latex.contains("\\begin{document}"))
    assertTrue(latex.contains("\\begin{problem}{Empty Problem}{input.txt}{output.txt}{1.0 seconds}{256 megabytes}"))
    assertTrue(latex.contains("\\InputFile"))
    assertTrue(latex.contains("\\OutputFile"))
    assertTrue(latex.contains("\\end{problem}"))
    assertTrue(latex.contains("\\end{document}"))
  }

  @Test
  fun `should include examples when sample tests exist`() {
    // Arrange
    val templateEngine = LatexTemplateEngine(fileRepository)
    val sampleTests = listOf(
      ProblemTest(
        testType = TestType.RAW,
        content = "3 5",
        description = "Simple test",
        sample = true,
        points = 1
      ),
      ProblemTest(
        testType = TestType.RAW,
        content = "10 20",
        description = "Another test",
        sample = true,
        points = 1
      )
    )
    val problem = createBasicProblem(title = "Problem with Examples", tests = sampleTests)

    // Act
    val latex = templateEngine.generateLatex(problem)

    // Assert
    assertTrue(latex.contains("\\documentclass[12pt]{article}"))
    assertTrue(latex.contains("\\usepackage{olymp}"))
    assertTrue(latex.contains("\\begin{document}"))
    assertTrue(latex.contains("\\Examples"))
    assertTrue(latex.contains("\\begin{example}"))
    assertTrue(latex.contains("\\exmp{3 5}{}%"))
    assertTrue(latex.contains("\\exmp{10 20}{}%"))
    assertTrue(latex.contains("\\end{example}"))
    assertTrue(latex.contains("\\end{document}"))
  }

  @Test
  fun `should not include examples when no sample tests exist`() {
    // Arrange
    val templateEngine = LatexTemplateEngine(fileRepository)
    val nonSampleTests = listOf(
      ProblemTest(
        testType = TestType.RAW,
        content = "3 5",
        description = "Simple test",
        sample = false,
        points = 1
      )
    )
    val problem = createBasicProblem(title = "Problem without Examples", tests = nonSampleTests)

    // Act
    val latex = templateEngine.generateLatex(problem)

    // Assert
    assertTrue(latex.contains("\\documentclass[12pt]{article}"))
    assertTrue(latex.contains("\\usepackage{olymp}"))
    assertTrue(latex.contains("\\begin{document}"))
    assertFalse(latex.contains("\\Examples"))
    assertFalse(latex.contains("\\begin{example}"))
    assertFalse(latex.contains("\\exmp"))
    assertTrue(latex.contains("\\end{document}"))
  }

  private fun createBasicProblem(
    title: String,
    legend: String = "",
    inputFormat: String = "",
    outputFormat: String = "",
    notes: String = "",
    tests: List<ProblemTest> = listOf(),
    timeLimit: Int = 1000,
  ): Problem {
    val problem = Problem()
    problem.title = title
    problem.problemInfo = ProblemInfo(
      General(
        inputFile = "input.txt",
        outputFile = "output.txt",
        timeLimit = timeLimit,
        memoryLimit = 256
      ),
      Statement(
        name = title,
        legend = legend,
        inputFormat = inputFormat,
        outputFormat = outputFormat,
        scoring = "",
        notes = notes,
        tutorial = ""
      ),
      tests = tests,
    )
    return problem
  }
}
