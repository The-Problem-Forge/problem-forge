import React, { useState, useEffect } from "react";
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
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckerTab;
