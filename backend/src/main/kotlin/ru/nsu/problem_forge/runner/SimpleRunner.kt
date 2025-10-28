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