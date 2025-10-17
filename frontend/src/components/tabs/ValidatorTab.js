import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { validatorAPI } from "../../services/api";
import Table from "../Table";

/**
 * ValidatorTab component for editing validator source and tests
 * @returns {React.Component} Validator tab
 */
const ValidatorTab = () => {
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
        validatorAPI.getSource(taskId),
        validatorAPI.listTests(taskId),
      ]);
      setData({
        source: sourceRes.data.source || "",
        language: sourceRes.data.language || "cpp",
        tests: testsRes.data || [],
      });
      setRunResults(sourceRes.data.runResults || {});
    } catch (err) {
      console.error("Failed to load validator data:", err);
      setError("Failed to load validator data");
    } finally {
      setLoading(false);
    }
  };

  const handleSaveSource = async () => {
    setSaving(true);
    try {
      await validatorAPI.updateSource(taskId, {
        source: data.source,
        language: data.language,
      });
      setError("");
    } catch (err) {
      console.error("Failed to save validator source:", err);
      setError("Failed to save validator source");
    } finally {
      setSaving(false);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const content = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => resolve(event.target.result);
      reader.onerror = reject;
      reader.readAsText(file);
    });
    setData({ ...data, source: content });
    const formData = new FormData();
    formData.append("source", file);
    formData.append("language", data.language);
    try {
      if (process.env.REACT_APP_UI_TEST === "true") {
        await validatorAPI.updateSource(taskId, {
          source: content,
          language: data.language,
        });
      } else {
        await validatorAPI.uploadSource(taskId, formData);
      }
      setError("");
    } catch (err) {
      console.error("Failed to upload source file:", err);
      setError("Failed to upload source file");
    }
  };

  const handleRunTests = async () => {
    setRunning(true);
    try {
      const response = await validatorAPI.runTests(taskId);
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
      const response = await validatorAPI.createTest(taskId, newTest);
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
      await validatorAPI.updateTest(taskId, editingTest.id, editingTest);
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
      await validatorAPI.deleteTest(taskId, testId);
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
    <div className="validator-tab">
      <h2>Validator</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="validator-controls">
        <button onClick={handleSaveSource} disabled={saving}>
          {saving ? "Saving..." : "Save Source"}
        </button>
        <button onClick={handleRunTests} disabled={running}>
          {running ? "Running..." : "Run Tests"}
        </button>
      </div>
      <div className="validator-editor">
        <div className="validator-fields">
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
              placeholder="Enter validator source code..."
            />
          </div>
          <div className="field-group">
            <label>Tests:</label>
            <Table
              headers={[
                {
                  key: "input",
                  label: "Input",
                  editable: true,
                  type: "textarea",
                },
                {
                  key: "output",
                  label: "Output",
                  editable: true,
                  type: "textarea",
                },
                {
                  key: "expected",
                  label: "Expected",
                  editable: true,
                  type: "textarea",
                },
                {
                  key: "verdict",
                  label: "Verdict",
                  editable: true,
                  type: "select",
                  options: [
                    { value: "VALID", label: "VALID" },
                    { value: "INVALID", label: "INVALID" },
                  ],
                },
                { key: "actions", label: "Actions" },
              ]}
              rows={data.tests}
              onSave={(testId, key, value) => {
                const updatedTest = data.tests.find(
                  (test) => test.id === testId,
                );
                if (updatedTest) {
                  handleUpdateTest({ ...updatedTest, [key]: value });
                }
              }}
              renderActions={(test) => (
                <>
                  <button onClick={() => handleDeleteTest(test.id)}>
                    Delete
                  </button>
                </>
              )}
              emptyMessage="No tests yet"
            />
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

export default ValidatorTab;
