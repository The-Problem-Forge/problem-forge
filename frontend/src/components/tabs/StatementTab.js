import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { statementAPI } from "../../services/api";
import MathJaxWrapper from "../MathJaxWrapper";

/**
 * StatementTab component for editing task statement
 * @returns {React.Component} Statement tab
 */
const StatementTab = () => {
  const { taskId } = useParams();
  const [data, setData] = useState({
    statementTex: "",
    inputFormatTex: "",
    outputFormatTex: "",
    notesTex: "",
    tutorialTex: "",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadData();
  }, [taskId]);

  const loadData = async () => {
    try {
      const response = await statementAPI.get(taskId);
      setData(response.data);
    } catch (err) {
      console.error("Failed to load statement data:", err);
      setError("Failed to load statement data");
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await statementAPI.update(taskId, data);
      setError("");
    } catch (err) {
      console.error("Failed to save statement data:", err);
      setError("Failed to save statement data");
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (field, value) => {
    setData({ ...data, [field]: value });
  };

  const handleExport = async (type) => {
    try {
      if (type === "tex") {
        const response = await statementAPI.exportTex(taskId);
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", "statement.tex");
        document.body.appendChild(link);
        link.click();
        link.remove();
      } else if (type === "pdf") {
        const response = await statementAPI.exportPdf(taskId);
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", "statement.pdf");
        document.body.appendChild(link);
        link.click();
        link.remove();
      }
    } catch (err) {
      console.error(`Failed to export ${type}:`, err);
      setError(`Failed to export ${type.toUpperCase()}`);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="statement-tab">
      <h2>Statement</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="statement-controls">
        <button onClick={() => setShowPreview(!showPreview)}>
          {showPreview ? "Hide Preview" : "Show Preview"}
        </button>
        <button onClick={() => handleExport("tex")}>Export LaTeX</button>
        <button onClick={() => handleExport("pdf")}>Export PDF</button>
        <button onClick={handleSave} disabled={saving}>
          {saving ? "Saving..." : "Save"}
        </button>
      </div>
      <div className="statement-editor">
        <div className="statement-fields">
          <div className="field-group">
            <label>Statement:</label>
            <textarea
              value={data.statementTex}
              onChange={(e) => handleChange("statementTex", e.target.value)}
              rows={6}
            />
          </div>
          <div className="field-group">
            <label>Input Format:</label>
            <textarea
              value={data.inputFormatTex}
              onChange={(e) => handleChange("inputFormatTex", e.target.value)}
              rows={4}
            />
          </div>
          <div className="field-group">
            <label>Output Format:</label>
            <textarea
              value={data.outputFormatTex}
              onChange={(e) => handleChange("outputFormatTex", e.target.value)}
              rows={4}
            />
          </div>
          <div className="field-group">
            <label>Notes:</label>
            <textarea
              value={data.notesTex}
              onChange={(e) => handleChange("notesTex", e.target.value)}
              rows={4}
            />
          </div>
          <div className="field-group">
            <label>Tutorial:</label>
            <textarea
              value={data.tutorialTex}
              onChange={(e) => handleChange("tutorialTex", e.target.value)}
              rows={4}
            />
          </div>
        </div>
        {showPreview && (
          <div className="statement-preview">
            <h3>Preview</h3>
            <MathJaxWrapper content={data.statementTex} />
            <h4>Input Format</h4>
            <MathJaxWrapper content={data.inputFormatTex} />
            <h4>Output Format</h4>
            <MathJaxWrapper content={data.outputFormatTex} />
            <h4>Notes</h4>
            <MathJaxWrapper content={data.notesTex} />
            <h4>Tutorial</h4>
            <MathJaxWrapper content={data.tutorialTex} />
          </div>
        )}
      </div>
    </div>
  );
};

export default StatementTab;
