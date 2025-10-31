package ru.nsu.problem_forge.runner

import org.springframework.stereotype.Component
import ru.nsu.problem_forge.runner.Runner.*
import java.io.File
import java.util.concurrent.TimeUnit

@Component
class SimpleRunner : Runner {

    override fun run(programSource: String, runs: List<RunInput>, testlibNeeded: Boolean): List<RunOutput> {
        var tempDir: File? = null
        try {
            tempDir = createTempDir("runner_")

            // Write source to file
            val sourceFile = File(tempDir, "program.cpp")
            sourceFile.writeText(programSource)

            // Compile the program
            val binaryFile = File(tempDir, "program.out")
            val compileSuccess = compileProgram(sourceFile, binaryFile, testlibNeeded, tempDir)

            if (!compileSuccess) {
                return runs.map {
                    RunOutput(RunStatus.COMPILE_ERROR, "Compilation failed")
                }
            }

            // Make binary executable
            binaryFile.setExecutable(true)

            // Execute all runs
            return runs.map { runInput ->
                try {
                    val command = mutableListOf(binaryFile.absolutePath)
                    command.addAll(runInput.args)

                    val runProcess = ProcessBuilder(command)
                        .directory(tempDir)
                        .redirectErrorStream(true)
                        .start()

                    // Write input to process
                    runProcess.outputStream.bufferedWriter().use { writer ->
                        writer.write(runInput.inputContent)
                        writer.flush()
                    }

                    val runSuccess = runProcess.waitFor(runInput.timeLimit, TimeUnit.MILLISECONDS)

                    if (!runSuccess) {
                        runProcess.destroyForcibly()
                        return@map RunOutput(RunStatus.RUNTIME_ERROR, "Time limit exceeded")
                    }

                    val runOutput = runProcess.inputStream.bufferedReader().readText()
                    val errorOutput = runProcess.errorStream.bufferedReader().readText()

                    if (runProcess.exitValue() != 0) {
                        return@map RunOutput(RunStatus.RUNTIME_ERROR, "Runtime error. stderr: $errorOutput")
                    }

                    RunOutput(RunStatus.SUCCESS, runOutput)

                } catch (e: Exception) {
                    RunOutput(RunStatus.RUNTIME_ERROR, "Execution error: ${e.message}")
                }
            }

        } catch (e: Exception) {
            return runs.map {
                RunOutput(RunStatus.RUNTIME_ERROR, "Setup error: ${e.message}")
            }
        } finally {
            tempDir?.deleteRecursively()
        }
    }

    private fun compileProgram(sourceFile: File, outputFile: File, testlibNeeded: Boolean, tempDir: File): Boolean {
        val compileCommand = mutableListOf("g++", "-std=c++17", "-O2")

        if (testlibNeeded) {
            val testlibFile = copyTestlibToTemp(tempDir)
            compileCommand.add("-I${tempDir.absolutePath}")
            compileCommand.add(testlibFile.absolutePath)
        }

        compileCommand.add(sourceFile.absolutePath)
        compileCommand.add("-o")
        compileCommand.add(outputFile.absolutePath)

        try {
            val compileProcess = ProcessBuilder(compileCommand)
                .directory(tempDir)
                .redirectErrorStream(true)
                .start()

            val compileSuccess = compileProcess.waitFor(10, TimeUnit.SECONDS)
            val compileOutput = compileProcess.inputStream.bufferedReader().readText()

            if (!compileSuccess || compileProcess.exitValue() != 0) {
                println("Compilation failed. Output: $compileOutput")
                return false
            }

            return outputFile.exists()
        } catch (e: Exception) {
            println("Compilation error: ${e.message}")
            return false
        }
    }

    override fun compileToBinary(programSource: String, testlibNeeded: Boolean): ByteArray? {
        var tempDir: File? = null
        try {
            tempDir = createTempDir("compiler_")
            val sourceFile = File(tempDir, "program.cpp")
            sourceFile.writeText(programSource)

            val outputFile = File(tempDir, "program.out")
            val compileSuccess = compileProgram(sourceFile, outputFile, testlibNeeded, tempDir)

            if (compileSuccess && outputFile.exists()) {
                return outputFile.readBytes()
            }
            return null
        } catch (e: Exception) {
            println("Compilation to binary error: ${e.message}")
            return null
        } finally {
            tempDir?.deleteRecursively()
        }
    }

    override fun runCheckerTest(checkerSource: String, language: String, input: String, output: String, expected: String): String {
        var tempDir: File? = null
        try {
            tempDir = createTempDir("checker_test_")

            // Write checker source
            val sourceFile = when (language) {
                "cpp" -> File(tempDir, "checker.cpp")
                "python" -> File(tempDir, "checker.py")
                "java" -> File(tempDir, "Checker.java")
                else -> throw IllegalArgumentException("Unsupported language: $language")
            }
            sourceFile.writeText(checkerSource)

            // Write test files
            val inputFile = File(tempDir, "input.txt").apply { writeText(input) }
            val outputFile = File(tempDir, "output.txt").apply { writeText(output) }
            val expectedFile = File(tempDir, "expected.txt").apply { writeText(expected) }

            // Compile if needed
            val binaryFile = when (language) {
                "cpp" -> {
                    val bin = File(tempDir, "checker.out")
                    val compileSuccess = compileProgram(sourceFile, bin, true, tempDir) // testlib needed for checkers
                    if (!compileSuccess) return "CRASHED"
                    bin.setExecutable(true)
                    bin
                }
                "python" -> sourceFile // Python doesn't need compilation
                "java" -> {
                    // Compile Java
                    val compileProcess = ProcessBuilder("javac", sourceFile.absolutePath)
                        .directory(tempDir)
                        .redirectErrorStream(true)
                        .start()
                    val compileSuccess = compileProcess.waitFor(10, TimeUnit.SECONDS)
                    if (!compileSuccess || compileProcess.exitValue() != 0) return "CRASHED"
                    File(tempDir, "Checker.class")
                }
                else -> throw IllegalArgumentException("Unsupported language: $language")
            }

            // Run the checker
            val command = when (language) {
                "cpp" -> listOf(binaryFile.absolutePath, inputFile.absolutePath, outputFile.absolutePath, expectedFile.absolutePath)
                "python" -> listOf("python3", sourceFile.absolutePath, inputFile.absolutePath, outputFile.absolutePath, expectedFile.absolutePath)
                "java" -> listOf("java", "-cp", tempDir.absolutePath, "Checker", inputFile.absolutePath, outputFile.absolutePath, expectedFile.absolutePath)
                else -> throw IllegalArgumentException("Unsupported language: $language")
            }

            val runProcess = ProcessBuilder(command)
                .directory(tempDir)
                .redirectErrorStream(true)
                .start()

            val runSuccess = runProcess.waitFor(5000, TimeUnit.MILLISECONDS)

            if (!runSuccess) {
                runProcess.destroyForcibly()
                return "CRASHED"
            }

            val exitCode = runProcess.exitValue()
            val outputContent = runProcess.inputStream.bufferedReader().readText()

            // Map exit codes to verdicts (common convention)
            return when (exitCode) {
                0 -> "OK"
                1 -> "WRONG_ANSWER"
                2 -> "PRESENTATION_ERROR"
                else -> "CRASHED"
            }

        } catch (e: Exception) {
            return "CRASHED"
        } finally {
            tempDir?.deleteRecursively()
        }
    }

    override fun runValidatorTest(validatorSource: String, language: String, input: String): String {
        // Stub implementation: always return VALID
        return "VALID"
    }

    private fun copyTestlibToTemp(tempDir: File): File {
        val resource = javaClass.classLoader.getResource("testlib.h")
            ?: throw IllegalStateException("testlib.h not found in resources")

        val testlibFile = File(tempDir, "testlib.h")
        resource.openStream().use { input ->
            testlibFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return testlibFile
    }
}