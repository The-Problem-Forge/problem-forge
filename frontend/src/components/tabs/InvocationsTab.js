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
      setMatrixData(response.data);
      setSelectedInvocation(invocationId);
    } catch (err) {
      console.error("Failed to load matrix:", err);
      setError("Failed to load matrix");
    } finally {
      setMatrixLoading(false);
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
                      {solution.language}, {solution.type}
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

        <button
          onClick={handleCreateInvocation}
          disabled={creating}
          className="create-invocation-btn"
        >
          {creating ? "Creating..." : "Create Invocation"}
        </button>
      </div>

      <div className="invocations-list">
        <h3>Invocations</h3>
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
                headers={[
                  { key: "test", label: "Test" },
                  ...(matrixData.solutions?.map((solution) => ({
                    key: `solution-${solution.id}`,
                    label: solution.name,
                  })) || []),
                ]}
                rows={[...(matrixData.tests || []), { isSummary: true }]}
                renderCell={(row, key, index) => {
                  if (row.isSummary) {
                    if (key === "test") return "Summary";
                    if (key.startsWith("solution-")) {
                      const solutionId = key.replace("solution-", "");
                      const results = matrixData.results?.[solutionId] || {};
                      const passedCount = Object.values(results).filter(
                        (r) => r?.verdict === "OK" || r?.verdict === "AC",
                      ).length;
                      const maxTime = Math.max(
                        ...Object.values(results)
                          .map((r) => r?.timeMs || 0)
                          .filter((t) => t > 0),
                      );
                      const maxMemory = Math.max(
                        ...Object.values(results)
                          .map((r) => r?.memoryKb || 0)
                          .filter((m) => m > 0),
                      );
                      return (
                        <div>
                          Passed: {passedCount}{" "}
                          <span style={{ fontSize: "smaller", color: "grey" }}>
                            Max: {maxTime}ms / {maxMemory}KB
                          </span>
                        </div>
                      );
                    }
                    return null;
                  } else {
                    if (key === "test") return `Test ${row.id}`;
                    if (key.startsWith("solution-")) {
                      const solutionId = key.replace("solution-", "");
                      const results = matrixData.results?.[solutionId] || {};
                      const result = results[row.id];
                      return result ? (
                        <div>
                          {result.verdict}{" "}
                          <span style={{ fontSize: "smaller", color: "grey" }}>
                            {result.timeMs}ms / {result.memoryKb}KB
                          </span>
                        </div>
                      ) : (
                        <span>-</span>
                      );
                    }
                    return null;
                  }
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
