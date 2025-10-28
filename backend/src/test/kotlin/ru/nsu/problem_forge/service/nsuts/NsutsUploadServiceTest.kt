package ru.nsu.problem_forge.service.nsuts

import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.nsu.problem_forge.dto.problem.PreviewStatus
import ru.nsu.problem_forge.dto.problem.TestPreviewStatus
import ru.nsu.problem_forge.runner.Runner
import ru.nsu.problem_forge.service.problem.ProblemTestsService
import java.util.Optional

class NsutsUploadServiceTest {

    private lateinit var nsutsUploadService: NsutsUploadService

    @BeforeEach
    fun setUp() {
        nsutsUploadService = NsutsUploadService()
    }

    @Test
    fun `generatePreview should handle generator failures gracefully`() {
        val array = readZipFromResources("garlic.zip")
        nsutsUploadService.uploadProblemPackage(array, 10, 2000, 512, listOf(
            """
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
            """.trimIndent(),
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
            """.trimIndent()))
    }

    fun readZipFromResources(filename: String = "file.zip"): ByteArray {
        return this::class.java.classLoader.getResourceAsStream(filename)
            ?.readBytes()
            ?: throw IllegalArgumentException("File $filename not found in resources")
    }
}