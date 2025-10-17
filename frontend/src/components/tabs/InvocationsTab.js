import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { invocationsAPI, solutionsAPI, testsAPI } from "../../services/api";

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
        {invocations.length === 0 ? (
          <p>No invocations yet</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Solutions</th>
                <th>Tests</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {invocations.map((inv) => (
                <tr key={inv.id}>
                  <td>{inv.id}</td>
                  <td>{inv.solutionIds?.length || 0}</td>
                  <td>{inv.testIds?.length || 0}</td>
                  <td>{inv.status || "Pending"}</td>
                  <td>
                    <button onClick={() => handleViewMatrix(inv.id)}>
                      View Matrix
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {selectedInvocation && matrixData && (
        <div className="matrix-section">
          <h3>Results Matrix</h3>
          {matrixLoading ? (
            <p>Loading matrix...</p>
          ) : (
            <div className="matrix-container">
              <table className="results-matrix">
                <thead>
                  <tr>
                    <th>Solution</th>
                    {matrixData.tests?.map((test) => (
                      <th key={test.id}>Test {test.id}</th>
                    ))}
                    <th>Summary</th>
                  </tr>
                </thead>
                <tbody>
                  {matrixData.solutions?.map((solution) => {
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
                      <tr key={solution.id}>
                        <td>{solution.name}</td>
                        {matrixData.tests?.map((test) => {
                          const result = results[test.id];
                          return (
                            <td key={test.id} className="result-cell">
                              {result ? (
                                <div>
                                  <div className="verdict">
                                    {result.verdict}
                                  </div>
                                  <div className="time">{result.timeMs}ms</div>
                                  <div className="memory">
                                    {result.memoryKb}KB
                                  </div>
                                </div>
                              ) : (
                                <span>-</span>
                              )}
                            </td>
                          );
                        })}
                        <td className="summary-cell">
                          <div>Passed: {passedCount}</div>
                          <div>Max Time: {maxTime}ms</div>
                          <div>Max Memory: {maxMemory}KB</div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default InvocationsTab;
