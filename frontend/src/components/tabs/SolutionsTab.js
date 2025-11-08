import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { solutionsAPI } from "../../services/api";
import Table from "../Table";
import SolutionModal from "../SolutionModal";
import SourceEditorModal from "../SourceEditorModal";
import "../SourceEditorModal.css";

/**
 * SolutionsTab component for solutions management
 * @returns {React.Component} Solutions tab
 */
const SolutionsTab = () => {
  const { taskId } = useParams();
  const [solutions, setSolutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [compiling, setCompiling] = useState({});
  const [compileResults, setCompileResults] = useState({});
  const [editingSource, setEditingSource] = useState(null);
  const [sourceContent, setSourceContent] = useState("");
  const [savingSource, setSavingSource] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSolution, setEditingSolution] = useState(null);
  const [isSourceModalOpen, setIsSourceModalOpen] = useState(false);

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
   * Handles opening the add solution modal
   */
  const handleAddSolution = () => {
    setEditingSolution(null);
    setIsModalOpen(true);
  };

  /**
   * Handles saving a solution from the modal
   * @param {Object} formData - Form data from modal
   */
  const handleSaveSolution = async (formData) => {
    try {
      if (editingSolution) {
        // Update existing solution
        await solutionsAPI.update(taskId, editingSolution.id, {
          name: formData.name,
          language: formData.language,
          solutionType: formData.solutionType,
        });
      } else {
        // Create new solution
        const uploadFormData = new FormData();
        uploadFormData.append("file", formData.file);
        uploadFormData.append("name", formData.name);
        uploadFormData.append("language", formData.language);
        uploadFormData.append("solutionType", formData.solutionType);
        await solutionsAPI.uploadSource(taskId, uploadFormData);
      }
      await loadSolutions();
      setIsModalOpen(false);
      setError("");
    } catch (err) {
      console.error("Failed to save solution:", err);
      setError("Failed to save solution");
    }
  };

  /**
   * Handles closing the modal
   */
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingSolution(null);
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
      setIsSourceModalOpen(true);
      setError("");
    } catch (err) {
      console.error("Failed to load solution source:", err);
      setError("Failed to load solution source");
    }
  };

  /**
   * Handles saving solution source
   * @param {string} content - Source code content to save
   * @returns {Promise<void>}
   */
  const handleSaveSource = async (content) => {
    if (!editingSource) return;

    setSavingSource(true);
    try {
      // Find the solution to get its current metadata
      const solution = solutions.find((s) => s.id === editingSource);
      if (!solution) {
        throw new Error("Solution not found");
      }

      await solutionsAPI.update(taskId, editingSource, {
        name: solution.name,
        language: solution.language,
        solutionType: solution.solutionType,
        source: content,
      });
      setSourceContent(content);
      setIsSourceModalOpen(false);
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
   * Handles closing the source editor modal
   */
  const handleCloseSourceModal = () => {
    setIsSourceModalOpen(false);
    setEditingSource(null);
    setSourceContent("");
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
        solutionType: updatedSolution.solutionType,
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
      <div className="solutions-controls">
        <button onClick={handleAddSolution}>Add Solution</button>
      </div>
      <div className="solutions-editor">
        <div className="solutions-fields">
          <div className="field-group">
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
                  key: "solutionType",
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
                    Edit
                  </button>
                  <button onClick={() => handleDownload(solution.id)}>
                    Download
                  </button>
                </>
              )}
              emptyMessage="No solutions yet"
            />
          </div>
        </div>
      </div>
      <SolutionModal
        isOpen={isModalOpen}
        solution={editingSolution}
        onSave={handleSaveSolution}
        onClose={handleCloseModal}
      />
      <SourceEditorModal
        isOpen={isSourceModalOpen}
        solutionId={editingSource}
        initialContent={sourceContent}
        onSave={handleSaveSource}
        onClose={handleCloseSourceModal}
        saving={savingSource}
      />
    </div>
  );
};

export default SolutionsTab;
