import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { generalAPI } from "../../services/api";

/**
 * GeneralTab component for editing general task settings
 * @returns {React.Component} General tab
 */
const GeneralTab = () => {
  const { taskId } = useParams();
  const [data, setData] = useState({
    name: "",
    inFile: "",
    outFile: "",
    timeLimitMs: 1000,
    memoryLimitMb: 256,
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadData();
  }, [taskId]);

  const loadData = async () => {
    try {
      const response = await generalAPI.get(taskId);
      setData(response.data);
    } catch (err) {
      console.error("Failed to load general data:", err);
      setError("Failed to load general data");
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await generalAPI.update(taskId, data);
      setError("");
    } catch (err) {
      console.error("Failed to save general data:", err);
      setError("Failed to save general data");
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (field, value) => {
    setData({ ...data, [field]: value });
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="general-tab">
      <h2>General Settings</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="form-group">
        <label>Name:</label>
        <input
          type="text"
          value={data.name}
          onChange={(e) => handleChange("name", e.target.value)}
        />
      </div>
      <div className="form-group">
        <label>Input File:</label>
        <input
          type="text"
          value={data.inFile}
          onChange={(e) => handleChange("inFile", e.target.value)}
        />
      </div>
      <div className="form-group">
        <label>Output File:</label>
        <input
          type="text"
          value={data.outFile}
          onChange={(e) => handleChange("outFile", e.target.value)}
        />
      </div>
      <div className="form-group">
        <label>Time Limit (ms):</label>
        <input
          type="number"
          value={data.timeLimitMs}
          onChange={(e) =>
            handleChange("timeLimitMs", parseInt(e.target.value))
          }
        />
      </div>
      <div className="form-group">
        <label>Memory Limit (MB):</label>
        <input
          type="number"
          value={data.memoryLimitMb}
          onChange={(e) =>
            handleChange("memoryLimitMb", parseInt(e.target.value))
          }
        />
      </div>
      <button onClick={handleSave} disabled={saving}>
        {saving ? "Saving..." : "Save"}
      </button>
    </div>
  );
};

export default GeneralTab;
