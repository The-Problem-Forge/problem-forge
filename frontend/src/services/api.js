import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_URL || "http://localhost:8080/api";

const UI_TEST = process.env.REACT_APP_UI_TEST === "true";

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Mock data for UI testing
const mockData = {
  contests: [
    {
      id: "1",
      name: "Sample Contest 1",
      description: "A sample contest for testing",
      role: "Owner",
      createdAt: "2023-01-01T00:00:00Z",
      updatedAt: "2023-01-01T00:00:00Z",
    },
    {
      id: "2",
      name: "Sample Contest 2",
      description: "Another sample contest",
      role: "Editor",
      createdAt: "2023-01-02T00:00:00Z",
      updatedAt: "2023-01-02T00:00:00Z",
    },
  ],
  tasks: {
    1: [
      {
        id: "1",
        title: "Task A",
        description: "First task in contest 1",
        orderIndex: 0,
        createdAt: "2023-01-01T00:00:00Z",
        updatedAt: "2023-01-01T00:00:00Z",
      },
      {
        id: "2",
        title: "Task B",
        description: "Second task in contest 1",
        orderIndex: 1,
        createdAt: "2023-01-01T00:00:00Z",
        updatedAt: "2023-01-01T00:00:00Z",
      },
    ],
    2: [
      {
        id: "3",
        title: "Task C",
        description: "First task in contest 2",
        orderIndex: 0,
        createdAt: "2023-01-02T00:00:00Z",
        updatedAt: "2023-01-02T00:00:00Z",
      },
    ],
  },
  allTasks: [
    {
      id: "1",
      title: "Task A",
      description: "First task in contest 1",
    },
    {
      id: "2",
      title: "Task B",
      description: "Second task in contest 1",
    },
    {
      id: "3",
      title: "Task C",
      description: "First task in contest 2",
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
      statementTex: "Solve the equation $x^2 + 2x + 1 = 0$.",
      inputFormatTex: "First line: $N$",
      outputFormatTex: "Single integer",
      notesTex: "Note that $x = -1$ is the solution.",
      tutorialTex:
        "Use the quadratic formula: $x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}$",
    },
    2: {
      statementTex:
        "Another problem with $\\sum_{i=1}^n i = \\frac{n(n+1)}{2}$.",
      inputFormatTex: "Input format",
      outputFormatTex: "Output format",
      notesTex: "",
      tutorialTex: "",
    },
    3: {
      statementTex: "Third problem: $\\int_0^1 x^2 dx = \\frac{1}{3}$.",
      inputFormatTex: "Format",
      outputFormatTex: "Result",
      notesTex: "Notes",
      tutorialTex: "Tutorial",
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
  login: (login, password) => api.post("/auth/login", { login, password }),

  /**
   * Registers a new user
   * @param {string} login - User login
   * @param {string} password - User password
   * @returns {Promise} Axios response
   * @throws {Error} If registration fails
   */
  register: (login, password) =>
    api.post("/auth/register", { login, password }),

  /**
   * Gets current user info
   * @returns {Promise} Axios response with user data
   * @throws {Error} If not authenticated or endpoint unavailable
   */
  me: () => api.get("/auth/me"),
};

/**
 * Task API methods
 */
export const taskAPI = {
  /**
   * Gets all tasks
   * @returns {Promise} Axios response with tasks array
   * @throws {Error} If fetch fails
   */
  getTasks: () => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.allTasks });
    }
    return api.get("/tasks");
  },

  /**
   * Creates a new task
   * @param {Object} task - Task data
   * @returns {Promise} Axios response with created task
   * @throws {Error} If creation fails
   */
  createTask: (task) => {
    if (UI_TEST) {
      const newTask = {
        id: String(
          Math.max(...mockData.allTasks.map((t) => parseInt(t.id)), 0) + 1,
        ),
        ...task,
      };
      mockData.allTasks.push(newTask);
      return Promise.resolve({ data: newTask });
    }
    return api.post("/tasks", task);
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
      delete mockData.tasks[id];
      return Promise.resolve({ data: null });
    }
    return api.delete(`/contests/${id}`);
  },

  /**
   * Reorders tasks in a contest
   * @param {string} contestId - Contest ID
   * @param {Array} order - Array of task IDs in new order
   * @returns {Promise} Axios response
   * @throws {Error} If reorder fails
   */
  reorderTasks: (contestId, order) => {
    if (UI_TEST) {
      const tasks = mockData.tasks[contestId] || [];
      const reorderedTasks = order
        .map((taskId, index) => {
          const task = tasks.find((t) => t.id === taskId);
          return task ? { ...task, orderIndex: index } : null;
        })
        .filter(Boolean);
      mockData.tasks[contestId] = reorderedTasks;
      return Promise.resolve({ data: reorderedTasks });
    }
    return api.put(`/contests/${contestId}/tasks/reorder`, { order });
  },
};

/**
 * Tasks API methods
 */
export const tasksAPI = {
  /**
   * Lists tasks by contest
   * @param {string} contestId - Contest ID
   * @returns {Promise} Axios response with tasks array
   * @throws {Error} If fetch fails
   */
  listByContest: (contestId) => {
    if (UI_TEST) {
      return Promise.resolve({ data: mockData.tasks[contestId] || [] });
    }
    return api.get(`/contests/${contestId}/tasks`);
  },

  /**
   * Gets a task by ID
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with task data
   * @throws {Error} If fetch fails
   */
  get: (taskId) => {
    if (UI_TEST) {
      for (const contestTasks of Object.values(mockData.tasks)) {
        const task = contestTasks.find((t) => t.id === taskId);
        if (task) {
          return Promise.resolve({ data: task });
        }
      }
      return Promise.reject(new Error("Task not found"));
    }
    return api.get(`/tasks/${taskId}`);
  },

  /**
   * Creates a new task in a contest
   * @param {string} contestId - Contest ID
   * @param {Object} task - Task data
   * @returns {Promise} Axios response with created task
   * @throws {Error} If creation fails
   */
  create: (contestId, task) => {
    if (UI_TEST) {
      const contestTasks = mockData.tasks[contestId] || [];
      const newTask = {
        id: String(
          Math.max(
            ...Object.values(mockData.tasks)
              .flat()
              .map((t) => parseInt(t.id)),
            0,
          ) + 1,
        ),
        ...task,
        orderIndex: contestTasks.length,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      if (!mockData.tasks[contestId]) {
        mockData.tasks[contestId] = [];
      }
      mockData.tasks[contestId].push(newTask);
      mockData.allTasks.push(newTask);
      return Promise.resolve({ data: newTask });
    }
    return api.post(`/contests/${contestId}/tasks`, task);
  },

  /**
   * Updates a task
   * @param {string} taskId - Task ID
   * @param {Object} task - Updated task data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, task) => {
    if (UI_TEST) {
      for (const contestId in mockData.tasks) {
        const tasks = mockData.tasks[contestId];
        const index = tasks.findIndex((t) => t.id === taskId);
        if (index !== -1) {
          tasks[index] = {
            ...tasks[index],
            ...task,
            updatedAt: new Date().toISOString(),
          };
          // Update in allTasks too
          const allIndex = mockData.allTasks.findIndex((t) => t.id === taskId);
          if (allIndex !== -1) {
            mockData.allTasks[allIndex] = tasks[index];
          }
          return Promise.resolve({ data: tasks[index] });
        }
      }
      return Promise.reject(new Error("Task not found"));
    }
    return api.put(`/tasks/${taskId}`, task);
  },

  /**
   * Deletes a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response
   * @throws {Error} If deletion fails
   */
  delete: (taskId) => {
    if (UI_TEST) {
      for (const contestId in mockData.tasks) {
        const tasks = mockData.tasks[contestId];
        const index = tasks.findIndex((t) => t.id === taskId);
        if (index !== -1) {
          tasks.splice(index, 1);
          // Remove from allTasks
          const allIndex = mockData.allTasks.findIndex((t) => t.id === taskId);
          if (allIndex !== -1) {
            mockData.allTasks.splice(allIndex, 1);
          }
          return Promise.resolve({ data: null });
        }
      }
      return Promise.reject(new Error("Task not found"));
    }
    return api.delete(`/tasks/${taskId}`);
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
    return api.get(`/tasks/${taskId}/general`);
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
    return api.put(`/tasks/${taskId}/general`, data);
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
    return api.get(`/tasks/${taskId}/statement`);
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
    return api.put(`/tasks/${taskId}/statement`, data);
  },

  /**
   * Exports LaTeX for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with LaTeX file
   * @throws {Error} If export fails
   */
  exportTex: (taskId) =>
    api.get(`/tasks/${taskId}/statement/export/tex`, { responseType: "blob" }),

  /**
   * Exports PDF for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with PDF file
   * @throws {Error} If export fails
   */
  exportPdf: (taskId) =>
    api.get(`/tasks/${taskId}/statement/export/pdf`, { responseType: "blob" }),
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
  uploadSource: (taskId, formData) =>
    api.post(`/tasks/${taskId}/checker/source`, formData),

  /**
   * Gets source for checker
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with source data
   * @throws {Error} If fetch fails
   */
  getSource: (taskId) => api.get(`/tasks/${taskId}/checker/source`),

  /**
   * Updates source for checker
   * @param {string} taskId - Task ID
   * @param {Object} data - Source data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  updateSource: (taskId, data) =>
    api.put(`/tasks/${taskId}/checker/source`, data),

  /**
   * Lists tests for checker
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with tests array
   * @throws {Error} If fetch fails
   */
  listTests: (taskId) => api.get(`/tasks/${taskId}/checker/tests`),

  /**
   * Creates a test for checker
   * @param {string} taskId - Task ID
   * @param {Object} test - Test data
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  createTest: (taskId, test) =>
    api.post(`/tasks/${taskId}/checker/tests`, test),

  /**
   * Updates a test for checker
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @param {Object} test - Updated test data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  updateTest: (taskId, testId, test) =>
    api.put(`/tasks/${taskId}/checker/tests/${testId}`, test),

  /**
   * Deletes a test for checker
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @returns {Promise} Axios response
   * @throws {Error} If deletion fails
   */
  deleteTest: (taskId, testId) =>
    api.delete(`/tasks/${taskId}/checker/tests/${testId}`),

  /**
   * Runs tests for checker
   * @param {string} taskId - Task ID
   * @param {Array} testIds - Optional test IDs to run
   * @returns {Promise} Axios response with results
   * @throws {Error} If run fails
   */
  runTests: (taskId, testIds) =>
    api.post(`/tasks/${taskId}/checker/run`, { testIds }),
};

/**
 * Validator API methods (similar to checker)
 */
export const validatorAPI = {
  uploadSource: (taskId, formData) =>
    api.post(`/tasks/${taskId}/validator/source`, formData),
  getSource: (taskId) => api.get(`/tasks/${taskId}/validator/source`),
  updateSource: (taskId, data) =>
    api.put(`/tasks/${taskId}/validator/source`, data),
  listTests: (taskId) => api.get(`/tasks/${taskId}/validator/tests`),
  createTest: (taskId, test) =>
    api.post(`/tasks/${taskId}/validator/tests`, test),
  updateTest: (taskId, testId, test) =>
    api.put(`/tasks/${taskId}/validator/tests/${testId}`, test),
  deleteTest: (taskId, testId) =>
    api.delete(`/tasks/${taskId}/validator/tests/${testId}`),
  runTests: (taskId, testIds) =>
    api.post(`/tasks/${taskId}/validator/run`, { testIds }),
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
  list: (taskId) => api.get(`/tasks/${taskId}/tests`),

  /**
   * Creates a test via text
   * @param {string} taskId - Task ID
   * @param {Object} test - Test data
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  createText: (taskId, test) => api.post(`/tasks/${taskId}/tests`, test),

  /**
   * Creates a test via file upload
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Test file
   * @returns {Promise} Axios response
   * @throws {Error} If creation fails
   */
  createFile: (taskId, formData) =>
    api.post(`/tasks/${taskId}/tests`, formData),

  /**
   * Updates a test
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @param {Object} test - Updated test data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, testId, test) =>
    api.put(`/tasks/${taskId}/tests/${testId}`, test),

  /**
   * Deletes a test
   * @param {string} taskId - Task ID
   * @param {string} testId - Test ID
   * @returns {Promise} Axios response
   * @throws {Error} If deletion fails
   */
  delete: (taskId, testId) => api.delete(`/tasks/${taskId}/tests/${testId}`),
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
  list: (taskId) => api.get(`/tasks/${taskId}/generators`),

  /**
   * Uploads source for generator
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Source file
   * @returns {Promise} Axios response
   * @throws {Error} If upload fails
   */
  uploadSource: (taskId, formData) =>
    api.post(`/tasks/${taskId}/generators`, formData),

  /**
   * Gets source for generator
   * @param {string} taskId - Task ID
   * @param {string} generatorId - Generator ID
   * @returns {Promise} Axios response with source data
   * @throws {Error} If fetch fails
   */
  getSource: (taskId, generatorId) =>
    api.get(`/tasks/${taskId}/generators/${generatorId}`),

  /**
   * Updates source for generator
   * @param {string} taskId - Task ID
   * @param {string} generatorId - Generator ID
   * @param {Object} data - Updated source data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  updateSource: (taskId, generatorId, data) =>
    api.put(`/tasks/${taskId}/generators/${generatorId}`, data),

  /**
   * Runs a generator
   * @param {string} taskId - Task ID
   * @param {string} generatorId - Generator ID
   * @param {Object} params - Run parameters
   * @returns {Promise} Axios response
   * @throws {Error} If run fails
   */
  run: (taskId, generatorId, params) =>
    api.post(`/tasks/${taskId}/generators/${generatorId}/run`, params),
};

/**
 * Solutions API methods
 */
export const solutionsAPI = {
  /**
   * Lists solutions for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with solutions array
   * @throws {Error} If fetch fails
   */
  list: (taskId) => api.get(`/tasks/${taskId}/solutions`),

  /**
   * Uploads source for solution
   * @param {string} taskId - Task ID
   * @param {FormData} formData - Source file
   * @returns {Promise} Axios response
   * @throws {Error} If upload fails
   */
  uploadSource: (taskId, formData) =>
    api.post(`/tasks/${taskId}/solutions`, formData),

  /**
   * Gets source for solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @returns {Promise} Axios response with source data
   * @throws {Error} If fetch fails
   */
  getSource: (taskId, solutionId) =>
    api.get(`/tasks/${taskId}/solutions/${solutionId}`),

  /**
   * Updates a solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @param {Object} data - Updated solution data
   * @returns {Promise} Axios response
   * @throws {Error} If update fails
   */
  update: (taskId, solutionId, data) =>
    api.put(`/tasks/${taskId}/solutions/${solutionId}`, data),

  /**
   * Downloads source for solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @returns {Promise} Axios response with file
   * @throws {Error} If download fails
   */
  download: (taskId, solutionId) =>
    api.get(`/tasks/${taskId}/solutions/${solutionId}/download`, {
      responseType: "blob",
    }),

  /**
   * Compiles a solution
   * @param {string} taskId - Task ID
   * @param {string} solutionId - Solution ID
   * @returns {Promise} Axios response with compile result
   * @throws {Error} If compile fails
   */
  compile: (taskId, solutionId) =>
    api.post(`/tasks/${taskId}/solutions/${solutionId}/compile`),
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
  create: (taskId, invocation) =>
    api.post(`/tasks/${taskId}/invocations`, invocation),

  /**
   * Lists invocations for a task
   * @param {string} taskId - Task ID
   * @returns {Promise} Axios response with invocations array
   * @throws {Error} If fetch fails
   */
  list: (taskId) => api.get(`/tasks/${taskId}/invocations`),

  /**
   * Gets an invocation
   * @param {string} taskId - Task ID
   * @param {string} invocationId - Invocation ID
   * @returns {Promise} Axios response with invocation data
   * @throws {Error} If fetch fails
   */
  get: (taskId, invocationId) =>
    api.get(`/tasks/${taskId}/invocations/${invocationId}`),

  /**
   * Gets matrix for an invocation
   * @param {string} taskId - Task ID
   * @param {string} invocationId - Invocation ID
   * @returns {Promise} Axios response with matrix data
   * @throws {Error} If fetch fails
   */
  matrix: (taskId, invocationId) =>
    api.get(`/tasks/${taskId}/invocations/${invocationId}/matrix`),
};

export default api;
