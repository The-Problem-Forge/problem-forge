import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { invocationsAPI, solutionsAPI, testsAPI } from "../../services/api";
import Table from "../Table";

/**
 * InvocationsTab component for managing invocations and viewing results matrix
 * @returns {React.Component} Invocations tab
 */
const InvocationsTab = () => {
  const { taskId } = useParams();
  const [invocations, setInvocations] = useState([]);
  const [solutions, setSolutions] = useState([]);
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [selectedSolutions, setSelectedSolutions] = useState([]);
  const [selectedTests, setSelectedTests] = useState([]);
  const [error, setError] = useState("");
  const [selectedInvocation, setSelectedInvocation] = useState(null);
  const [matrixData, setMatrixData] = useState(null);
  const [matrixLoading, setMatrixLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadData();
  }, [taskId]);

  const loadData = async () => {
    try {
      const [invRes, solRes, testRes] = await Promise.all([
        invocationsAPI.list(taskId),
        solutionsAPI.list(taskId),
        testsAPI.list(taskId),
      ]);
      setInvocations(invRes.data || []);
      setSolutions(solRes.data || []);
      setTests(testRes.data || []);
    } catch (err) {
      console.error("Failed to load invocations data:", err);
      setError("Failed to load invocations data");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateInvocation = async () => {
    if (selectedSolutions.length === 0 || selectedTests.length === 0) {
      setError("Please select at least one solution and one test");
      return;
    }

    setCreating(true);
    try {
      await invocationsAPI.create(taskId, {
        solutionIds: selectedSolutions,
        testIds: selectedTests,
      });
      await loadData();
      setSelectedSolutions([]);
      setSelectedTests([]);
      setError("");
    } catch (err) {
      console.error("Failed to create invocation:", err);
      setError("Failed to create invocation");
    } finally {
      setCreating(false);
    }
  };

  const handleViewMatrix = async (invocationId) => {
    setMatrixLoading(true);
    try {
      const response = await invocationsAPI.matrix(taskId, invocationId);
      const transformedData = transformMatrixData(response.data);
      setMatrixData(transformedData);
      setSelectedInvocation(invocationId);
    } catch (err) {
      console.error("Failed to load matrix:", err);
      setError("Failed to load matrix");
    } finally {
      setMatrixLoading(false);
    }
  };

  /**
   * Transforms backend matrix data into a Table-friendly structure
   * @param {Object} matrixData - Raw matrix data from backend
   * @returns {Object} Transformed data with headers and rows
   */
  const transformMatrixData = (data) => {
    if (!data || !data.solutions || !data.tests || !data.results) {
      return { headers: [], rows: [] };
    }

    // Create headers: Test column + one column per solution
    const headers = [
      { key: "testName", label: "Test" },
      ...data.solutions.map((solution) => ({
        key: `solution_${solution.id}`,
        label: solution.name,
      })),
    ];

    // Create rows: one per test + summary row
    const rows = data.tests.map((test) => {
      const row = {
        testName: `Test ${test.testNumber}`,
        isSummary: false,
        testNumber: test.testNumber,
      };

      // Add result for each solution
      data.solutions.forEach((solution, i) => {
        const solutionResults = data.results[i + 1] || [];
        const result = solutionResults.find(
          (r) => r.testNumber === test.testNumber,
        );

        row[`solution_${solution.id}`] = result || null;
      });

      return row;
    });

    // Add summary row
    const summaryRow = {
      testName: "Summary",
      isSummary: true,
    };

    data.solutions.forEach((solution, i) => {
      const solutionResults = data.results[i + 1] || [];
      const passedCount = solutionResults.filter((r) => r?.verdict === "A").length;
      const maxTime = Math.max(
        ...solutionResults.map((r) => r?.timeMs || 0).filter((t) => t > 0),
        0,
      );
      const maxMemory = Math.max(
        ...solutionResults.map((r) => r?.memoryKb || 0).filter((m) => m > 0),
        0,
      );

      summaryRow[`solution_${solution.id}`] = {
        passedCount,
        maxTime,
        maxMemory,
      };
    });

    rows.push(summaryRow);

    return { headers, rows };
  };

  /**
   * Refreshes the invocations list
   */
  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      const response = await invocationsAPI.list(taskId);
      setInvocations(response.data || []);
      setError("");
    } catch (err) {
      console.error("Failed to refresh invocations:", err);
      setError("Failed to refresh invocations");
    } finally {
      setRefreshing(false);
    }
  };

  const toggleSolution = (solutionId) => {
    setSelectedSolutions((prev) =>
      prev.includes(solutionId)
        ? prev.filter((id) => id !== solutionId)
        : [...prev, solutionId],
    );
  };

  const toggleTest = (testId) => {
    setSelectedTests((prev) =>
      prev.includes(testId)
        ? prev.filter((id) => id !== testId)
        : [...prev, testId],
    );
  };

  const selectAllSolutions = () => {
    const allIds = solutions.map((solution) => solution.id);
    const isAllSelected = allIds.every((id) => selectedSolutions.includes(id));
    setSelectedSolutions(isAllSelected ? [] : allIds);
  };

  const selectAllTests = () => {
    const allIds = tests.map((test) => test.id);
    const isAllSelected = allIds.every((id) => selectedTests.includes(id));
    setSelectedTests(isAllSelected ? [] : allIds);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="invocations-tab">
      <h2>Invocations</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div className="invocation-creator">
        <div className="selector-section">
          <div className="solutions-selector">
            <h4>Select Solutions</h4>
            {solutions.length > 0 && (
              <button onClick={selectAllSolutions} className="select-all-btn">
                {solutions.every((solution) =>
                  selectedSolutions.includes(solution.id),
                )
                  ? "Deselect All"
                  : "Select All"}
              </button>
            )}
            {solutions.length === 0 ? (
              <p>No solutions available</p>
            ) : (
              <div className="selector-list">
                {solutions.map((solution) => (
                  <div
                    key={solution.id}
                    className={`selector-item ${
                      selectedSolutions.includes(solution.id) ? "selected" : ""
                    }`}
                    onClick={() => toggleSolution(solution.id)}
                  >
                    <div>{solution.name}</div>
                    <div className="subtext">
                      {solution.language}, {solution.solutionType}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="tests-selector">
            <h4>Select Tests</h4>
            {tests.length > 0 && (
              <button onClick={selectAllTests} className="select-all-btn">
                {tests.every((test) => selectedTests.includes(test.id))
                  ? "Deselect All"
                  : "Select All"}
              </button>
            )}
            {tests.length === 0 ? (
              <p>No tests available</p>
            ) : (
              <div className="selector-list">
                {tests.map((test) => (
                  <div
                    key={test.id}
                    className={`selector-item ${
                      selectedTests.includes(test.id) ? "selected" : ""
                    }`}
                    onClick={() => toggleTest(test.id)}
                  >
                    Test {test.id}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="invocation-buttons">
          <button
            onClick={handleCreateInvocation}
            disabled={creating}
            className="create-invocation-btn"
          >
            {creating ? "Creating..." : "Create Invocation"}
          </button>
        </div>
      </div>

      <div className="invocations-list">
        <div className="invocations-header">
          <h3>Invocations</h3>
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="refresh-btn"
            title="Refresh invocations"
          >
            {refreshing ? (
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                className="refresh-icon spinning"
              >
                <path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
              </svg>
            ) : (
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                className="refresh-icon"
              >
                <path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
              </svg>
            )}
          </button>
        </div>
        <Table
          headers={[
            { key: "id", label: "ID" },
            { key: "solutions", label: "Solutions" },
            { key: "tests", label: "Tests" },
            { key: "status", label: "Status" },
            { key: "createdAt", label: "Created At" },
            { key: "actions", label: "Actions" },
          ]}
          rows={invocations}
          renderCell={(inv, key) => {
            if (key === "id") return inv.id;
            if (key === "solutions") return inv.solutionIds?.length || 0;
            if (key === "tests") return inv.testIds?.length || 0;
            if (key === "status") return inv.status || "Pending";
            if (key === "createdAt")
              return inv.createdAt
                ? new Date(inv.createdAt).toLocaleString()
                : "N/A";
            if (key === "actions") {
              return (
                <button onClick={() => handleViewMatrix(inv.id)}>
                  View Matrix
                </button>
              );
            }
            return null;
          }}
          emptyMessage="No invocations yet"
        />
      </div>

      {selectedInvocation && matrixData && (
        <div className="matrix-section">
          <h3>Results Matrix</h3>
          {matrixLoading ? (
            <p>Loading matrix...</p>
          ) : (
            <div className="matrix-container">
              <Table
                className="results-matrix"
                headers={matrixData.headers}
                rows={matrixData.rows}
                renderCell={(row, key) => {
                  if (key === "testName") {
                    return row.testName;
                  }

                  // Handle solution columns
                  if (key.startsWith("solution_")) {
                    const cellData = row[key];

                    if (row.isSummary) {
                      // Summary row
                      if (cellData && cellData.passedCount !== undefined) {
                        return (
                          <div>
                            Passed: {cellData.passedCount}{" "}
                            <span
                              style={{ fontSize: "smaller", color: "grey" }}
                            >
                              Max: {cellData.maxTime}ms / {cellData.maxMemory}KB
                            </span>
                          </div>
                        );
                      }
                      return <span>-</span>;
                    } else {
                      // Test result row
                      if (cellData) {
                        return (
                          <div>
                            {cellData.description}{" "}
                            <span
                              style={{ fontSize: "smaller", color: "grey" }}
                            >
                              {cellData.timeMs}ms / {cellData.memoryKb}KB
                            </span>
                          </div>
                        );
                      }
                      return <span>-</span>;
                    }
                  }

                  return null;
                }}
                emptyMessage="No matrix data"
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default InvocationsTab;
