package ru.nsu.problem_forge.service.nsuts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.nsu.problem_forge.dto.problem.InvocationResponseTestDto

class NsutsTestingServiceTest {

    private lateinit var nsutsUploadService: NsutsTestingService

    @BeforeEach
    fun setUp() {
        nsutsUploadService = NsutsTestingService()
    }

    @Test
    fun `nsuts integration test`() {
        val array = readZipFromResources("garlic.zip")
        val results = nsutsUploadService.runSolutions(
            array, 10, 2000, 512,
            listOf(acceptedSolutionSource, wrongAnswerSolutionSource)
        )

        assertEquals(results.size, 2)

        assertEquals(results[0].solutionId, 1)
        assertEquals(results[0].testResults.size, 4)
        assertEquals(results[0].testResults[0].resultCode, "A")
        assertEquals(results[0].testResults[0].resultCode, "A")

        assertEquals(results[1].testResults.size, 4)
        assertEquals(results[1].testResults[0].resultCode, "A")
        assertEquals(results[1].testResults[0].testNumber, 1)
        assertEquals(results[1].testResults[3].resultCode, "W")
    }

    val acceptedSolutionSource = """
#include<bits/stdc++.h>
 
using namespace std;
 
int main() {
 
    freopen("input.txt", "r", stdin);
    freopen("output.txt", "w", stdout);
 
    int n, s;
    cin >> n >> s;
 
    vector<int> a(2 * n);
    for(int i = 0; i < n; i++) {
        cin >> a[i];
        a[n + i] = a[i];
    }
 
 
    // [l; r) - two pointers
    int r = 0;
    int ans = 0, summ = 0;
 
    for(int l = 0; l < n; l++) {
        if (r < l) {
            r = l;
            summ = 0;
        }
 
        while (r - l <= n && summ + a[r] <= s) {
            summ += a[r];
            r++;
        }
 
        ans = max(ans, summ);
        summ -= a[l];
    }
 
    cout << ans << endl;
 
    return 0;
}
            """.trimIndent()

    val wrongAnswerSolutionSource =
        """
#include<bits/stdc++.h>
 
using namespace std;
 
int main() {
 
    freopen("input.txt", "r", stdin);
    freopen("output.txt", "w", stdout);
    
    int n, s;
    cin >> n >> s;
    
    vector<int> a(n);
    for(int i = 0; i < n; i++) {
        cin >> a[i];
    }
    
    int wait = 100000000;
    while(wait--) {
    }
    
    // [l; r) - two pointers
    int r = 0;
    int ans = 0, summ = 0;
 
    for(int l = 0; l < n; l++) {
 
        while (r < n && summ + a[r] <= s) {
            summ += a[r];
            r++;
        }
 
        ans = max(ans, summ);
        summ -= a[l];
    }
 
    cout << ans << endl;
 
    return 0;
}
            """.trimIndent()

    fun readZipFromResources(filename: String = "file.zip"): ByteArray {
        return javaClass.classLoader.getResourceAsStream(filename)
            ?.readBytes()
            ?: throw IllegalArgumentException("File $filename not found in resources")
    }
}