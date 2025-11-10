import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { testsAPI, generatorsAPI } from "../../services/api";
import Table from "../Table";
import EditableCell from "../EditableCell";

/**
 * TestsTab component for problem tests management
 * @returns {React.Component} Tests tab
 */
const TestsTab = () => {
  const { taskId } = useParams();
  const [tests, setTests] = useState([]);
  const [generators, setGenerators] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [addMode, setAddMode] = useState("text"); // text, file, generator
  const [newTest, setNewTest] = useState({
    inputText: "",
    description: "",
    points: 1,
    sample: false,
  });
  const [selectedGenerator, setSelectedGenerator] = useState(null);
  const [generatorArgs, setGeneratorArgs] = useState("");
  const [showGeneratorModal, setShowGeneratorModal] = useState(false);
  const [generatorFile, setGeneratorFile] = useState(null);
  const [generatorLanguage, setGeneratorLanguage] = useState("");

  useEffect(() => {
    loadData();
  }, [taskId]);

  const loadData = async () => {
    try {
      const [testsRes, generatorsRes] = await Promise.all([
        testsAPI.list(taskId),
        generatorsAPI.list(taskId),
      ]);

      setTests(testsRes.data || []);
      setGenerators(generatorsRes.data || []);
    } catch (err) {
      console.error("Failed to load tests data:", err);
      setError("Failed to load tests data");
    } finally {
      setLoading(false);
    }
  };

  const handleAddTest = async (e) => {
    e.preventDefault();
    if (addMode === "text" && !newTest.inputText.trim()) return;

    try {
      let response;
      if (addMode === "text") {
        response = await testsAPI.createText(taskId, newTest);
      } else if (addMode === "file" && generatorFile) {
        // Read file content and send as text
        const fileContent = await new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = (e) => resolve(e.target.result);
          reader.onerror = reject;
          reader.readAsText(generatorFile);
        });

        const testData = {
          inputText: fileContent,
          description: newTest.description || "",
          points: newTest.points || 1,
          sample: newTest.sample || false,
        };
        response = await testsAPI.createText(taskId, testData);
      } else if (addMode === "generator" && selectedGenerator) {
        // Add test with GENERATED type and bash command as content
        const command =
          `${selectedGenerator.alias} ${generatorArgs || ""}`.trim();
        const testData = {
          inputText: command,
          description: newTest.description,
          points: newTest.points || 1,
          sample: newTest.sample || false,
          testType: "GENERATED",
        };
        response = await testsAPI.createText(taskId, testData);
      }
      if (response) {
        // Reload data to get updated preview with outputs
        await loadData();
        setNewTest({
          inputText: "",
          description: "",
          points: 1,
          sample: false,
        });
        setGeneratorFile(null);
        setSelectedGenerator(null);
        setGeneratorArgs("");
        setShowAddModal(false);
        setError("");
      }
    } catch (err) {
      console.error("Failed to add test:", err);
      setError("Failed to add test");
    }
  };

  const handleDeleteTest = async (testId) => {
    try {
      await testsAPI.delete(taskId, testId);
      // Reload data to get updated preview
      await loadData();
      setError("");
    } catch (err) {
      console.error("Failed to delete test:", err);
      setError("Failed to delete test");
    }
  };

  const handleUploadGenerator = async (e) => {
    e.preventDefault();
    if (!generatorFile || !generatorLanguage) {
      setError("Generator file and language are required");
      return;
    }

    try {
      // Read file content and convert to base64
      const fileContent = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          // Convert to base64
          const base64 = btoa(e.target.result);
          resolve(base64);
        };
        reader.onerror = reject;
        reader.readAsBinaryString(generatorFile);
      });

      // Determine format based on language
      let format;
      switch (generatorLanguage.toLowerCase()) {
        case "cpp":
          format = "CPP_17";
          break;
        case "python":
          format = "PYTHON";
          break;
        case "java":
          format = "JAVA_17";
          break;
        default:
          format = "TEXT";
      }

      // Generate alias from filename
      const filename = generatorFile.name;
      const alias =
        filename.substring(0, filename.lastIndexOf(".")) || filename;

      const generatorData = {
        file: fileContent,
        format: format,
        alias: alias,
      };

      await generatorsAPI.create(taskId, generatorData);
      setGeneratorFile(null);
      setGeneratorLanguage("");
      await loadData();
      setShowGeneratorModal(false);
      setError("");
    } catch (err) {
      console.error("Failed to upload generator:", err);
      setError("Failed to upload generator");
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="tests-tab">
      <h2>Tests</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="tests-controls">
        <button onClick={() => setShowAddModal(true)}>Add Test</button>
        <button onClick={() => setShowGeneratorModal(true)}>
          Upload Generator
        </button>
      </div>
      <div className="tests-editor">
        <div className="tests-fields">
          <div className="field-group">
            <Table
              headers={[
                {
                  key: "input",
                  label: "Input",
                  type: "textarea",
                  placeholder: "Input text",
                  editable: true,
                },
                {
                  key: "description",
                  label: "Description",
                  type: "text",
                  placeholder: "Description",
                  editable: true,
                },
                {
                  key: "points",
                  label: "Points",
                  type: "number",
                  placeholder: "Points",
                  editable: true,
                },
                {
                  key: "sample",
                  label: "Sample",
                  type: "checkbox",
                  editable: true,
                },
                { key: "actions", label: "Actions" },
              ]}
              rows={tests}
              renderCell={(row, key, index, isEditing, onEdit) => {
                if (key === "input") {
                  const inputText = row[key] || "";
                  const truncated =
                    inputText.length > 100
                      ? inputText.substring(0, 100) + "..."
                      : inputText;
                  return (
                    <span
                      title={inputText}
                      style={{ cursor: "pointer" }}
                      onClick={onEdit}
                    >
                      {truncated}
                    </span>
                  );
                }
                return null; // Let default rendering handle other columns
              }}
              onSave={async (rowId, columnKey, newValue) => {
                try {
                  const test = tests.find((t) => t.id === rowId);
                  const updateData = {
                    inputText: test?.input || "",
                    description: test?.description || "",
                    points: test?.points || 1,
                    sample: test?.sample || false,
                  };

                  if (columnKey === "input") {
                    updateData.inputText = newValue;
                  } else if (columnKey === "description") {
                    updateData.description = newValue;
                  } else if (columnKey === "points") {
                    updateData.points = parseInt(newValue) || 1;
                  } else if (columnKey === "sample") {
                    updateData.sample = newValue;
                  }

                  await testsAPI.update(taskId, rowId, updateData);
                  const res = await testsAPI.list(taskId);
                  setTests(res.data || []);
                  setError("");
                } catch (err) {
                  console.error("Failed to update test:", err);
                  setError("Failed to update test");
                }
              }}
              renderActions={(test) => (
                <button onClick={() => handleDeleteTest(test.id)}>
                  Delete
                </button>
              )}
              emptyMessage="No tests yet"
            />
          </div>
          <div className="field-group">
            <label>Generators:</label>
            <Table
              headers={[
                { key: "alias", label: "Generator" },
                { key: "language", label: "Language" },
              ]}
              rows={generators}
              renderCell={(gen, key) => {
                if (key === "alias") return gen.alias;
                if (key === "language") return gen.language || "N/A";
                return null;
              }}
              emptyMessage="No generators yet"
            />
          </div>
        </div>
      </div>

      {showAddModal && (
        <div className="modal">
          <div className="modal-content">
            <h3>Add Test</h3>
            <div className="modal-tabs">
              <button
                className={addMode === "text" ? "active" : ""}
                onClick={() => setAddMode("text")}
              >
                Manual
              </button>
              <button
                className={addMode === "file" ? "active" : ""}
                onClick={() => setAddMode("file")}
              >
                File Upload
              </button>
              <button
                className={addMode === "generator" ? "active" : ""}
                onClick={() => setAddMode("generator")}
              >
                From Generator
              </button>
            </div>

            {addMode === "text" && (
              <form onSubmit={handleAddTest}>
                <div className="form-group">
                  <label>Input Text</label>
                  <textarea
                    placeholder="Input text"
                    value={newTest.inputText}
                    onChange={(e) =>
                      setNewTest({ ...newTest, inputText: e.target.value })
                    }
                    rows={5}
                  />
                </div>

                <div className="form-group">
                  <label>Description (optional)</label>
                  <input
                    type="text"
                    placeholder="Description"
                    value={newTest.description}
                    onChange={(e) =>
                      setNewTest({ ...newTest, description: e.target.value })
                    }
                  />
                </div>

                <div className="form-group">
                  <label>Points</label>
                  <input
                    type="number"
                    placeholder="Points"
                    value={newTest.points}
                    onChange={(e) =>
                      setNewTest({
                        ...newTest,
                        points: parseInt(e.target.value) || 1,
                      })
                    }
                    min="1"
                  />
                </div>

                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={newTest.sample}
                      onChange={(e) =>
                        setNewTest({ ...newTest, sample: e.target.checked })
                      }
                    />
                    Sample test
                  </label>
                </div>

                <div className="modal-buttons">
                  <button type="submit">Add Test</button>
                  <button type="button" onClick={() => setShowAddModal(false)}>
                    Cancel
                  </button>
                </div>
              </form>
            )}

            {addMode === "file" && (
              <form onSubmit={handleAddTest}>
                <div className="form-group">
                  <label>Test File</label>
                  <input
                    type="file"
                    onChange={(e) => setGeneratorFile(e.target.files[0])}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Description (optional)</label>
                  <input
                    type="text"
                    placeholder="Description"
                    value={newTest.description}
                    onChange={(e) =>
                      setNewTest({ ...newTest, description: e.target.value })
                    }
                  />
                </div>
                <div className="form-group">
                  <label>Points</label>
                  <input
                    type="number"
                    placeholder="Points"
                    value={newTest.points}
                    onChange={(e) =>
                      setNewTest({
                        ...newTest,
                        points: parseInt(e.target.value) || 1,
                      })
                    }
                    min="1"
                  />
                </div>
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={newTest.sample}
                      onChange={(e) =>
                        setNewTest({ ...newTest, sample: e.target.checked })
                      }
                    />
                    Sample test
                  </label>
                </div>
                <div className="modal-buttons">
                  <button type="submit">Add Test</button>
                  <button
                    type="button"
                    className="cancel-btn"
                    onClick={() => setShowAddModal(false)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}

            {addMode === "generator" && (
              <form onSubmit={handleAddTest}>
                <div className="form-group">
                  <label>Generator</label>
                  <select
                    value={selectedGenerator ? selectedGenerator.id : ""}
                    onChange={(e) => {
                      const gen = generators.find(
                        (g) => g.id === e.target.value,
                      );
                      setSelectedGenerator(gen || null);
                    }}
                    required
                  >
                    <option value="">Select generator</option>
                    {generators.map((gen) => (
                      <option key={gen.id} value={gen.id}>
                        {gen.alias || gen.name || gen.id}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Arguments (optional)</label>
                  <input
                    type="text"
                    placeholder="Arguments"
                    value={generatorArgs}
                    onChange={(e) => setGeneratorArgs(e.target.value)}
                  />
                </div>

                <div className="modal-buttons">
                  <button type="submit">Add Test</button>
                  <button
                    type="button"
                    className="cancel-btn"
                    onClick={() => setShowAddModal(false)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {showGeneratorModal && (
        <div className="modal">
          <div className="modal-content">
            <h3>Upload Generator</h3>
            <form onSubmit={handleUploadGenerator}>
              <div className="form-group">
                <label>Generator File</label>
                <input
                  type="file"
                  onChange={(e) => setGeneratorFile(e.target.files[0])}
                  required
                />
              </div>
              <div className="form-group">
                <label>Language</label>
                <select
                  value={generatorLanguage}
                  onChange={(e) => setGeneratorLanguage(e.target.value)}
                  required
                >
                  <option value="">Select language</option>
                  <option value="cpp">C++</option>
                  <option value="python">Python</option>
                  <option value="java">Java</option>
                </select>
              </div>
              <div className="modal-buttons">
                <button type="submit">Upload</button>
                <button
                  type="button"
                  className="cancel-btn"
                  onClick={() => setShowGeneratorModal(false)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TestsTab;
