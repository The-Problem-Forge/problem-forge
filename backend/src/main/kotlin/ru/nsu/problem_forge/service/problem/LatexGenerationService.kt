package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import ru.nsu.problem_forge.entity.Problem

@Service
class LatexGenerationService {

    fun generateLatexStatement(problem: Problem): String {
        val statement = problem.problemInfo.statement
        return """
            \documentclass[12pt]{article}
            \usepackage[utf8]{inputenc}
            \usepackage{amsmath}
            \usepackage{amssymb}
            \usepackage{graphicx}
            \usepackage{geometry}
            \geometry{a4paper, margin=1in}
            
            \title{${escapeLatex(statement.name)}}
            \author{}
            \date{}
            
            \begin{document}
            
            \maketitle
            
            \section*{Problem Statement}
            ${escapeLatex(statement.legend)}
            
            \section*{Input Format}
            ${escapeLatex(statement.inputFormat)}
            
            \section*{Output Format}
            ${escapeLatex(statement.outputFormat)}
            
            \section*{Scoring}
            ${escapeLatex(statement.scoring)}
            
            \section*{Notes}
            ${escapeLatex(statement.notes)}
            
            \end{document}
        """.trimIndent()
    }

    fun generateLatexTutorial(problem: Problem): String {
        val statement = problem.problemInfo.statement
        return """
            \documentclass[12pt]{article}
            \usepackage[utf8]{inputenc}
            \usepackage{amsmath}
            \usepackage{amssymb}
            \usepackage{graphicx}
            \usepackage{geometry}
            \geometry{a4paper, margin=1in}
            
            \title{Tutorial: ${escapeLatex(statement.name)}}
            \author{}
            \date{}
            
            \begin{document}
            
            \maketitle
            
            ${escapeLatex(statement.tutorial)}
            
            \end{document}
        """.trimIndent()
    }

    private fun escapeLatex(text: String): String {
        return text
            .replace("\\", "\\textbackslash ")
            .replace("#", "\\#")
            .replace("$", "\\$")
            .replace("%", "\\%")
            .replace("&", "\\&")
            .replace("_", "\\_")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("~", "\\textasciitilde ")
            .replace("^", "\\textasciicircum ")
            .replace("\n", "\n\n")
    }
}