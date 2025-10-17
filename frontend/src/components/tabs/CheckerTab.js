import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { checkerAPI } from "../../services/api";

/**
 * CheckerTab component for editing checker source and tests
 * @returns {React.Component} Checker tab
 */
const CheckerTab = () => {
  const { taskId } = useParams();
  const [data, setData] = useState({
    source: "",
    language: "cpp",
    tests: [],
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState("");
  const [runResults, setRunResults] = useState({});
  const [editingTest, setEditingTest] = useState(null);
  const [newTest, setNewTest] = useState({
    input: "",
    output: "",
    expected: "",
  });

  useEffect(() => {
    loadData();
  }, [taskId]);

  const loadData = async () => {
    try {
      const [sourceRes, testsRes] = await Promise.all([
        checkerAPI.getSource(taskId),
        checkerAPI.listTests(taskId),
      ]);
      setData({
        source: sourceRes.data.source || "",
        language: sourceRes.data.language || "cpp",
        tests: testsRes.data || [],
      });
      setRunResults(sourceRes.data.runResults || {});
    } catch (err) {
      console.error("Failed to load checker data:", err);
      setError("Failed to load checker data");
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSource = async () => {
    setSaving(true);
    try {
      await checkerAPI.updateSource(taskId, {
        source: data.source,
        language: data.language,
      });
      setError("");
    } catch (err) {
      console.error("Failed to save checker source:", err);
      setError("Failed to save checker source");
    } finally {
      setSaving(false);
    }
  };

  /**
   * Handles file upload for checker source
   * @param {Event} e - File input change event
   */
  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (event) => {
      const content = event.target.result;
      setData({ ...data, source: content });
      const formData = new FormData();
      formData.append("source", file);
      formData.append("language", data.language);
      if (process.env.REACT_APP_UI_TEST === "true") {
        try {
          await checkerAPI.updateSource(taskId, {
            source: content,
            language: data.language,
          });
          setError("");
        } catch (err) {
          console.error("Failed to upload source file:", err);
          setError("Failed to upload source file");
        }
      } else {
        try {
          await checkerAPI.uploadSource(taskId, formData);
          setError("");
        } catch (err) {
          console.error("Failed to upload source file:", err);
          setError("Failed to upload source file");
        }
      }
    };
    reader.readAsText(file);
  };

  const handleRunTests = async () => {
    setRunning(true);
    try {
      const response = await checkerAPI.runTests(taskId);
      setRunResults(response.data);
      setError("");
    } catch (err) {
      console.error("Failed to run tests:", err);
      setError("Failed to run tests");
    } finally {
      setRunning(false);
    }
  };

  const handleAddTest = async () => {
    try {
      const response = await checkerAPI.createTest(taskId, newTest);
      setData((prevData) => ({
        ...prevData,
        tests: [...prevData.tests, response.data],
      }));
      setNewTest({ input: "", output: "", expected: "" });
      setError("");
    } catch (err) {
      console.error("Failed to add test:", err);
      setError("Failed to add test");
    }
  };

  const handleEditTest = (test) => {
    setEditingTest(test);
  };

  const handleUpdateTest = async () => {
    try {
      await checkerAPI.updateTest(taskId, editingTest.id, editingTest);
      setData((prevData) => ({
        ...prevData,
        tests: prevData.tests.map((test) =>
          test.id === editingTest.id ? editingTest : test,
        ),
      }));
      setEditingTest(null);
      setError("");
    } catch (err) {
      console.error("Failed to update test:", err);
      setError("Failed to update test");
    }
  };

  const handleDeleteTest = async (testId) => {
    try {
      await checkerAPI.deleteTest(taskId, testId);
      setData((prevData) => ({
        ...prevData,
        tests: prevData.tests.filter((test) => test.id !== testId),
      }));
      setError("");
    } catch (err) {
      console.error("Failed to delete test:", err);
      setError("Failed to delete test");
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="checker-tab">
      <h2>Checker</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="checker-controls">
        <button onClick={handleSaveSource} disabled={saving}>
          {saving ? "Saving..." : "Save Source"}
        </button>
        <button onClick={handleRunTests} disabled={running}>
          {running ? "Running..." : "Run Tests"}
        </button>
      </div>
      <div className="checker-editor">
        <div className="checker-fields">
          <div className="field-group">
            <label>Language:</label>
            <select
              value={data.language}
              onChange={(e) => setData({ ...data, language: e.target.value })}
            >
              <option value="cpp">C++</option>
              <option value="python">Python</option>
              <option value="java">Java</option>
            </select>
          </div>
          <div className="field-group">
            <label>Source Code:</label>
            <input
              type="file"
              accept=".cpp,.py,.java,.c,.js"
              onChange={handleFileUpload}
            />
            <textarea
              value={data.source}
              onChange={(e) => setData({ ...data, source: e.target.value })}
              rows={10}
              placeholder="Enter checker source code..."
            />
          </div>
          <div className="field-group">
            <label>Tests:</label>
            <table>
              <thead>
                <tr>
                  <th>Input</th>
                  <th>Output</th>
                  <th>Expected</th>
                  <th>Verdict</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {data.tests.map((test) => (
                  <tr key={test.id}>
                    <td>{test.input}</td>
                    <td>{test.output}</td>
                    <td>{test.expected}</td>
                    <td>{runResults[test.id] || test.verdict || "N/A"}</td>
                    <td>
                      <button onClick={() => handleEditTest(test)}>Edit</button>
                      <button onClick={() => handleDeleteTest(test.id)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="add-test">
              <h4>Add New Test</h4>
              <input
                type="text"
                placeholder="Input"
                value={newTest.input}
                onChange={(e) =>
                  setNewTest({ ...newTest, input: e.target.value })
                }
              />
              <input
                type="text"
                placeholder="Output"
                value={newTest.output}
                onChange={(e) =>
                  setNewTest({ ...newTest, output: e.target.value })
                }
              />
              <input
                type="text"
                placeholder="Expected"
                value={newTest.expected}
                onChange={(e) =>
                  setNewTest({ ...newTest, expected: e.target.value })
                }
              />
              <button onClick={handleAddTest}>Add Test</button>
            </div>
            {editingTest && (
              <div className="edit-test">
                <h4>Edit Test</h4>
                <input
                  type="text"
                  placeholder="Input"
                  value={editingTest.input}
                  onChange={(e) =>
                    setEditingTest({ ...editingTest, input: e.target.value })
                  }
                />
                <input
                  type="text"
                  placeholder="Output"
                  value={editingTest.output}
                  onChange={(e) =>
                    setEditingTest({ ...editingTest, output: e.target.value })
                  }
                />
                <input
                  type="text"
                  placeholder="Expected"
                  value={editingTest.expected}
                  onChange={(e) =>
                    setEditingTest({ ...editingTest, expected: e.target.value })
                  }
                />
                <button onClick={handleUpdateTest}>Update Test</button>
                <button onClick={() => setEditingTest(null)}>Cancel</button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckerTab;
