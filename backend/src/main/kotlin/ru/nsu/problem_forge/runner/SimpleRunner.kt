package ru.nsu.problem_forge.runner

import org.springframework.stereotype.Component
import java.io.*
import java.util.concurrent.TimeUnit
import ru.nsu.problem_forge.runner.Runner.RunOutput
import ru.nsu.problem_forge.runner.Runner.RunInput
import ru.nsu.problem_forge.runner.Runner.RunStatus
@Component
class SimpleRunner : Runner {

    override fun run(programSource: String, runs: List<RunInput>): List<RunOutput> {
        return runs.map { runInput ->
            var tempDir: File? = null
            try {
                // Create unique temp directory for this run
                tempDir = createTempDir("runner_")
                println("Created temp directory: ${tempDir.absolutePath}")

                // Write source to temporary file in our unique directory
                val sourceFile = File(tempDir, "program.cpp")
                sourceFile.writeText(programSource)
                println("Source file created: ${sourceFile.absolutePath}")

                val compileCommand = mutableListOf("g++", "-std=c++17", "-O2")

                if (runInput.testlibNeeded) {
                    // Copy testlib.h to our unique directory
                    val testlibFile = copyTestlibToTemp(tempDir)
                    compileCommand.add("-I${tempDir.absolutePath}")
                    compileCommand.add(testlibFile.absolutePath)
                    println("Testlib included: ${testlibFile.absolutePath}")
                }

                compileCommand.add(sourceFile.absolutePath)
                compileCommand.add("-o")
                val outputFile = File(tempDir, "program.out")
                compileCommand.add(outputFile.absolutePath)

                println("Compile command: ${compileCommand.joinToString(" ")}")

                // Compile
                val compileProcess = ProcessBuilder(compileCommand)
                    .directory(tempDir) // Set working directory
                    .redirectErrorStream(true)
                    .start()

                val compileSuccess = compileProcess.waitFor(10, TimeUnit.SECONDS)
                val compileOutput = compileProcess.inputStream.bufferedReader().readText()

                println("Compilation exit code: ${compileProcess.exitValue()}")
                println("Compilation output: $compileOutput")

                if (!compileSuccess || compileProcess.exitValue() != 0) {
                    return@map RunOutput(RunStatus.COMPILE_ERROR, "Compilation failed: $compileOutput")
                }

                // Build command with arguments
                val command = mutableListOf(outputFile.absolutePath)
                command.addAll(runInput.args)

                println("Run command: ${command.joinToString(" ")}")

                // Run with input and arguments
                val runProcess = ProcessBuilder(command)
                    .directory(tempDir) // Set working directory
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
                println("Run exit code: ${runProcess.exitValue()}")
                println("Run output: $runOutput")

                if (runProcess.exitValue() != 0) {
                    return@map RunOutput(RunStatus.RUNTIME_ERROR, "Runtime error: $runOutput")
                }

                RunOutput(RunStatus.SUCCESS, runOutput)
            } catch (e: Exception) {
                println("Exception during execution: ${e.message}")
                e.printStackTrace()
                RunOutput(RunStatus.RUNTIME_ERROR, "Error: ${e.message}")
            } finally {
                // Cleanup temp directory
                tempDir?.deleteRecursively()
                println("Cleaned up temp directory: ${tempDir?.absolutePath}")
            }
        }
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