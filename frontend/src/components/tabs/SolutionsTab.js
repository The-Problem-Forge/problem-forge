import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { solutionsAPI } from "../../services/api";

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

  if (loading) return <div>Loading...</div>;

  return (
    <div className="solutions-tab">
      <h2>Solutions</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div className="solutions-upload">
        <h3>Upload New Solution</h3>
        <input
          type="file"
          accept=".cpp,.py,.java,.c,.js"
          onChange={handleFileUpload}
          disabled={uploadingFile}
        />
        {uploadingFile && <span>Uploading...</span>}
      </div>

      <div className="solutions-list">
        <h3>Solutions</h3>
        {solutions.length === 0 ? (
          <p>No solutions yet</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Language</th>
                <th>Type</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {solutions.map((solution) => (
                <React.Fragment key={solution.id}>
                  <tr>
                    <td>{solution.name || `Solution ${solution.id}`}</td>
                    <td>{solution.language || "Unknown"}</td>
                    <td>{solution.type || "Unknown"}</td>
                    <td>
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
                    </td>
                  </tr>
                  {compileResults[solution.id] && (
                    <tr>
                      <td colSpan="4" style={{ backgroundColor: "#f9f9f9" }}>
                        <strong>Compile Result:</strong> Verdict: {compileResults[solution.id].verdict}; Stdout: {compileResults[solution.id].stdout}; Stderr: {compileResults[solution.id].stderr}
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {editingSource && (
        <div className="source-editor">
          <h3>Edit Solution Source</h3>
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
  );
};

export default SolutionsTab;
