import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_URL || "http://localhost:8080/api";

const UI_TEST = process.env.REACT_APP_UI_TEST === "true";

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Mock data for UI testing
const mockData = {
  // Existing mock data...
  // Add generators storage for UI test mode
  generators: {},
  contests: [
    {
      id: "1",
      name: "Sample Contest 1",
      description: "A sample contest for testing",
      location: "Online",
      contestDate: "2023-06-15T10:00:00Z",
      role: "Owner",
      createdAt: "2023-01-01T00:00:00Z",
      updatedAt: "2023-01-01T00:00:00Z",
    },
    {
      id: "2",
      name: "Sample Contest 2",
      description: "Another sample contest",
      location: "University Campus",
      contestDate: "2023-07-20T09:00:00Z",
      role: "Editor",
      createdAt: "2023-01-02T00:00:00Z",
      updatedAt: "2023-01-02T00:00:00Z",
    },
  ],
  problems: [
    {
      id: "1",
      title: "A+B Problem",
      createdAt: "2023-01-01T00:00:00Z",
      modifiedAt: "2023-01-01T00:00:00Z",
    },
    {
      id: "2",
      title: "Hello World",
      createdAt: "2023-01-02T00:00:00Z",
      modifiedAt: "2023-01-02T00:00:00Z",
    },
  ],

  allTasks: [
    {
      id: "1",
      title: "Task A",
      description: "First task in contest 1",
      contestId: "1",
    },
    {
      id: "2",
      title: "Task B",
      description: "Second task in contest 1",
      contestId: "1",
    },
    {
      id: "3",
      title: "Task C",
      description: "First task in contest 2",
      contestId: "2",
    },
  ],
  general: {
    1: {
      name: "Task A",
      inFile: "input.txt",
      outFile: "output.txt",
      timeLimitMs: 1000,
      memoryLimitMb: 256,
    },
    2: {
      name: "Task B",
      inFile: "input.txt",
      outFile: "output.txt",
      timeLimitMs: 2000,
      memoryLimitMb: 512,
    },
    3: {
      name: "Task C",
      inFile: "input.txt",
      outFile: "output.txt",
      timeLimitMs: 1500,
      memoryLimitMb: 256,
    },
  },
  statement: {
    1: {
      legend: "Solve the equation $x^2 + 2x + 1 = 0$.",
      inputFormat: "First line: $N$",
      outputFormat: "Single integer",
      notes: "Note that $x = -1$ is the solution.",
      tutorial:
        "Use the quadratic formula: $x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}$",
    },
    2: {
      legend: "Another problem with $\\sum_{i=1}^n i = \\frac{n(n+1)}{2}$.",
      inputFormat: "Input format",
      outputFormat: "Output format",
      notes: "",
      tutorial: "",
    },
    3: {
      legend: "Third problem: $\\int_0^1 x^2 dx = \\frac{1}{3}$.",
      inputFormat: "Format",
      outputFormat: "Result",
      notes: "Notes",
      tutorial: "Tutorial",
    },
  },
  checker: {
    1: {
      source:
        "#include <bits/stdc++.h>\nusing namespace std;\nint main() { /* checker code */ }",
      language: "cpp",
      tests: [
        { id: "1", input: "1 2", output: "3", expected: "3", verdict: "OK" },
        { id: "2", input: "2 3", output: "5", expected: "5", verdict: "OK" },
      ],
      runResults: { 1: "OK", 2: "OK" },
    },
    2: {
      source: "def checker():\n    pass",
      language: "python",
      tests: [
        { id: "3", input: "1", output: "1", expected: "1", verdict: "WA" },
      ],
      runResults: { 3: "WA" },
    },
    3: {
      source: "public class Checker { /* java checker */ }",
      language: "java",
      tests: [],
      runResults: {},
    },
  },
  validator: {
    1: {
      source:
        "#include <bits/stdc++.h>\nusing namespace std;\nint main() { /* validator code */ }",
      language: "cpp",
      tests: [
        { id: "1", input: "1 2", output: "3", expected: "3", verdict: "OK" },
      ],
      runResults: { 1: "OK" },
    },
    2: {
      source: "def validator():\n    pass",
      language: "python",
      tests: [
        { id: "2", input: "2", output: "2", expected: "2", verdict: "OK" },
      ],
      runResults: { 2: "OK" },
    },
    3: {
      source: "public class Validator { /* java validator */ }",
      language: "java",
      tests: [],
      runResults: {},
    },
  },
  tests: {
    1: [
      {
        id: "1",
        input: "1 2",
        description: "Test 1",
        points: 1,
        sample: false,
      },
      { id: "2", input: "2 3", description: "Test 2", points: 2, sample: true },
    ],
    2: [
      { id: "3", input: "1", description: "Test 3", points: 1, sample: false },
    ],
    3: [],
  },
  solutions: {
    1: [
      {
        id: "1",
        name: "Solution A",
        language: "cpp",
        compilerVariant: "g++",
        solutionType: "main",
      },
      {
        id: "2",
        name: "Solution B",
        language: "python",
        solutionType: "accepted",
      },
    ],
    2: [
      {
        id: "3",
        name: "Solution C",
        language: "java",
        solutionType: "main",
      },
    ],
    3: [],
  },
  invocations: {
    1: [
      {
        id: "1",
        solutionIds: ["1", "2"],
        testIds: ["1", "2"],
        status: "Completed",
        createdAt: "2023-01-01T00:00:00Z",
      },
    ],
    2: [],
    3: [],
  },
  invocationMatrices: {
    "1:1": {
      solutions: [
        { id: "1", name: "Solution A", language: "cpp" },
        { id: "2", name: "Solution B", language: "python" },
      ],
      tests: [{ id: "1" }, { id: "2" }],
      results: {
        1: {
          1: { verdict: "OK", timeMs: 100, memoryKb: 1024 },
          2: { verdict: "OK", timeMs: 150, memoryKb: 2048 },
        },
        2: {
          1: { verdict: "WA", timeMs: 200, memoryKb: 1536 },
          2: { verdict: "OK", timeMs: 120, memoryKb: 1024 },
        },
      },
    },
  },
};

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Authentication API methods
 */
export const authAPI = {
  /**
   * Logs in a user
   * @param {string} login - User login
   * @param {string} password - User password
   * @returns {Promise} Axios response
   * @throws {Error} If login fails
   */
  login: (login, password) => {
    if (UI_TEST) {
      return Promise.resolve({
        data: { token: "mock-jwt-token", login: login },
      });
    }
    return api.post("/auth/login", { login, password });
  },

  /**
   * Registers a new user
   * @param {string} login - User login
   * @param {string} password - User password
   * @returns {Promise} Axios response
   * @throws {Error} If registration fails
   */
  register: (login, password) => {
    if (UI_TEST) {
      return Promise.resolve({
        data: { token: "mock-jwt-token", login: login },
      });
    }
    return api.post("/auth/register", { login, password });
  },

  /**
   * Gets current user info
   * @returns {Promise} Axios response with user data
   * @throws {Error} If not authenticated or endpoint unavailable
   */
  me: () => {
    if (UI_TEST) {
      return Promise.resolve({
        data: { login: localStorage.getItem("login") || "mock-user" },
      });
    }
    return api.get("/auth/me");
  },
};

/**
 * Contests API methods
 */
export const contestsAPI = {
  /**
   * Lists user's contests
   * @returns {Promise} Axios response with contests array
   * @throws {Error} If fetch fails
   */
  list: () => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.contests });
    }
    return api.get("/contests");
  },

  /**
   * Gets a contest by ID
   * @param {string} id - Contest ID
   * @returns {Promise} Axios response with contest data
   * @throws {Error} If fetch fails
   */
  get: (id) => {
    if (UI_TEST) {
      const contest = mockData.contests.find((c) => c.id === id);
      if (!contest) {
        return Promise.reject(new Error("Contest not found"));
      }
      return Promise.resolve({ data: contest });
    }
    return api.get(`/contests/${id}`);
  },

  /**
   * Creates a new contest
   * @param {Object} contest - Contest data
   * @returns {Promise} Axios response with created contest
   * @throws {Error} If creation fails
   */
  create: (contest) => {
    if (UI_TEST) {
      const newContest = {
        id: String(mockData.contests.length + 1),
        ...contest,
        role: "Owner",
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      mockData.contests.push(newContest);
      return Promise.resolve({ data: newContest });
    }
    return api.post("/contests", contest);
  },

  /**
   * Updates a contest
   * @param {string} id - Contest ID
   * @param {Object} contest - Updated contest data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (id, contest) => {
    if (UI_TEST) {
      const index = mockData.contests.findIndex((c) => c.id === id);
      if (index === -1) {
        return Promise.reject(new Error("Contest not found"));
      }
      mockData.contests[index] = {
        ...mockData.contests[index],
        ...contest,
        updatedAt: new Date().toISOString(),
      };
      return Promise.resolve({ data: mockData.contests[index] });
    }
    return api.put(`/contests/${id}`, contest);
  },

  /**
   * Deletes a contest
   * @param {string} id - Contest ID
   * @returns {Promise} Axios response
   * @throws {Error} If deletion fails
   */
  delete: (id) => {
    if (UI_TEST) {
      const index = mockData.contests.findIndex((c) => c.id === id);
      if (index === -1) {
        return Promise.reject(new Error("Contest not found"));
      }
      mockData.contests.splice(index, 1);
      return Promise.resolve({ data: null });
    }
    return api.delete(`/contests/${id}`);
  },

  /**
   * Gets problems for a contest
   * @param {string} contestId - Contest ID
   * @returns {Promise} Axios response with problems array
   * @throws {Error} If fetch fails
   */
  getProblems: (contestId) => {
    if (UI_TEST) {
      return Promise.resolve({
        data: mockData.allTasks.filter((t) => t.contestId === contestId),
      });
    }
    return api.get(`/contests/${contestId}/problems`);
  },

  /**
   * Reorders problems in a contest
   * @param {string} contestId - Contest ID
   * @param {Array} order - Array of problem IDs in new order
   * @returns {Promise} Axios response
   * @throws {Error} If reorder fails
   */
  reorderProblems: (contestId, order) => {
    if (UI_TEST) {
      const contestTasks = mockData.allTasks.filter(
        (t) => t.contestId === contestId,
      );
      const reorderedTasks = order
        .map((taskId, index) => {
          const task = contestTasks.find((t) => t.id === taskId);
          return task ? { ...task, orderIndex: index } : null;
        })
        .filter(Boolean);
      // Update allTasks
      reorderedTasks.forEach((updated) => {
        const index = mockData.allTasks.findIndex((t) => t.id === updated.id);
        if (index !== -1) mockData.allTasks[index] = updated;
      });
      return Promise.resolve({ data: reorderedTasks });
    }
    return api.put(`/contests/${contestId}/problems/reorder`, { order });
  },

  /**
   * Adds a problem to a contest
   * @param {string} contestId - Contest ID
   * @param {string} problemId - Problem ID
   * @returns {Promise} Axios response
   * @throws {Error} If addition fails
   */
  addProblemToContest: (contestId, problemId) => {
    if (UI_TEST) {
      const task = mockData.allTasks.find((t) => t.id === problemId);
      if (task) {
        mockData.allTasks.push({ ...task, contestId });
      }
      return Promise.resolve({ data: null });
    }
    return api.post(`/contests/${contestId}/problems/${problemId}`);
  },

  /**
   * Removes a problem from a contest
   * @param {string} contestId - Contest ID
   * @param {string} problemId - Problem ID
   * @returns {Promise} Axios response
   * @throws {Error} If removal fails
   */
  removeProblemFromContest: (contestId, problemId) => {
    if (UI_TEST) {
      const index = mockData.allTasks.findIndex(
        (t) => t.contestId === contestId && t.id === problemId,
      );
      if (index !== -1) {
        mockData.allTasks.splice(index, 1);
      }
      return Promise.resolve({ data: null });
    }
    return api.delete(`/contests/${contestId}/problems/${problemId}`);
  },
};

/**
 * Problems API methods
 */
export const problemsAPI = {
  /**
   * Lists user's problems
   * @returns {Promise} Axios response with problems array
   * @throws {Error} If fetch fails
   */
  list: () => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.allTasks });
    }
    return api.get("/problems");
  },

  /**
   * Creates a new problem
   * @param {Object} problem - Problem data
   * @returns {Promise} Axios response with created problem
   * @throws {Error} If creation fails
   */
  create: (problem, contestId = null) => {
    if (UI_TEST) {
      const newProblem = {
        id: String(
          Math.max(...mockData.allTasks.map((t) => parseInt(t.id)), 0) + 1,
        ),
        ...problem,
        contestId: contestId || "1",
        description: problem.description || "",
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      mockData.allTasks.push(newProblem);
      return Promise.resolve({ data: newProblem });
    }
    return api.post("/problems", {
      title: problem.title,
      contestId: contestId,
    });
  },

  get: (taskId) => {
    if (UI_TEST) {
      const task = mockData.allTasks.find((t) => t.id === taskId);
      if (!task) return Promise.reject(new Error("Task not found"));
      return Promise.resolve({ data: task });
    }
    return api.get(`/problems/${taskId}`);
  },

  /**
   * Gets contests for a problem
   * @param {string} problemId - Problem ID
   * @returns {Promise} Axios response with contests array
   * @throws {Error} If fetch fails
   */
  getContests: (problemId) => {
    if (UI_TEST) {
      // Mock contests for the problem
      const problemContests = mockData.allTasks
        .filter((t) => t.id === problemId)
        .map((t) => t.contestId)
        .filter(Boolean)
        .map((contestId) => mockData.contests.find((c) => c.id === contestId))
        .filter(Boolean);
      return Promise.resolve({ data: problemContests });
    }
    return api.get(`/problems/${problemId}/contests`);
  },

  update: (taskId, task) => {
    if (UI_TEST) {
      const index = mockData.allTasks.findIndex((t) => t.id === taskId);
      if (index === -1) return Promise.reject(new Error("Task not found"));
      mockData.allTasks[index] = {
        ...mockData.allTasks[index],
        ...task,
        updatedAt: new Date().toISOString(),
      };
      return Promise.resolve({ data: mockData.allTasks[index] });
    }
    return api.put(`/problems/${taskId}`, task);
  },

  delete: (taskId) => {
    if (UI_TEST) {
      const index = mockData.allTasks.findIndex((t) => t.id === taskId);
      if (index === -1) return Promise.reject(new Error("Task not found"));
      mockData.allTasks.splice(index, 1);
      return Promise.resolve({ data: null });
    }
    return api.delete(`/problems/${taskId}`);
  },
};

/**
 * General API methods for task general settings
 */
export const generalAPI = {
  /**
   * Gets general settings for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with general data
   * @throws {Error} If fetch fails
   */
  get: (taskId) => {
    if (UI_TEST) {
      const data = mockData.general[taskId];
      if (!data) {
        return Promise.reject(new Error("General data not found"));
      }
      return Promise.resolve({ data });
    }
    return api.get(`/problems/${taskId}/general`);
  },

  /**
   * Updates general settings for a task
   * @param {string} taskId - Task ID
   * @param {Object} data - General data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, data) => {
    if (UI_TEST) {
      mockData.general[taskId] = { ...mockData.general[taskId], ...data };
      return Promise.resolve({ data: mockData.general[taskId] });
    }
    return api.put(`/problems/${taskId}/general`, data);
  },
};

/**
 * Statement API methods for task statement
 */
export const statementAPI = {
  /**
   * Gets statement for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with statement data
   * @throws {Error} If fetch fails
   */
  get: (taskId) => {
    if (UI_TEST) {
      const data = mockData.statement[taskId];
      if (!data) {
        return Promise.reject(new Error("Statement data not found"));
      }
      return Promise.resolve({ data });
    }
    return api.get(`/problems/${taskId}/statement`);
  },

  /**
   * Updates statement for a task
   * @param {string} taskId - Task ID
   * @param {Object} data - Statement data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, data) => {
    if (UI_TEST) {
      mockData.statement[taskId] = { ...mockData.statement[taskId], ...data };
      return Promise.resolve({ data: mockData.statement[taskId] });
    }
    return api.put(`/problems/${taskId}/statement`, data);
  },

  /**
   * Exports LaTeX for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with LaTeX file
   * @throws {Error} If export fails
   */
  exportTex: (taskId) =>
    api.get(`/problems/${taskId}/statement/export/tex`, {
      responseType: "blob",
    }),

  /**
   * Exports PDF for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with PDF file
   * @throws {Error} If export fails
   */
  exportPdf: (taskId) =>
    api.get(`/problems/${taskId}/statement/export/pdf`, {
      responseType: "blob",
    }),
};

/**
 * Checker API methods
 */
export const checkerAPI = {
  /**
   * Uploads source for checker
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Source file and language
   * @returns {Promise} Axios response
   * @throws {Error} If upload fails
   */
  uploadSource: (taskId, formData) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      return Promise.resolve({
        data: { message: "Source uploaded successfully" },
      });
    }
    return api.post(`/problems/${taskId}/checker/source`, formData);
  },

  /**
   * Gets source for checker
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with source data
   * @throws {Error} If fetch fails
   */
  getSource: (taskId) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      return Promise.resolve({ data });
    }
    return api.get(`/problems/${taskId}/checker/source`);
  },

  /**
   * Updates source for checker
   * @param {string} taskId - Task ID
   * @param {Object} data - Source data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  updateSource: (taskId, data) => {
    if (UI_TEST) {
      mockData.checker[taskId] = { ...mockData.checker[taskId], ...data };
      return Promise.resolve({ data: mockData.checker[taskId] });
    }
    return api.put(`/problems/${taskId}/checker/source`, data);
  },

  /**
   * Lists tests for checker
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with tests array
   * @throws {Error} If fetch fails
   */
  listTests: (taskId) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      return Promise.resolve({ data: data.tests });
    }
    return api.get(`/problems/${taskId}/checker/tests`);
  },

  /**
   * Creates a test for checker
   * @param {string} taskId - Task ID
   * @param {Object} test - Test data
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  createTest: (taskId, test) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      const newTest = { id: String(data.tests.length + 1), ...test };
      data.tests.push(newTest);
      return Promise.resolve({ data: newTest });
    }
    return api.post(`/problems/${taskId}/checker/tests`, test);
  },

  /**
   * Updates a test for checker
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @param {Object} test - Updated test data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  updateTest: (taskId, testId, test) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      const index = data.tests.findIndex((t) => t.id === testId);
      if (index === -1) {
        return Promise.reject(new Error("Test not found"));
      }
      data.tests[index] = { ...data.tests[index], ...test };
      return Promise.resolve({ data: data.tests[index] });
    }
    return api.put(`/problems/${taskId}/checker/tests/${testId}`, test);
  },

  /**
   * Deletes a test for checker
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @returns {Promise} Axios response
   * @throws {Error} If deletion fails
   */
  deleteTest: (taskId, testId) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      const index = data.tests.findIndex((t) => t.id === testId);
      if (index === -1) {
        return Promise.reject(new Error("Test not found"));
      }
      data.tests.splice(index, 1);
      return Promise.resolve({ data: null });
    }
    return api.delete(`/problems/${taskId}/checker/tests/${testId}`);
  },

  /**
   * Runs tests for checker
   * @param {string} taskId - Task ID
   * @param {Array} testIds - Optional test IDs to run
   * @returns {Promise} Axios response with results
   * @throws {Error} If run fails
   */
  runTests: (taskId, testIds) => {
    if (UI_TEST) {
      const data = mockData.checker[taskId];
      if (!data) {
        return Promise.reject(new Error("Checker data not found"));
      }
      const results = {};
      const testsToRun = testIds
        ? data.tests.filter((t) => testIds.includes(t.id))
        : data.tests;
      testsToRun.forEach((test) => {
        results[test.id] = test.verdict || "OK";
      });
      data.runResults = results;
      return Promise.resolve({ data: results });
    }
    return api.post(`/problems/${taskId}/checker/run`, { testIds });
  },
};

/**
 * Validator API methods (similar to checker)
 */
export const validatorAPI = {
  uploadSource: (taskId, formData) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      return Promise.resolve({
        data: { message: "Source uploaded successfully" },
      });
    }
    return api.post(`/problems/${taskId}/validator/source`, formData);
  },
  getSource: (taskId) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      return Promise.resolve({ data });
    }
    return api.get(`/problems/${taskId}/validator/source`);
  },
  updateSource: (taskId, data) => {
    if (UI_TEST) {
      mockData.validator[taskId] = { ...mockData.validator[taskId], ...data };
      return Promise.resolve({ data: mockData.validator[taskId] });
    }
    return api.put(`/problems/${taskId}/validator/source`, data);
  },
  listTests: (taskId) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      return Promise.resolve({ data: data.tests });
    }
    return api.get(`/problems/${taskId}/validator/tests`);
  },
  createTest: (taskId, test) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      const newTest = { id: String(data.tests.length + 1), ...test };
      data.tests.push(newTest);
      return Promise.resolve({ data: newTest });
    }
    return api.post(`/problems/${taskId}/validator/tests`, test);
  },
  updateTest: (taskId, testId, test) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      const index = data.tests.findIndex((t) => t.id === testId);
      if (index === -1) {
        return Promise.reject(new Error("Test not found"));
      }
      data.tests[index] = { ...data.tests[index], ...test };
      return Promise.resolve({ data: data.tests[index] });
    }
    return api.put(`/problems/${taskId}/validator/tests/${testId}`, test);
  },
  deleteTest: (taskId, testId) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      const index = data.tests.findIndex((t) => t.id === testId);
      if (index === -1) {
        return Promise.reject(new Error("Test not found"));
      }
      data.tests.splice(index, 1);
      return Promise.resolve({ data: null });
    }
    return api.delete(`/problems/${taskId}/validator/tests/${testId}`);
  },
  runTests: (taskId, testIds) => {
    if (UI_TEST) {
      const data = mockData.validator[taskId];
      if (!data) {
        return Promise.reject(new Error("Validator data not found"));
      }
      const results = {};
      const testsToRun = testIds
        ? data.tests.filter((t) => testIds.includes(t.id))
        : data.tests;
      testsToRun.forEach((test) => {
        results[test.id] = test.verdict || "OK";
      });
      data.runResults = results;
      return Promise.resolve({ data: results });
    }
    return api.post(`/problems/${taskId}/validator/run`, { testIds });
  },
};

/**
 * Tests API methods for problem tests
 */
export const testsAPI = {
  /**
   * Lists tests for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with tests array
   * @throws {Error} If fetch fails
   */
  list: async (taskId) => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.tests[taskId] || [] });
    }
    const response = await api.get(`/problems/${taskId}/tests`);
    // Transform backend TestResponse to frontend format
    const transformedTests = response.data.map((test) => ({
      id: test.testId,
      input: test.content, // content is the input for RAW tests
      output: null, // output will be fetched separately if needed
      description: test.description,
      testType: test.testType,
      sample: test.sample,
      points: test.points,
    }));
    return { data: transformedTests };
  },

  /**
   * Creates a test via text
   * @param {string} taskId - Task ID
   * @param {Object} test - Test data
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  createText: (taskId, test) => {
    if (UI_TEST) {
      const newTest = {
        id: String(
          (mockData.tests[taskId] ? mockData.tests[taskId].length : 0) + 1,
        ),
        input: test.inputText,
        description: test.description,
        points: test.points || 1,
        sample: test.sample || false,
        testType: test.testType || "RAW",
        ...test,
      };
      if (!mockData.tests[taskId]) {
        mockData.tests[taskId] = [];
      }
      mockData.tests[taskId].push(newTest);
      return Promise.resolve({ data: newTest });
    }
    // Transform frontend data to backend format
    const backendTest = {
      testType: test.testType || "RAW",
      content: test.inputText,
      description: test.description,
      sample: test.sample || false,
      points: test.points || 1,
    };
    return api.post(`/problems/${taskId}/tests`, backendTest);
  },

  /**
   * Creates a test via file upload
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Test file
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  createFile: (taskId, formData) => {
    if (UI_TEST) {
      // For UI test mode, treat file upload similarly to text creation
      const newTest = {
        id: String(
          (mockData.tests[taskId] ? mockData.tests[taskId].length : 0) + 1,
        ),
        // No specific fields from formData; placeholder values
        input: "uploaded",
        description: "",
        points: 1,
      };
      if (!mockData.tests[taskId]) {
        mockData.tests[taskId] = [];
      }
      mockData.tests[taskId].push(newTest);
      return Promise.resolve({ data: newTest });
    }
    return api.post(`/problems/${taskId}/tests`, formData);
  },

  /**
   * Updates a test
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @param {Object} test - Updated test data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, testId, test) => {
    if (UI_TEST) {
      const taskTests = mockData.tests[taskId];
      if (!taskTests) {
        return Promise.reject(new Error("No tests for this task"));
      }
      const index = taskTests.findIndex((t) => t.id === testId);
      if (index === -1) {
        return Promise.reject(new Error("Test not found"));
      }
      // Support both inputText and legacy input field
      const existing = taskTests[index];
      const updated = {
        ...existing,
        ...test,
        input: test.inputText !== undefined ? test.inputText : existing.input,
        inputText:
          test.inputText !== undefined ? test.inputText : existing.inputText,
        points: test.points !== undefined ? test.points : existing.points,
        sample: test.sample !== undefined ? test.sample : existing.sample,
      };
      taskTests[index] = updated;
      return Promise.resolve({ data: updated });
    }
    // Transform frontend data to backend format
    const backendTest = {
      testType: "RAW",
      content: test.inputText,
      description: test.description,
      sample: test.sample || false,
      points: test.points || 1,
    };
    return api.put(`/problems/${taskId}/tests/${testId}`, backendTest);
  },

  /**
   * Deletes a test
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @returns {Promise} Axios response
   * @throws {Error} If deletion fails
   */
  delete: (taskId, testId) => {
    if (UI_TEST) {
      if (!mockData.tests[taskId]) {
        return Promise.reject(new Error("No tests for this task"));
      }
      const index = mockData.tests[taskId].findIndex((t) => t.id === testId);
      if (index === -1) {
        return Promise.reject(new Error("Test not found"));
      }
      mockData.tests[taskId].splice(index, 1);
      return Promise.resolve({ data: null });
    }
    return api.delete(`/problems/${taskId}/tests/${testId}`);
  },
};

/**
 * Generators API methods
 */
export const generatorsAPI = {
  /**
   * Lists generators for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with generators array
   * @throws {Error} If fetch fails
   */
  list: (taskId) => {
    if (UI_TEST) {
      // Return mock generators if any, otherwise empty array
      return Promise.resolve({ data: mockData.generators[taskId] || [] });
    }
    return api.get(`/problems/${taskId}/generators`).then((response) => {
      // Transform backend response to frontend format
      const transformedGenerators = response.data.map((gen) => ({
        id: gen.generatorId.toString(), // Convert Long to string
        alias: gen.alias,
        language: gen.format
          .toLowerCase()
          .replace("_17", "")
          .replace("_14", ""),
        ...gen,
      }));
      return { data: transformedGenerators };
    });
  },

  /**
   * Creates a generator
   * @param {string} taskId - Task ID
   * @param {Object} generatorData - Generator data with file, format, alias
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  create: (taskId, generatorData) => {
    if (UI_TEST) {
      // Create a mock generator entry
      const newGenerator = {
        id: String(
          (mockData.generators[taskId]
            ? mockData.generators[taskId].length
            : 0) + 1,
        ),
        alias: generatorData.alias,
        language: generatorData.format.toLowerCase().replace("_17", ""),
      };
      if (!mockData.generators[taskId]) {
        mockData.generators[taskId] = [];
      }
      mockData.generators[taskId].push(newGenerator);
      return Promise.resolve({ data: newGenerator });
    }
    return api.post(`/problems/${taskId}/generators`, generatorData);
  },

  /**
   * Uploads source for generator (legacy - kept for compatibility)
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Source file
   * @returns {Promise} Axios response
   * @throws {Error} If upload fails
   */
  uploadSource: (taskId, formData) => {
    // This is now deprecated - use create() instead
    return api.post(`/problems/${taskId}/generators`, formData);
  },

  /**
   * Gets source for generator
   * @param {string} taskId - Task ID
   * @param {string} generatorId - Generator ID
   * @returns {Promise} Axios response with source data
   * @throws {Error} If fetch fails
   */
  getSource: (taskId, generatorId) =>
    api.get(`/problems/${taskId}/generators/${generatorId}`),

  /**
   * Updates source for generator
   * @param {string} taskId - Task ID
   * @param {string} generatorId - Generator ID
   * @param {Object} data - Updated source data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  updateSource: (taskId, generatorId, data) =>
    api.put(`/problems/${taskId}/generators/${generatorId}`, data),

  /**
   * Runs a generator
   * @param {string} taskId - Task ID
   * @param {string} generatorId - Generator ID
   * @param {Object} params - Run parameters
   * @returns {Promise} Axios response
   * @throws {Error} If run fails
   */
  run: (taskId, generatorId, params) =>
    api.post(`/problems/${taskId}/generators/${generatorId}/run`, params),
};

/**
 * Solutions API methods
 */
const getFileFormatFromLanguage = (language) => {
  switch (language) {
    case "cpp":
      return "CPP_17";
    case "c":
      return "C";
    case "java":
      return "JAVA_17";
    case "python":
      return "PYTHON";
    case "js":
      return "JSON"; // Using JSON as placeholder for JS
    default:
      return "TEXT";
  }
};

export const solutionsAPI = {
  /**
   * Lists solutions for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with solutions array
   * @throws {Error} If fetch fails
   */
  list: (taskId) => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.solutions[taskId] || [] });
    }
    return api.get(`/problems/${taskId}/solutions`);
  },

  /**
   * Uploads source for solution
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Source file with name, language, solutionType
   * @returns {Promise} Axios response
   * @throws {Error} If upload fails
   */
  uploadSource: (taskId, formData) => {
    if (UI_TEST) {
      const name = formData.get("name") || `Solution ${Date.now()}`;
      const language = formData.get("language") || "cpp";
      const solutionType =
        formData.get("solutionType") || "Main correct solution";

      const newSolution = {
        id: String(
          (mockData.solutions[taskId] ? mockData.solutions[taskId].length : 0) +
            1,
        ),
        name,
        language,
        solutionType: solutionType,
      };
      if (!mockData.solutions[taskId]) {
        mockData.solutions[taskId] = [];
      }
      mockData.solutions[taskId].push(newSolution);
      if (!mockData.solutionSources) {
        mockData.solutionSources = {};
      }
      mockData.solutionSources[`${taskId}:${newSolution.id}`] = {
        source: "// placeholder source code",
        language,
      };
      return Promise.resolve({ data: newSolution });
    }
    return api.post(`/problems/${taskId}/solutions`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  /**
   * Gets source for solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @returns {Promise} Axios response with source data
   * @throws {Error} If fetch fails
   */
  getSource: (taskId, solutionId) => {
    if (UI_TEST) {
      if (
        mockData.solutionSources &&
        mockData.solutionSources[`${taskId}:${solutionId}`]
      ) {
        return Promise.resolve({
          data: mockData.solutionSources[`${taskId}:${solutionId}`],
        });
      }
      return Promise.resolve({
        data: { source: "// placeholder source code", language: "cpp" },
      });
    }
    return api.get(`/problems/${taskId}/solutions/${solutionId}/source`);
  },

  /**
   * Updates a solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @param {Object} data - Updated solution data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, solutionId, data) => {
    if (UI_TEST) {
      const solutions = mockData.solutions[taskId] || [];
      const index = solutions.findIndex((s) => s.id === solutionId);
      if (index === -1) {
        return Promise.reject(new Error("Solution not found"));
      }
      solutions[index] = { ...solutions[index], ...data };
      if (data.source) {
        if (!mockData.solutionSources) {
          mockData.solutionSources = {};
        }
        mockData.solutionSources[`${taskId}:${solutionId}`] = {
          source: data.source,
          language: data.language || solutions[index].language,
        };
      }
      return Promise.resolve({ data: solutions[index] });
    }
    // Transform frontend data to backend format
    const backendData = {
      name: data.name,
      language: data.language,
      file: data.source ? btoa(data.source) : "", // Only send file if source is provided
      format: getFileFormatFromLanguage(data.language),
      solutionType: data.solutionType,
    };
    return api.put(`/problems/${taskId}/solutions/${solutionId}`, backendData);
  },

  /**
   * Downloads source for solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @returns {Promise} Axios response with file
   * @throws {Error} If download fails
   */
  download: (taskId, solutionId) => {
    if (UI_TEST) {
      const sourceObj = (mockData.solutionSources &&
        mockData.solutionSources[`${taskId}:${solutionId}`]) || {
        source: "// placeholder source code",
        language: "cpp",
      };
      const blob = new Blob([sourceObj.source], {
        type: "text/plain;charset=utf-8",
      });
      return Promise.resolve({ data: blob });
    }
    return api.get(`/problems/${taskId}/solutions/${solutionId}/download`, {
      responseType: "blob",
    });
  },

  /**
   * Compiles a solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @returns {Promise} Axios response with compile result
   * @throws {Error} If compile fails
   */
  compile: (taskId, solutionId) => {
    if (UI_TEST) {
      return Promise.resolve({
        data: {
          verdict: "OK",
          stdout: "Compilation successful",
          stderr: "",
        },
      });
    }
    return api.post(`/problems/${taskId}/solutions/${solutionId}/compile`);
  },
};

/**
 * Invocations API methods
 */
export const invocationsAPI = {
  /**
   * Creates an invocation
   * @param {string} taskId - Task ID
   * @param {Object} invocation - Invocation data
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  create: (taskId, invocation) => {
    if (UI_TEST) {
      const invs = mockData.invocations[taskId] || [];
      const newId = String(
        Math.max(...invs.map((i) => parseInt(i.id, 10)), 0) + 1,
      );
      const newInv = {
        id: newId,
        solutionIds: invocation.solutionIds || [],
        testIds: invocation.testIds || [],
        status: invocation.status || "Completed",
        createdAt: new Date().toISOString(),
      };
      if (!mockData.invocations[taskId]) mockData.invocations[taskId] = [];
      mockData.invocations[taskId].push(newInv);

      // Build a simple matrix entry for UI testing
      const solutions = (mockData.solutions[taskId] || []).filter((s) =>
        newInv.solutionIds.includes(s.id),
      );
      const tests = (mockData.tests[taskId] || []).filter((t) =>
        newInv.testIds.includes(t.id),
      );
      const results = {};
      solutions.forEach((sol, si) => {
        results[sol.id] = {};
        tests.forEach((test, ti) => {
          // deterministic sample verdict based on ids
          const verdict = (si + ti) % 3 === 0 ? "WA" : "OK";
          results[sol.id][test.id] = {
            verdict,
            timeMs: 100 + (si + ti) * 10,
            memoryKb: 1024 + (si + ti) * 100,
          };
        });
      });

      if (!mockData.invocationMatrices) mockData.invocationMatrices = {};
      mockData.invocationMatrices[`${taskId}:${newId}`] = {
        invocationId: newId,
        status: "Completed",
        solutions,
        tests,
        results,
      };
      return Promise.resolve({ data: newInv });
    }
    return api.post(`/problems/${taskId}/invocations`, invocation);
  },

  /**
   * Lists invocations for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with invocations array
   * @throws {Error} If fetch fails
   */
  list: (taskId) => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.invocations[taskId] || [] });
    }
    return api.get(`/problems/${taskId}/invocations`);
  },

  /**
   * Gets an invocation
   * @param {string} taskId - Task ID
   * @param {string} invocationId - Invocation ID
   * @returns {Promise} Axios response with invocation data
   * @throws {Error} If fetch fails
   */
  get: (taskId, invocationId) =>
    api.get(`/problems/${taskId}/invocations/${invocationId}`),

  /**
   * Gets matrix for an invocation
   * @param {string} taskId - Task ID
   * @param {string} invocationId - Invocation ID
   * @returns {Promise} Axios response with matrix data
   * @throws {Error} If fetch fails
   */
  matrix: (taskId, invocationId) => {
    if (UI_TEST) {
      const key = `${taskId}:${invocationId}`;
      if (mockData.invocationMatrices && mockData.invocationMatrices[key]) {
        return Promise.resolve({ data: mockData.invocationMatrices[key] });
      }
      // Fallback: try to build matrix from invocation entry
      const inv = (mockData.invocations[taskId] || []).find(
        (i) => i.id === invocationId,
      );
      if (!inv) {
        return Promise.reject(new Error("Invocation not found"));
      }
      const solutions = (mockData.solutions[taskId] || []).filter((s) =>
        inv.solutionIds.includes(s.id),
      );
      const tests = (mockData.tests[taskId] || []).filter((t) =>
        inv.testIds.includes(t.id),
      );
      const results = {};
      solutions.forEach((sol, si) => {
        results[sol.id] = {};
        tests.forEach((test, ti) => {
          results[sol.id][test.id] = {
            verdict: "OK",
            timeMs: 100 + (si + ti) * 10,
            memoryKb: 1024,
          };
        });
      });
      return Promise.resolve({
        data: {
          invocationId: invocationId,
          status: "Completed",
          solutions,
          tests,
          results,
        },
      });
    }
    return api.get(`/problems/${taskId}/invocations/${invocationId}/matrix`);
  },
};

export default api;
