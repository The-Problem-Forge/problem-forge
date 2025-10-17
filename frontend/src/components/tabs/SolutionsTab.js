import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { solutionsAPI } from "../../services/api";
import Table from "../Table";

/**
 * SolutionsTab component for solutions management
 * @returns {React.Component} Solutions tab
 */
const SolutionsTab = () => {
  const { taskId } = useParams();
  const [solutions, setSolutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [uploadingFile, setUploadingFile] = useState(false);
  const [compiling, setCompiling] = useState({});
  const [compileResults, setCompileResults] = useState({});
  const [editingSource, setEditingSource] = useState(null);
  const [sourceContent, setSourceContent] = useState("");
  const [savingSource, setSavingSource] = useState(false);

  useEffect(() => {
    loadSolutions();
  }, [taskId]);

  /**
   * Loads solutions for the task
   * @returns {Promise<void>}
   */
  const loadSolutions = async () => {
    try {
      const response = await solutionsAPI.list(taskId);
      setSolutions(response.data || []);
      setError("");
    } catch (err) {
      console.error("Failed to load solutions:", err);
      setError("Failed to load solutions");
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handles file upload for new solution
   * @param {Event} e - File input change event
   * @returns {Promise<void>}
   */
  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploadingFile(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      await solutionsAPI.uploadSource(taskId, formData);
      await loadSolutions();
      setError("");
    } catch (err) {
      console.error("Failed to upload solution:", err);
      setError("Failed to upload solution");
    } finally {
      setUploadingFile(false);
    }
  };

  /**
   * Handles compile check for a solution
   * @param {string} solutionId - Solution ID
   * @returns {Promise<void>}
   */
  const handleCompile = async (solutionId) => {
    setCompiling((prev) => ({ ...prev, [solutionId]: true }));
    try {
      const response = await solutionsAPI.compile(taskId, solutionId);
      setError("");
      setCompileResults((prev) => ({
        ...prev,
        [solutionId]: response.data,
      }));
      await loadSolutions();
    } catch (err) {
      console.error("Failed to compile solution:", err);
      setError("Failed to compile solution");
    } finally {
      setCompiling((prev) => ({ ...prev, [solutionId]: false }));
    }
  };

  /**
   * Handles viewing solution source
   * @param {string} solutionId - Solution ID
   * @returns {Promise<void>}
   */
  const handleViewSource = async (solutionId) => {
    try {
      const response = await solutionsAPI.getSource(taskId, solutionId);
      setSourceContent(response.data?.source || "");
      setEditingSource(solutionId);
      setError("");
    } catch (err) {
      console.error("Failed to load solution source:", err);
      setError("Failed to load solution source");
    }
  };

  /**
   * Handles saving solution source
   * @returns {Promise<void>}
   */
  const handleSaveSource = async () => {
    if (!editingSource) return;

    setSavingSource(true);
    try {
      await solutionsAPI.update(taskId, editingSource, {
        source: sourceContent,
      });
      setEditingSource(null);
      setSourceContent("");
      setError("");
      await loadSolutions();
    } catch (err) {
      console.error("Failed to save solution source:", err);
      setError("Failed to save solution source");
    } finally {
      setSavingSource(false);
    }
  };

  /**
   * Handles downloading solution
   * @param {string} solutionId - Solution ID
   * @returns {Promise<void>}
   */
  const handleDownload = async (solutionId) => {
    try {
      const response = await solutionsAPI.download(taskId, solutionId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `solution-${solutionId}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      setError("");
    } catch (err) {
      console.error("Failed to download solution:", err);
      setError("Failed to download solution");
    }
  };

  /**
   * Handles updating solution metadata
   * @param {Object} updatedSolution - Updated solution object
   * @returns {Promise<void>}
   */
  const handleUpdateSolution = async (updatedSolution) => {
    try {
      await solutionsAPI.update(taskId, updatedSolution.id, {
        name: updatedSolution.name,
        language: updatedSolution.language,
        type: updatedSolution.type,
      });
      setSolutions((prevSolutions) =>
        prevSolutions.map((solution) =>
          solution.id === updatedSolution.id ? updatedSolution : solution,
        ),
      );
      setError("");
    } catch (err) {
      console.error("Failed to update solution:", err);
      setError("Failed to update solution");
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="solutions-tab">
      <h2>Solutions</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="solutions-controls">
        <input
          type="file"
          accept=".cpp,.py,.java,.c,.js"
          onChange={handleFileUpload}
          disabled={uploadingFile}
        />
        {uploadingFile && <span>Uploading...</span>}
      </div>
      <div className="solutions-editor">
        <div className="solutions-fields">
          <div className="field-group">
            <label>Solutions:</label>
            <Table
              headers={[
                {
                  key: "name",
                  label: "Name",
                  editable: true,
                  type: "text",
                },
                {
                  key: "language",
                  label: "Language",
                  editable: true,
                  type: "select",
                  options: [
                    { value: "cpp", label: "C++" },
                    { value: "python", label: "Python" },
                    { value: "java", label: "Java" },
                    { value: "c", label: "C" },
                    { value: "js", label: "JavaScript" },
                  ],
                },
                {
                  key: "type",
                  label: "Type",
                  editable: true,
                  type: "select",
                  options: [
                    {
                      value: "Main correct solution",
                      label: "Main correct solution",
                    },
                    { value: "Correct", label: "Correct" },
                    { value: "Incorrect", label: "Incorrect" },
                    {
                      value: "Time limit exceeded",
                      label: "Time limit exceeded",
                    },
                    {
                      value: "Time limit exceeded or correct",
                      label: "Time limit exceeded or correct",
                    },
                    {
                      value: "Time limit exceeded or memory limit exceeded",
                      label: "Time limit exceeded or memory limit exceeded",
                    },
                    { value: "Wrong answer", label: "Wrong answer" },
                    {
                      value: "Presentation error",
                      label: "Presentation error",
                    },
                    {
                      value: "Memory limit exceeded",
                      label: "Memory limit exceeded",
                    },
                    { value: "Do not run", label: "Do not run" },
                    { value: "Failed", label: "Failed" },
                  ],
                },
                { key: "verdict", label: "Compile Verdict" },
                { key: "actions", label: "Actions" },
              ]}
              rows={solutions.map((solution) => ({
                ...solution,
                verdict: compileResults[solution.id]?.verdict || "Not compiled",
              }))}
              onSave={(solutionId, key, value) => {
                const updatedSolution = solutions.find(
                  (solution) => solution.id === solutionId,
                );
                if (updatedSolution) {
                  handleUpdateSolution({ ...updatedSolution, [key]: value });
                }
              }}
              renderActions={(solution) => (
                <>
                  <button
                    onClick={() => handleCompile(solution.id)}
                    disabled={compiling[solution.id]}
                  >
                    {compiling[solution.id] ? "Compiling..." : "Compile"}
                  </button>
                  <button onClick={() => handleViewSource(solution.id)}>
                    View/Edit
                  </button>
                  <button onClick={() => handleDownload(solution.id)}>
                    Download
                  </button>
                </>
              )}
              emptyMessage="No solutions yet"
            />
          </div>
          {editingSource && (
            <div className="field-group">
              <label>Edit Solution Source:</label>
              <textarea
                value={sourceContent}
                onChange={(e) => setSourceContent(e.target.value)}
                rows={15}
                placeholder="Enter solution source code..."
              />
              <div className="editor-buttons">
                <button onClick={handleSaveSource} disabled={savingSource}>
                  {savingSource ? "Saving..." : "Save"}
                </button>
                <button onClick={() => setEditingSource(null)}>Cancel</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SolutionsTab;
