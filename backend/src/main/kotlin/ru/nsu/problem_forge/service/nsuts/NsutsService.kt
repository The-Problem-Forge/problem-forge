package ru.nsu.problem_forge.service.nsuts

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.springframework.stereotype.Service
import ru.nsu.problem_forge.dto.nsuts.*
import java.io.IOException
import java.util.concurrent.TimeUnit

private val objectMapper = ObjectMapper().registerKotlinModule()

@Service
class NsutsUploadService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        })
        .build()

    private val baseUrl = "http://91.218.244.146/nsuts-new"

    @Synchronized
    fun uploadProblemPackage(
        zipBytes: ByteArray,
        problemId: Long,
        timeLimit: Long,
        memoryLimit: Long,
        solutions: List<String>,
        deleteProblem: Boolean = true
    ): List<InvocationDto> {
        try {
            // Step 1: Login
            val sessionCookie = login()

            // Step 2: Enter olympiad
            enterOlympiad(sessionCookie)

            // Step 3: Enter tour
            enterTour(sessionCookie)

            // Step 4: Create new problem
            createProblem(sessionCookie)

            // Step 5: Get latest task ID
            val taskId = getLatestTaskId(sessionCookie)

            // Step 6: Update task limits and title
            updateTask(sessionCookie, taskId, problemId, timeLimit, memoryLimit)

            // Step 7: Upload package
            val filename = "archive.zip"
            val uploadResult = uploadPackage(sessionCookie, taskId, zipBytes, filename)

            // Step 8: Submit solutions
            if (solutions.isNotEmpty()) {
                solutions.forEachIndexed { index, solution ->
                    submitSolution(sessionCookie, taskId, solution, "sgcc", index + 1)
                    // Wait a bit between submissions to avoid rate limiting
                    Thread.sleep(3000)
                }

                Thread.sleep(5000)
            }

            // Step 9: Gather submission results
            val results = gatherSubmissionResults(sessionCookie, solutions.size, taskId.toString())

            if (deleteProblem) {
                deleteTask(sessionCookie, taskId.toString())
            }

            // Step 8: Logout
            logout(sessionCookie)

            return results
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload package to NSUTS: ${e.message}", e)
        }
    }
    private fun login(): String {
        val loginRequest = NsutsLoginRequest(
            email = "ProblemForgeTester@mail.ru",
            password = "ProblemForgeTester"
        )

        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            "{\"email\":\"${loginRequest.email}\",\"password\":\"${loginRequest.password}\",\"method\":\"${loginRequest.method}\"}"
        )

        val request = Request.Builder()
            .url("$baseUrl/api/login")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Login failed: ${response.code} ${response.message}")
            }

            // Extract session cookie
            val cookies = response.headers("set-cookie")
            val sessionCookie = cookies.find { it.contains("CGISESSID") }
                ?: throw IOException("No session cookie received")

            return sessionCookie.substringBefore(";")
        }
    }

    private fun enterOlympiad(sessionCookie: String) {
        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            "{\"olympiad\":\"58\"}"
        )

        val request = Request.Builder()
            .url("$baseUrl/api/olympiads/enter")
            .post(requestBody)
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Enter olympiad failed: ${response.code}")
            }
        }
    }

    private fun enterTour(sessionCookie: String) {
        val request = Request.Builder()
            .url("$baseUrl/api/tours/enter?tour=11233")
            .get()
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Enter tour failed: ${response.code}")
            }
        }
    }

    private fun createProblem(sessionCookie: String) {
        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            "{}"
        )

        val request = Request.Builder()
            .url("$baseUrl/api/jury/tasks/create")
            .post(requestBody)
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Create problem failed: ${response.code}")
            }
        }
    }

    private fun deleteTask(sessionCookie: String, taskId: String) {
        val request = Request.Builder()
            .url("$baseUrl/api/jury/tasks/delete?task=$taskId")
            .delete()
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Delete task failed: ${response.code}")
            }
            println("Task $taskId deleted successfully")
        }
    }

    private fun getLatestTaskId(sessionCookie: String): Long {
        val request = Request.Builder()
            .url("$baseUrl/api/jury/tasks/list")
            .get()
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Get task list failed: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response from task list")

            // Parse JSON manually or use Jackson
            val tasks = parseTaskList(responseBody)
            return tasks.maxByOrNull { it.id }?.id
                ?: throw IOException("No tasks found in response")
        }
    }

    private fun getAllTasks(sessionCookie: String): List<NsutsTask> {
        val request = Request.Builder()
            .url("$baseUrl/api/jury/tasks/list")
            .get()
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Get task list failed: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response from task list")

            return parseTaskList(responseBody)
        }
    }
    private fun updateTask(sessionCookie: String, taskId: Long, problemId: Long, timeLimit: Long, memoryLimit: Long) {
        // Step 1: Get all tasks
        val allTasks = getAllTasks(sessionCookie)

        // Step 2: Find and update the specific task
        val updatedTasks = allTasks.map { task ->
            if (task.id == taskId) {
                task.copy(
                    title = "problem_forge_${problemId}_${java.time.LocalDateTime.now()}",
                    timeLimit = timeLimit / 1000,
                    memoryLimit = memoryLimit * 1024, // Convert to KB
                    jvmTimeLimit = timeLimit / 1000,
                    jvmMemoryLimit = memoryLimit * 1024
                )
            } else {
                task
            }
        }

        // Step 3: Send the entire tasks array
        val updateJson = """
        {
            "tasks": [
                ${
            updatedTasks.joinToString(",\n") { task ->
                """
                    {
                        "id": ${task.id},
                        "tourId": ${task.tourId},
                        "title": "${task.title}",
                        "model": "${task.model}",
                        "inputFile": "${task.inputFile}",
                        "outputFile": "${task.outputFile}",
                        "timeLimit": ${task.timeLimit},
                        "memoryLimit": ${task.memoryLimit},
                        "jvmTimeLimit": ${task.jvmTimeLimit},
                        "jvmMemoryLimit": ${task.jvmMemoryLimit},
                        "position": ${task.position},
                        "testsMd5": ${if (task.testsMd5 != null) "\"${task.testsMd5}\"" else "null"},
                        "rateTotalLimit": ${task.rateTotalLimit ?: "null"},
                        "rateTimeInterval": ${task.rateTimeInterval ?: "null"},
                        "rateSubmitsInTime": ${task.rateSubmitsInTime ?: "null"}
                    }
                    """.trimIndent()
            }
        }
            ]
        }
    """.trimIndent()

        val requestBody = RequestBody.create(
            "application/json".toMediaType(),
            updateJson
        )

        val request = Request.Builder()
            .url("$baseUrl/api/jury/tasks/save")
            .put(requestBody)
            .header("Cookie", sessionCookie)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Update task failed: ${response.code} ${response.message}")
            }
            println("Task $taskId updated successfully")
        }
    }

    private fun submitSolution(
        sessionCookie: String,
        taskId: Long,
        sourceCode: String,
        langId: String,
        solutionNumber: Int
    ) {
        val boundary = "----WebKitFormBoundary${System.currentTimeMillis().toString(16).toUpperCase()}"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("langId", langId) // "sgcc" for C++
            .addFormDataPart("taskId", taskId.toString())
            .addFormDataPart("sourceText", sourceCode)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/api/submit/do_submit")
            .post(requestBody)
            .header("Cookie", sessionCookie)
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Origin", "http://91.218.244.146")
            .header("Referer", "$baseUrl/submit")
            .header(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36"
            )
            .build()

        client.newCall(request).execute().use { response ->
            println("Solution $solutionNumber submission response code: ${response.code}")
            val responseBody = response.body?.string()
            println("Solution $solutionNumber submission response: $responseBody")

            if (!response.isSuccessful) {
                throw IOException("Solution $solutionNumber submission failed: ${response.code} ${response.message}")
            }

            println("Solution $solutionNumber submitted successfully to task ID: $taskId")
        }
    }

    private fun logout(sessionCookie: String) {
        val request = Request.Builder()
            .url("$baseUrl/api/logout")
            .post(RequestBody.create(null, ""))
            .header("Cookie", sessionCookie)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Logout failed: ${response.code}, but continuing...")
            } else {
                println("Logged out successfully")
            }
        }
    }

    private fun uploadPackage(sessionCookie: String, taskId: Long, zipBytes: ByteArray, filename: String): String {
        val boundary = "----WebKitFormBoundary${System.currentTimeMillis().toString(16).toUpperCase()}"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "test0",  // Must be "test0" not "file"
                filename,
                RequestBody.create("application/zip".toMediaType(), zipBytes)
            )
            .addFormDataPart("quantity", "1")  // Tell server we're sending 1 file
            .build()

        val request = Request.Builder()
            .url("$baseUrl/admin_tests.cgi?taskid=$taskId&posted=1")
            .post(requestBody)
            .header("Cookie", sessionCookie)
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
            )
            .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Cache-Control", "max-age=0")
            .header("Origin", "http://91.218.244.146")
            .header("Referer", "$baseUrl/admin_tests.cgi?taskid=$taskId")
            .header("Upgrade-Insecure-Requests", "1")
            .header(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36"
            )
            .build()

        client.newCall(request).execute().use { response ->
            println("Upload response code: ${response.code}")
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                throw IOException("Upload package failed: ${response.code} ${response.message}")
            }

            // Check if upload was successful by looking for error messages in response
            if (responseBody?.contains("Архив корректен") == true ||
                responseBody?.contains("Скачать весь архив") == true
            ) {
                return "Package uploaded successfully to task ID: $taskId"
            } else {
                throw IOException("Upload might have failed. Check server response.")
            }
        }
    }

    private fun parseTaskList(json: String): List<NsutsTask> {
        // Simple JSON parsing - you might want to use Jackson/ObjectMapper for better parsing
        try {
            val tasksArray = json.substringAfter("\"tasks\":[").substringBefore("]")
            val tasks = mutableListOf<NsutsTask>()

            // Simple parsing logic - replace with proper JSON parsing
            var currentIndex = 0
            while (currentIndex < tasksArray.length) {
                val taskStart = tasksArray.indexOf("{", currentIndex)
                if (taskStart == -1) break

                val taskEnd = tasksArray.indexOf("}", taskStart)
                if (taskEnd == -1) break

                val taskJson = tasksArray.substring(taskStart, taskEnd + 1)
                val task = parseTask(taskJson)
                tasks.add(task)

                currentIndex = taskEnd + 1
            }

            return tasks
        } catch (e: Exception) {
            throw IOException("Failed to parse task list: ${e.message}")
        }
    }

    private fun parseTask(taskJson: String): NsutsTask {
        // Extract fields from JSON string - replace with proper JSON parsing
        fun extractField(field: String): String {
            val pattern = "\"$field\":\"([^\"]+)\"".toRegex()
            val match = pattern.find(taskJson)
            return match?.groupValues?.get(1) ?: ""
        }

        fun extractLongField(field: String): Long? {
            val pattern = "\"$field\":(\\d+)".toRegex()
            val match = pattern.find(taskJson)
            return match?.groupValues?.get(1)?.toLongOrNull()
        }

        fun extractIntField(field: String): Int? {
            val pattern = "\"$field\":(\\d+)".toRegex()
            val match = pattern.find(taskJson)
            return match?.groupValues?.get(1)?.toIntOrNull()
        }

        return NsutsTask(
            id = extractLongField("id") ?: 0L,
            tourId = extractLongField("tourId") ?: 0L,
            title = extractField("title"),
            model = extractField("model"),
            inputFile = extractField("inputFile"),
            outputFile = extractField("outputFile"),
            timeLimit = extractLongField("timeLimit") ?: 0L,
            memoryLimit = extractLongField("memoryLimit") ?: 0L,
            position = extractLongField("position") ?: 0L,
            jvmTimeLimit = extractLongField("jvmTimeLimit") ?: 0L,
            jvmMemoryLimit = extractLongField("jvmMemoryLimit") ?: 0L,
            rateTotalLimit = extractIntField("rateTotalLimit"),
            rateTimeInterval = extractIntField("rateTimeInterval"),
            rateSubmitsInTime = extractIntField("rateSubmitsInTime"),
            testsMd5 = extractField("testsMd5").takeIf { it.isNotEmpty() }
        )
    }
    private fun gatherSubmissionResults(
        sessionCookie: String,
        solutionsCount: Int,
        taskId: String
    ): List<InvocationDto> {
        val maxRetries = 100 // Maximum number of retries to prevent infinite loop
        var retries: Long = 0

        while (retries < maxRetries) {
            val submissions = getSubmissions(sessionCookie, taskId)

            // Take the latest submissions for our task
            val ourSubmissions = submissions
                .filter { it.taskId == taskId }
                .sortedByDescending { it.id.toLong() }
                .take(solutionsCount)
                .reversed() // Reverse to match submission order

            println("Found ${ourSubmissions.size} submissions for task $taskId")

            // Check if all submissions have non-null res fields
            val allProcessed = ourSubmissions.all { it.res != null }

            if (allProcessed) {
                println("All submissions processed, returning results")
                return ourSubmissions.mapIndexed { index, submission ->
                    parseSubmissionToInvocation(index.toLong() + 1, submission)
                }
            } else {
                val pendingCount = ourSubmissions.count { it.res == null }
                println("$pendingCount submissions still pending, waiting 2 seconds... (attempt ${retries + 1}/$maxRetries)")
                Thread.sleep(5000 + retries * 3000)
                retries++
            }
        }

        throw IOException("Failed to get processed submissions after $maxRetries attempts")
    }

    private fun getSubmissions(sessionCookie: String, taskId: String): List<Submission> {
        val request = Request.Builder()
            .url("$baseUrl/api/queue/submissions?limit=50&last=false&checker=true")
            .get()
            .header("Cookie", sessionCookie)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Get submissions failed: ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response from submissions")

            return parseSubmissions(responseBody)
        }
    }

    private fun parseSubmissions(json: String): List<Submission> {
        return try {
            val response = objectMapper.readValue(json, SubmissionResponse::class.java)
            response.submissions
        } catch (e: Exception) {
            throw IOException("Failed to parse submissions: ${e.message}")
        }
    }
    private fun parseSubmissionToInvocation(solutionId: Long, submission: Submission): InvocationDto {
        val testResults = mutableListOf<InvocationTestDto>()

        // Parse result codes only if res is not null and not empty
        submission.res?.forEachIndexed { index, char ->
            val resultCode = char.toString()
            val statusInfo = StatusMapping.getStatusInfo(resultCode)

            // Parse time and memory for this test
            val (timeMs, memoryKb) = parseTimeAndMemory(submission.time_and_memory, index + 1)

            testResults.add(
                InvocationTestDto(
                    testNumber = index + 1,
                    resultCode = resultCode,
                    resultDescription = statusInfo.english,
                    usedTimeMs = timeMs,
                    usedMemoryKb = memoryKb
                )
            )
        }

        return InvocationDto(
            solutionId = solutionId,
            testResults = testResults
        )
    }
    private fun parseTimeAndMemory(timeAndMemoryJson: String?, testNumber: Int): Pair<Int, Int> {
        if (timeAndMemoryJson == null) {
            return Pair(0, 0)
        }

        return try {
            val jsonObject = objectMapper.readTree(timeAndMemoryJson)
            val testData = jsonObject.get(testNumber.toString())
            if (testData != null && testData.isObject) {
                val timeMs = testData.get("t")?.asInt() ?: 0
                val memoryKb = testData.get("m")?.asInt() ?: 0
                Pair(timeMs, memoryKb)
            } else {
                Pair(0, 0)
            }
        } catch (e: Exception) {
            println("Failed to parse time_and_memory for test $testNumber: ${e.message}")
            Pair(0, 0)
        }
    }
}