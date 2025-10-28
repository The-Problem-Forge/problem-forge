package ru.nsu.problem_forge.dto.nsuts

object StatusMapping {
    private val statuses = mapOf(
        "A" to StatusInfo("🏆 ACCEPTED", "ПРИНЯТО"),
        "C" to StatusInfo("🚧 Compile Error", "Ошибка компиляции"),
        "D" to StatusInfo("🥶 Deadlock - Timeout", "Превышено астрономическое время"),
        "F" to StatusInfo("🤢 Judgement Failed", "Ошибка тестирования"),
        "J" to StatusInfo("🤮 Jury Error", "Ошибка жюри"),
        "K" to StatusInfo("👍 Compiled", "Скомпилировано"),
        "M" to StatusInfo("🤯 Memory Limit Exceeded", "Превышено потребление памяти"),
        "O" to StatusInfo("🔍 No Output File", "Отсутствует выходной файл"),
        "P" to StatusInfo("🚽 Presentation Error", "Неверный формат выходных данных"),
        "R" to StatusInfo("🔥 Run-Time Error", "Ошибка во время исполнения"),
        "S" to StatusInfo("💀 Security Violation", "Программа выполнила запрещённое действие"),
        "T" to StatusInfo("⌛ Time Limit Exceeded", "Превышено процессорное время"),
        "W" to StatusInfo("🗿 Wrong Answer", "Неправильный ответ"),
        "X" to StatusInfo("✨ Static Analysis Failed", "Ошибка статического анализа кода"),
        "Y" to StatusInfo("🔧 Dynamic Analysis Failed", "Ошибка динамического анализа кода"),
        "." to StatusInfo("👀 Skipped", "Тест пропущен")
    )

    fun getStatusInfo(code: String): StatusInfo {
        return statuses[code] ?: StatusInfo("❓ Unknown", "Неизвестный статус")
    }
}

data class StatusInfo(
    val english: String,
    val russian: String
)