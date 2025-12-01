import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
  invocationsAPI,
  solutionsAPI,
  testsAPI,
  problemsAPI,
} from "../../services/api";
import Table from "../Table";
import InvocationModal from "../InvocationModal";

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
  const [invocationError, setInvocationError] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [showModal, setShowModal] = useState(false);

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
      setShowModal(false);
    } catch (err) {
      console.error("Failed to create invocation:", err);
      setError("Failed to create invocation");
    } finally {
      setCreating(false);
    }
  };

  const handleViewMatrix = async (invocationId) => {
    setMatrixLoading(true);
    setInvocationError(null);
    setMatrixData(null);

    const invocation = invocations.find((inv) => inv.id === invocationId);
    if (!invocation) {
      setInvocationError("Invocation not found");
      setMatrixLoading(false);
      return;
    }

    // If invocation is not completed, show error message
    if (invocation.status !== "COMPLETED") {
      const errorMsg =
        invocation.errorMessage ||
        `Invocation ${invocation.status.toLowerCase().replace("_", " ")}`;
      setInvocationError(errorMsg);
      setSelectedInvocation(invocationId);
      setMatrixLoading(false);
      return;
    }

    try {
      const response = await invocationsAPI.matrix(taskId, invocationId);
      const transformedData = transformMatrixData(response.data);
      setMatrixData(transformedData);
      setSelectedInvocation(invocationId);
    } catch (err) {
      console.error("Failed to load matrix:", err);
      setInvocationError("Failed to load matrix data");
      setSelectedInvocation(invocationId);
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
      const passedCount = solutionResults.filter(
        (r) => r?.verdict === "A",
      ).length;
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

  /**
   * Downloads the problem package
   */
  const handleDownloadPackage = async () => {
    setDownloading(true);
    setError("");

    try {
      // First check package status and generate if needed
      let statusResponse = await problemsAPI.getPackageStatus(taskId);
      let status = statusResponse.data.status;

      if (status === "ERROR") {
        throw new Error(
          statusResponse.data.message || "Package generation failed",
        );
      }

      if (status === "PENDING" || status === "IN_PROGRESS") {
        // Wait for package generation to complete
        let attempts = 0;
        const maxAttempts = 30; // 30 seconds max wait

        while (
          (status === "PENDING" || status === "IN_PROGRESS") &&
          attempts < maxAttempts
        ) {
          await new Promise((resolve) => setTimeout(resolve, 1000)); // Wait 1 second
          statusResponse = await problemsAPI.getPackageStatus(taskId);
          status = statusResponse.data.status;
          attempts++;
        }

        if (status === "ERROR") {
          throw new Error(
            statusResponse.data.message || "Package generation failed",
          );
        }

        if (status !== "COMPLETED") {
          throw new Error(
            "Package generation timed out. Please try again later.",
          );
        }
      }

      // Package is ready, download it
      const response = await problemsAPI.downloadPackage(taskId);
      // Create a download link and trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `problem_${taskId}_package.zip`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      setError("");
    } catch (err) {
      console.error("Failed to download package:", err);
      setError(
        "Failed to download package: " +
          (err.response?.data?.message || err.message || "Unknown error"),
      );
    } finally {
      setDownloading(false);
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
    <>
      <div className="invocations-tab">
        <div className="invocations-header">
          <h2>Invocations</h2>
          <div className="header-buttons">
            <button
              onClick={() => {
                setSelectedSolutions(solutions.map((s) => s.id));
                setSelectedTests(tests.map((t) => t.id));
                setShowModal(true);
              }}
              className="create-invocation-btn"
            >
              Create Invocation
            </button>
            <button
              onClick={handleDownloadPackage}
              disabled={downloading}
              className="download-btn"
              title="Download problem package"
            >
              {downloading ? (
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  className="download-icon spinning"
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
                  className="download-icon"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
                </svg>
              )}
            </button>
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
        </div>
        {error && <p style={{ color: "red" }}>{error}</p>}

        <div className="invocations-content">
          <div className="invocations-list">
            <div className="invocation-cards">
              {invocations.length === 0 ? (
                <p className="empty-message">No invocations yet</p>
              ) : (
                invocations.map((invocation) => (
                  <div
                    key={invocation.id}
                    className={`invocation-card ${
                      selectedInvocation === invocation.id ? "selected" : ""
                    } status-${invocation.status.toLowerCase()}`}
                    onClick={() => handleViewMatrix(invocation.id)}
                  >
                    <div className="invocation-status">
                      {invocation.status || "Pending"}
                    </div>
                    <div className="invocation-details">
                      <div className="invocation-id">ID: {invocation.id}</div>
                      <div className="invocation-time">
                        {invocation.createdAt
                          ? new Date(invocation.createdAt).toLocaleString()
                          : "N/A"}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="matrix-section">
            {selectedInvocation ? (
              <>
                <h3>Results Matrix</h3>
                {matrixLoading ? (
                  <p>Loading matrix...</p>
                ) : invocationError ? (
                  <div className="error-container">
                    <p style={{ color: "red", fontWeight: "bold" }}>
                      Error: {invocationError}
                    </p>
                  </div>
                ) : matrixData ? (
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
                            if (
                              cellData &&
                              cellData.passedCount !== undefined
                            ) {
                              return (
                                <div>
                                  Passed: {cellData.passedCount}{" "}
                                  <span
                                    style={{
                                      fontSize: "smaller",
                                      color: "grey",
                                    }}
                                  >
                                    Max: {cellData.maxTime}ms /{" "}
                                    {cellData.maxMemory}KB
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
                                    style={{
                                      fontSize: "smaller",
                                      color: "grey",
                                    }}
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
                ) : (
                  <div className="matrix-placeholder">
                    <p>No matrix data available</p>
                  </div>
                )}
              </>
            ) : (
              <div className="matrix-placeholder">
                <p>Select an invocation to view results matrix</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <InvocationModal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setError("");
        }}
        solutions={solutions}
        tests={tests}
        selectedSolutions={selectedSolutions}
        selectedTests={selectedTests}
        onToggleSolution={toggleSolution}
        onToggleTest={toggleTest}
        onSelectAllSolutions={selectAllSolutions}
        onSelectAllTests={selectAllTests}
        onCreate={handleCreateInvocation}
        creating={creating}
        error={error}
      />
    </>
  );
};

export default InvocationsTab;
