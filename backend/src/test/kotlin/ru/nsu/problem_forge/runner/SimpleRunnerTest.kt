package ru.nsu.problem_forge.runner

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.nsu.problem_forge.runner.Runner.RunInput
import ru.nsu.problem_forge.runner.Runner.RunStatus
import java.io.File

class SimpleRunnerTest {

    private lateinit var simpleRunner: SimpleRunner

    @TempDir
    lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        simpleRunner = SimpleRunner()
    }

    @Test
    fun `run should return success for valid C++ program`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main() {
                int a, b;
                cin >> a >> b;
                cout << a + b;
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "5 3")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(2, results.size)
        assertEquals(RunStatus.SUCCESS, results[0].status)
        assertEquals("8", results[0].outputContent.trim())
    }

    @Test
    fun `run should handle multiple inputs`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main() {
                int n;
                cin >> n;
                cout << n * 2;
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "5"),
            RunInput(inputContent = "10"),
            RunInput(inputContent = "15")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(3, results.size)
        results.forEach { result ->
            assertEquals(RunStatus.SUCCESS, result.status)
        }
        assertEquals("10", results[0].outputContent.trim())
        assertEquals("20", results[1].outputContent.trim())
        assertEquals("30", results[2].outputContent.trim())
    }

    @Test
    fun `run should return compile error for invalid C++ program`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main() {
                syntax error here
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "test")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.COMPILE_ERROR, results[0].status)
        assertTrue(results[0].outputContent.contains("Compilation failed"))
    }

    @Test
    fun `run should return runtime error for program that crashes`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main() {
                int* ptr = nullptr;
                *ptr = 5; // Segmentation fault
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.RUNTIME_ERROR, results[0].status)
    }

    @Test
    fun `run should handle time limit exceeded`() {
        // Given
        val programSource = """
            #include <iostream>
            #include <thread>
            #include <chrono>
            using namespace std;
            
            int main() {
                // Infinite loop
                while(true) {
                    // Do nothing
                }
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.RUNTIME_ERROR, results[0].status)
        assertTrue(results[0].outputContent.contains("Time limit exceeded"))
    }

    @Test
    fun `run should handle command line arguments`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main(int argc, char* argv[]) {
                if (argc > 1) {
                    cout << "Arg: " << argv[1];
                } else {
                    cout << "No args";
                }
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(args = listOf("hello"), inputContent = ""),
            RunInput(args = emptyList(), inputContent = "")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(2, results.size)
        assertEquals(RunStatus.SUCCESS, results[0].status)
        assertEquals("Arg: hello", results[0].outputContent.trim())
        assertEquals("No args", results[1].outputContent.trim())
    }

    @Test
    fun `run should handle testlibNeeded flag`() {
        // Given
        val programSource = """
#include "testlib.h" 
using namespace std;
int main(int argc, char *argv[]) {
 
    registerGen(argc, argv, 1);
 
    int n = atoi(argv[1]);
    int q = atoi(argv[2]);
    int t = atoi(argv[3]);
    int qtype = atoi(argv[4]);
    int maxc = atoi(argv[5]);
 
    vector<int> p(n);
 
    /* setup parents for vertices 1..n-1 */
    for(int i = 1; i < n; i++) {
        p[i] = rnd.wnext(i, t);
    }
 
    printf("%d\n", n);
 
    /* shuffle vertices 1..n-1 */
    vector<int> perm(n);
    for(int i = 0; i < n; i++) {
        perm[i] = i;
    }
    shuffle(perm.begin() + 1, perm.end());
 
    /* put edges considering shuffled vertices */
    vector<pair<int,int> > edges;
    for (int i = 1; i < n; i++)
        if (rnd.next(2))
            edges.push_back(make_pair(perm[i], perm[p[i]]));
        else
            edges.push_back(make_pair(perm[p[i]], perm[i]));
 
    /* shuffle edges */
    shuffle(edges.begin(), edges.end());
 
    for (int i = 0; i + 1 < n; i++)
        printf("%d %d\n", edges[i].first + 1, edges[i].second + 1);
 
    printf("%d\n", q);
    for(int i = 0; i < q - 1; i++) {
        int type = rnd.wnext(2, qtype);
        if (type == 0) {
            printf("? %d\n", rnd.next(n) + 1);
        } else {
            int x = rnd.next(maxc) + 1; 
            printf("+ %d %d\n", rnd.next(n) + 1, x);
        }
    }
    
    printf("? %d\n", rnd.next(n) + 1);
 
    return 0;
}
        """.trimIndent()

        val runs = listOf(
            RunInput(testlibNeeded = true, inputContent = "", args = listOf("100", "100", "0", "0", "5"))
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.SUCCESS, results[0].status)
        assertEquals(relaxation_tree_sample_test.trim(), results[0].outputContent.trim())
    }

    @Test
    fun `run should handle testlibNeeded flag 2`() {
        // Given
        val programSource = """
            #include "testlib.h"
            using namespace std;
            
            int main() {
                cout << "Testlib not actually used in this test";
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(testlibNeeded = false, inputContent = "")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.COMPILE_ERROR, results[0].status)
    }

    @Test
    fun `run should handle empty input`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main() {
                cout << "Hello, World!";
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.SUCCESS, results[0].status)
        assertEquals("Hello, World!", results[0].outputContent.trim())
    }

    @Test
    fun `run should handle large input output`() {
        // Given
        val programSource = """
            #include <iostream>
            #include <string>
            using namespace std;
            
            int main() {
                string line;
                while (getline(cin, line)) {
                    cout << line.length() << " ";
                }
                return 0;
            }
        """.trimIndent()

        val largeInput = "A".repeat(1000) + "\n" + "B".repeat(500)
        val runs = listOf(
            RunInput(inputContent = largeInput)
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.SUCCESS, results[0].status)
        assertTrue(results[0].outputContent.contains("1000"))
        assertTrue(results[0].outputContent.contains("500"))
    }

    @Test
    fun `run should handle program that reads until EOF`() {
        // Given
        val programSource = """
            #include <iostream>
            using namespace std;
            
            int main() {
                int sum = 0, num;
                while (cin >> num) {
                    sum += num;
                }
                cout << sum;
                return 0;
            }
        """.trimIndent()

        val runs = listOf(
            RunInput(inputContent = "1 2 3 4 5")
        )

        // When
        val results = simpleRunner.run(programSource, runs)

        // Then
        assertEquals(1, results.size)
        assertEquals(RunStatus.SUCCESS, results[0].status)
        assertEquals("15", results[0].outputContent.trim())
    }

    private val relaxation_tree_sample_test = """
100
2 40
40 84
89 37
17 33
16 56
16 92
85 13
15 95
28 4
14 31
2 73
34 65
83 68
61 85
4 88
51 53
42 77
15 98
58 88
51 39
57 6
58 11
85 37
55 44
43 21
81 99
1 14
98 34
34 70
41 19
22 3
36 12
56 100
55 41
30 27
52 86
43 74
96 99
7 40
69 52
70 26
74 41
8 91
4 49
75 34
46 85
16 37
72 11
43 99
18 65
94 67
14 63
24 50
67 81
61 62
55 91
64 100
51 75
68 58
1 93
99 79
35 68
12 6
74 100
29 67
80 34
20 79
99 9
48 79
87 65
25 34
38 69
56 82
21 32
98 90
78 39
68 60
10 81
93 34
27 52
1 43
23 28
76 65
27 99
56 6
24 6
51 59
65 54
49 22
77 69
43 45
47 58
1 22
5 100
97 94
71 90
66 70
7 65
17 95
100
+ 6 3
? 13
+ 28 1
+ 14 3
? 31
? 40
+ 34 3
+ 25 2
+ 60 4
? 87
+ 86 3
? 76
+ 8 5
+ 9 5
+ 50 3
+ 82 5
? 49
+ 81 5
? 41
+ 93 5
? 45
? 37
? 54
+ 46 3
? 15
? 63
+ 44 4
+ 98 3
? 2
? 48
+ 95 2
? 49
? 28
? 36
+ 80 5
+ 37 3
+ 65 1
+ 99 3
+ 94 4
+ 18 5
+ 32 1
+ 94 5
+ 3 2
+ 19 3
+ 56 2
+ 73 4
? 91
+ 96 2
+ 30 1
? 96
? 20
+ 39 4
? 54
? 11
+ 44 4
+ 28 5
? 62
+ 12 2
? 59
? 56
? 23
? 46
+ 50 1
+ 40 3
+ 84 1
+ 5 5
+ 84 4
+ 40 1
? 100
? 10
+ 45 4
+ 96 2
? 20
+ 57 5
+ 92 4
+ 36 3
+ 90 3
? 51
? 100
+ 71 4
+ 13 3
+ 23 2
? 64
+ 12 4
+ 75 5
? 47
? 85
? 57
+ 28 3
? 43
? 49
+ 36 1
? 65
? 2
? 33
+ 89 2
? 78
? 66
+ 65 1
? 2
        """
}