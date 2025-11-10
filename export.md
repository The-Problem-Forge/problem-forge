# LaTeX/PDF Export Implementation Guide

## Overview
Add LaTeX source and PDF export functionality for problem statements to the Kotlin Spring backend, matching the format shown in `example.tex` and `example.pdf` using `olymp.sty` styling.

---

## 1. Locate Existing Foundation

### Find in Codebase:
- **Problem Entity/Model**: Class storing problem data (likely fields: `title`, `description`, `inputSpec`, `outputSpec`, `examples: List&lt;Example&gt;`, `timeLimit`, `memoryLimit`, `constraints`)
- **Problem Service**: Service layer for fetching problems (e.g., `ProblemService.getProblemById(id: Long): Problem`)
- **Existing Export Patterns**: Check for any current export features (JSON, CSV responses) to follow established patterns
- **File Download Utilities**: Look for existing file serving logic (e.g., `ByteArrayResource`, `ResponseEntity` helpers)

---

## 2. New Components to Create

Create package: `com.yourapp.problem.export`

| File | Responsibility |
|------|----------------|
| `LatexTemplateEngine.kt` | Generates valid `.tex` content from Problem entity using template engine |
| `PdfGenerator.kt` | Compiles generated LaTeX to PDF (spawns `pdflatex` or uses Java library) |
| `ExportController.kt` | REST endpoint: `GET /api/problems/{id}/export?format=[pdf/tex]` |
| `LatexEscaper.kt` | Sanitizes user input text for safe LaTeX compilation |

---

## 3. Key Implementation Tasks

### 3.1 Template Setup
- Add `olymp.sty` to: `src/main/resources/latex/olymp.sty`
- Create Mustache/Thymeleaf template at: `src/main/resources/templates/problem.tex.mustache`
  - Match structure of `example.tex` exactly
  - Use placeholders: `{{title}}`, `{{inputFormat}}`, `{{timeLimit}}`, `{{memoryLimit}}`
  - Loop examples: `{{#examples}} \exmp{ {{input}} }{ {{output}} } {{/examples}}`

### 3.2 LatexTemplateEngine
- Inject template engine (Spring's `TemplateEngine` or Mustache)
- Method: `generateLatex(problem: Problem): String`
- Handle multiline: Convert `\n` → `\\` or `\par` in descriptions
- Escape all user text fields using `LatexEscaper`

### 3.3 LatexEscaper
- Method: `escape(text: String): String`
- Replace: `$ → \$`, `% → \%`, `& → \&`, `_ → \_`, `# → \#`, `{ → \{`, `} → \}`, `~ → \textasciitilde`, `^ → \textasciicircum`
- Preserve existing LaTeX commands if any (unlikely for problem statements)

### 3.4 PdfGenerator
- Method: `generatePdf(latexContent: String): ByteArray`
- Steps:
  1. Create temp directory with `Files.createTempDirectory()`
  2. Write `problem.tex` and copy `olymp.sty` into it
  3. Execute: `pdflatex -interaction=nonstopmode problem.tex`
  4. Capture `problem.pdf` as `ByteArray`
  5. **Always cleanup** temp files in `finally` block
- Use `ProcessBuilder` for execution
- Timeout: 10 seconds max per compilation
- Handle errors: Check exit code, capture `stderr`

### 3.5 ExportController
```kotlin
@GetMapping("/api/problems/{id}/export")
fun exportProblem(
    @PathVariable id: Long,
    @RequestParam(defaultValue = "pdf") format: String
): ResponseEntity&lt;ByteArrayResource&gt;
