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
      const response = await invocationsAPI.create(taskId, {
        solutionIds: selectedSolutions,
        testIds: selectedTests,
      });
      setInvocations([...invocations, response.data]);
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

  if (loading) return <div>Loading...</div>;

  return (
    <div className="invocations-tab">
      <h2>Invocations</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div className="invocation-creator">
        <h3>Create New Invocation</h3>
        <div className="selector-section">
          <div className="solutions-selector">
            <h4>Select Solutions</h4>
            {solutions.length === 0 ? (
              <p>No solutions available</p>
            ) : (
              <div className="checkbox-list">
                {solutions.map((solution) => (
                  <label key={solution.id}>
                    <input
                      type="checkbox"
                      checked={selectedSolutions.includes(solution.id)}
                      onChange={() => toggleSolution(solution.id)}
                    />
                    {solution.name} ({solution.language})
                  </label>
                ))}
              </div>
            )}
          </div>

          <div className="tests-selector">
            <h4>Select Tests</h4>
            {tests.length === 0 ? (
              <p>No tests available</p>
            ) : (
              <div className="checkbox-list">
                {tests.map((test) => (
                  <label key={test.id}>
                    <input
                      type="checkbox"
                      checked={selectedTests.includes(test.id)}
                      onChange={() => toggleTest(test.id)}
                    />
                    Test {test.id}
                  </label>
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
            { key: "actions", label: "Actions" },
          ]}
          rows={invocations}
          renderCell={(inv, key) => {
            if (key === "id") return inv.id;
            if (key === "solutions") return inv.solutionIds?.length || 0;
            if (key === "tests") return inv.testIds?.length || 0;
            if (key === "status") return inv.status || "Pending";
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
                  { key: "solution", label: "Solution" },
                  ...(matrixData.tests?.map((test) => ({
                    key: `test-${test.id}`,
                    label: `Test ${test.id}`,
                  })) || []),
                  { key: "summary", label: "Summary" },
                ]}
                rows={matrixData.solutions || []}
                renderCell={(solution, key, index) => {
                  if (key === "solution") return solution.name;
                  if (key.startsWith("test-")) {
                    const testId = key.replace("test-", "");
                    const results = matrixData.results?.[solution.id] || {};
                    const result = results[testId];
                    return result ? (
                      <div>
                        <div className="verdict">{result.verdict}</div>
                        <div className="time">{result.timeMs}ms</div>
                        <div className="memory">{result.memoryKb}KB</div>
                      </div>
                    ) : (
                      <span>-</span>
                    );
                  }
                  if (key === "summary") {
                    const results = matrixData.results?.[solution.id] || {};
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
                        <div>Passed: {passedCount}</div>
                        <div>Max Time: {maxTime}ms</div>
                        <div>Max Memory: {maxMemory}KB</div>
                      </div>
                    );
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
