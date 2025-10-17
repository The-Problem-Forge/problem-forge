import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { testsAPI, generatorsAPI } from "../../services/api";

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
  const [editingTestId, setEditingTestId] = useState(null);
  const [editInput, setEditInput] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [addMode, setAddMode] = useState("text"); // text, file, generator
  const [newTest, setNewTest] = useState({ inputText: "", description: "" });
  const [selectedGenerator, setSelectedGenerator] = useState(null);
  const [generatorArgs, setGeneratorArgs] = useState("");
  const [generatorCount, setGeneratorCount] = useState(1);
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
        const formData = new FormData();
        formData.append("file", generatorFile);
        if (newTest.description) {
          formData.append("description", newTest.description);
        }
        response = await testsAPI.createFile(taskId, formData);
      }
      if (response) {
        setTests([...tests, response.data]);
        setNewTest({ inputText: "", description: "" });
        setGeneratorFile(null);
        setShowAddModal(false);
        setError("");
      }
    } catch (err) {
      console.error("Failed to add test:", err);
      setError("Failed to add test");
    }
  };

  const handleRunGenerator = async () => {
    if (!selectedGenerator) return;

    try {
      const params = {
        args: generatorArgs || undefined,
        count: generatorCount,
      };
      const response = await generatorsAPI.run(
        taskId,
        selectedGenerator.id,
        params,
      );
      // Append generated tests to list
      if (response.data && Array.isArray(response.data)) {
        setTests([...tests, ...response.data]);
      }
      setSelectedGenerator(null);
      setGeneratorArgs("");
      setGeneratorCount(1);
      setShowGeneratorModal(false);
      setError("");
    } catch (err) {
      console.error("Failed to run generator:", err);
      setError("Failed to run generator");
    }
  };

  const handleDeleteTest = async (testId) => {
    try {
      await testsAPI.delete(taskId, testId);
      setTests(tests.filter((t) => t.id !== testId));
      setError("");
    } catch (err) {
      console.error("Failed to delete test:", err);
      setError("Failed to delete test");
    }
  };

  const handleEditClick = (test) => {
    setEditingTestId(test.id);
    setEditInput(test.inputText || "");
    setEditDescription(test.description || "");
  };

  const handleSaveEdit = async (testId) => {
    try {
      await testsAPI.update(taskId, testId, {
        inputText: editInput,
        description: editDescription,
      });
      const res = await testsAPI.list(taskId);
      setTests(res.data || []);
      setEditingTestId(null);
      setError("");
    } catch (err) {
      console.error("Failed to update test:", err);
      setError("Failed to update test");
    }
  };

  const handleUploadGenerator = async (e) => {
    e.preventDefault();
    if (!generatorFile || !generatorLanguage) {
      setError("Generator file and language are required");
      return;
    }

    try {
      const formData = new FormData();
      formData.append("file", generatorFile);
      formData.append("language", generatorLanguage);
      await generatorsAPI.uploadSource(taskId, formData);
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
            <label>Problem Tests:</label>
            {tests.length === 0 ? (
              <p>No tests yet</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Input</th>
                    <th>Description</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {tests.map((test) => (
                    <tr key={test.id}>
                      <>
                        <td>
                          {editingTestId === test.id ? (
                            <textarea
                              value={editInput}
                              onChange={(e) => setEditInput(e.target.value)}
                              rows={3}
                            />
                          ) : (
                            test.inputText?.substring(0, 50) || "N/A"
                          )}
                        </td>
                        <td>
                          {editingTestId === test.id ? (
                            <input
                              type="text"
                              value={editDescription}
                              onChange={(e) =>
                                setEditDescription(e.target.value)
                              }
                            />
                          ) : (
                            test.description || "N/A"
                          )}
                        </td>
                        <td>
                          {editingTestId === test.id ? (
                            <>
                              <button onClick={() => handleSaveEdit(test.id)}>
                                Save
                              </button>
                              <button onClick={() => setEditingTestId(null)}>
                                Cancel
                              </button>
                            </>
                          ) : (
                            <>
                              <button onClick={() => handleEditClick(test)}>
                                Edit
                              </button>
                              <button onClick={() => handleDeleteTest(test.id)}>
                                Delete
                              </button>
                            </>
                          )}
                        </td>
                      </>
                    </tr>
                  ))}
                  <tr>
                    <td colSpan="3">
                      <div className="add-test">
                        <input
                          type="text"
                          placeholder="Input"
                          value={newTest.inputText}
                          onChange={(e) =>
                            setNewTest({
                              ...newTest,
                              inputText: e.target.value,
                            })
                          }
                        />
                        <input
                          type="text"
                          placeholder="Description (optional)"
                          value={newTest.description}
                          onChange={(e) =>
                            setNewTest({
                              ...newTest,
                              description: e.target.value,
                            })
                          }
                        />
                        <button onClick={handleAddTest}>Add Test</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            )}
          </div>
          <div className="field-group">
            <label>Generators:</label>
            {generators.length === 0 ? (
              <p>No generators yet</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Generator</th>
                    <th>Language</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {generators.map((gen) => (
                    <tr key={gen.id}>
                      <td>{gen.name || gen.id}</td>
                      <td>{gen.language || "N/A"}</td>
                      <td>
                        <button onClick={() => setSelectedGenerator(gen)}>
                          Run
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
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
                <textarea
                  placeholder="Input text"
                  value={newTest.inputText}
                  onChange={(e) =>
                    setNewTest({ ...newTest, inputText: e.target.value })
                  }
                  rows={5}
                />
                <input
                  type="text"
                  placeholder="Description (optional)"
                  value={newTest.description}
                  onChange={(e) =>
                    setNewTest({ ...newTest, description: e.target.value })
                  }
                />
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
                <input
                  type="file"
                  onChange={(e) => setGeneratorFile(e.target.files[0])}
                  required
                />
                <input
                  type="text"
                  placeholder="Description (optional)"
                  value={newTest.description}
                  onChange={(e) =>
                    setNewTest({ ...newTest, description: e.target.value })
                  }
                />
                <div className="modal-buttons">
                  <button type="submit">Add Test</button>
                  <button type="button" onClick={() => setShowAddModal(false)}>
                    Cancel
                  </button>
                </div>
              </form>
            )}

            {addMode === "generator" && (
              <form onSubmit={handleAddTest}>
                <label>
                  Generator:
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
                        {gen.name || gen.id}
                      </option>
                    ))}
                  </select>
                </label>
                <input
                  type="text"
                  placeholder="Arguments (optional)"
                  value={generatorArgs}
                  onChange={(e) => setGeneratorArgs(e.target.value)}
                />
                <input
                  type="number"
                  placeholder="Count"
                  value={generatorCount}
                  onChange={(e) => setGeneratorCount(parseInt(e.target.value))}
                  min="1"
                />
                <div className="modal-buttons">
                  <button type="submit">Create Tests</button>
                  <button type="button" onClick={() => setShowAddModal(false)}>
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
              <input
                type="file"
                onChange={(e) => setGeneratorFile(e.target.files[0])}
                required
              />
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
              <div className="modal-buttons">
                <button type="submit">Upload</button>
                <button
                  type="button"
                  onClick={() => setShowGeneratorModal(false)}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {selectedGenerator && (
        <div className="modal">
          <div className="modal-content">
            <h3>
              Run Generator: {selectedGenerator.name || selectedGenerator.id}
            </h3>
            <input
              type="text"
              placeholder="Arguments (optional)"
              value={generatorArgs}
              onChange={(e) => setGeneratorArgs(e.target.value)}
            />
            <input
              type="number"
              placeholder="Count"
              value={generatorCount}
              onChange={(e) => setGeneratorCount(parseInt(e.target.value))}
              min="1"
            />
            <div className="modal-buttons">
              <button onClick={handleRunGenerator}>Run</button>
              <button onClick={() => setSelectedGenerator(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TestsTab;
