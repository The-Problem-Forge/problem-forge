import React, { useState, useEffect, useRef } from "react";
import { useParams } from "react-router-dom";
import { validatorAPI } from "../../services/api";
import Table from "../Table";
import EditableCell from "../EditableCell";

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
  const [newTest, setNewTest] = useState({
    input: "",
    verdict: "VALID",
  });
  const [showAddTestModal, setShowAddTestModal] = useState(false);
  const fileInputRef = useRef(null);

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

  /**
   * Handles file upload for validator source
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
          await validatorAPI.updateSource(taskId, {
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
          await validatorAPI.uploadSource(taskId, formData);
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
      await validatorAPI.createTest(taskId, newTest);
      const testsRes = await validatorAPI.listTests(taskId);
      setData((prevData) => ({
        ...prevData,
        tests: testsRes.data || [],
      }));
      setNewTest({ input: "", verdict: "VALID" });
      setShowAddTestModal(false);
      setError("");
    } catch (err) {
      console.error("Failed to add test:", err);
      setError("Failed to add test");
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
            <div className="source-controls">
              <button onClick={() => fileInputRef.current.click()}>
                Load from file
              </button>
              <button onClick={handleSaveSource} disabled={saving}>
                {saving ? "Saving..." : "Save Source"}
              </button>
            </div>
            <input
              type="file"
              accept=".cpp,.py,.java,.c,.js"
              onChange={handleFileUpload}
              ref={fileInputRef}
              style={{ display: "none" }}
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
            <div className="test-controls">
              <button onClick={handleRunTests} disabled={running}>
                {running ? "Running..." : "Run Tests"}
              </button>
              <button onClick={() => setShowAddTestModal(true)}>
                Add test
              </button>
            </div>
            <Table
              headers={[
                {
                  key: "input",
                  label: "Input",
                  editable: true,
                  type: "text",
                  placeholder: "Input",
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
              onSave={async (rowId, columnKey, newValue) => {
                try {
                  const test = data.tests.find((t) => t.id === rowId);
                  const updateData = {
                    input: columnKey === "input" ? newValue : test.input,
                    verdict: columnKey === "verdict" ? newValue : test.verdict,
                  };
                  await validatorAPI.updateTest(taskId, rowId, updateData);
                  setData((prevData) => ({
                    ...prevData,
                    tests: prevData.tests.map((test) =>
                      test.id === rowId ? { ...test, ...updateData } : test,
                    ),
                  }));
                  setError("");
                } catch (err) {
                  console.error("Failed to update test:", err);
                  setError("Failed to update test");
                }
              }}
              renderCell={(test, key) => {
                if (key === "input") return test.input;
                if (key === "verdict") return test.verdict || "VALID";
                return null;
              }}
              renderActions={(test) => (
                <button onClick={() => handleDeleteTest(test.id)}>
                  Delete
                </button>
              )}
              emptyMessage="No tests yet"
            />
          </div>
        </div>
      </div>
      {showAddTestModal && (
        <div className="modal" onClick={() => setShowAddTestModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Add New Test</h3>
            <div className="form-group">
              <label>Input:</label>
              <textarea
                value={newTest.input}
                onChange={(e) =>
                  setNewTest({ ...newTest, input: e.target.value })
                }
                placeholder="Enter input"
              />
            </div>
            <div className="form-group">
              <label>Verdict:</label>
              <select
                value={newTest.verdict}
                onChange={(e) =>
                  setNewTest({ ...newTest, verdict: e.target.value })
                }
              >
                <option value="VALID">VALID</option>
                <option value="INVALID">INVALID</option>
              </select>
            </div>
            <div className="modal-buttons">
              <button onClick={handleAddTest}>Add</button>
              <button onClick={() => setShowAddTestModal(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ValidatorTab;
