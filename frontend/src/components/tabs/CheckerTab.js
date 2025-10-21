import React, { useState, useEffect, useRef } from "react";
import { useParams } from "react-router-dom";
import { checkerAPI } from "../../services/api";
import Table from "../Table";
import EditableCell from "../EditableCell";

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
  const [newTest, setNewTest] = useState({
    input: "",
    output: "",
    expected: "",
    verdict: "OK",
  });
  const [showAddTestModal, setShowAddTestModal] = useState(false);
  const fileInputRef = useRef(null);

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
      await checkerAPI.createTest(taskId, newTest);
      const testsRes = await checkerAPI.listTests(taskId);
      setData((prevData) => ({
        ...prevData,
        tests: testsRes.data || [],
      }));
      setNewTest({ input: "", output: "", expected: "", verdict: "OK" });
      setShowAddTestModal(false);
      setError("");
    } catch (err) {
      console.error("Failed to add test:", err);
      setError("Failed to add test");
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
              placeholder="Enter checker source code..."
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
                  key: "output",
                  label: "Output",
                  editable: true,
                  type: "text",
                  placeholder: "Output",
                },
                {
                  key: "expected",
                  label: "Expected",
                  editable: true,
                  type: "text",
                  placeholder: "Expected",
                },
                {
                  key: "verdict",
                  label: "Verdict",
                  editable: true,
                  type: "select",
                  options: [
                    { value: "OK", label: "OK" },
                    { value: "WRONG_ANSWER", label: "WRONG_ANSWER" },
                    {
                      value: "PRESENTATION_ERROR",
                      label: "PRESENTATION_ERROR",
                    },
                    { value: "CRASHED", label: "CRASHED" },
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
                    output: columnKey === "output" ? newValue : test.output,
                    expected:
                      columnKey === "expected" ? newValue : test.expected,
                    verdict: columnKey === "verdict" ? newValue : test.verdict,
                  };
                  await checkerAPI.updateTest(taskId, rowId, updateData);
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
                if (key === "output") return test.output;
                if (key === "expected") return test.expected;
                if (key === "verdict") return test.verdict || "OK";
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
              <label>Output:</label>
              <textarea
                value={newTest.output}
                onChange={(e) =>
                  setNewTest({ ...newTest, output: e.target.value })
                }
                placeholder="Enter output"
              />
            </div>
            <div className="form-group">
              <label>Expected:</label>
              <textarea
                value={newTest.expected}
                onChange={(e) =>
                  setNewTest({ ...newTest, expected: e.target.value })
                }
                placeholder="Enter expected"
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
                <option value="OK">OK</option>
                <option value="WRONG_ANSWER">WRONG_ANSWER</option>
                <option value="PRESENTATION_ERROR">PRESENTATION_ERROR</option>
                <option value="CRASHED">CRASHED</option>
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

export default CheckerTab;
