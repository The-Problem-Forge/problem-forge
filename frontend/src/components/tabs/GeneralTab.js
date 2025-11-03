import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { generalAPI, problemsAPI, contestsAPI } from "../../services/api";

/**
 * GeneralTab component for editing general task settings
 * @returns {React.Component} General tab
 */
const GeneralTab = () => {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState({
    title: "",
    inputFile: "",
    outputFile: "",
    timeLimit: 1000,
    memoryLimit: 256,
  });
  const [originalData, setOriginalData] = useState({});
  const [contests, setContests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [showAddModal, setShowAddModal] = useState(false);
  const [contestSearch, setContestSearch] = useState("");
  const [allContests, setAllContests] = useState([]);
  const [filteredContests, setFilteredContests] = useState([]);

  useEffect(() => {
    loadData();
  }, [taskId]);

  const loadData = async () => {
    try {
      const [generalResponse, contestsResponse, allContestsResponse] =
        await Promise.all([
          generalAPI.get(taskId),
          problemsAPI.getContests(taskId),
          contestsAPI.list(),
        ]);
      setData(generalResponse.data);
      setOriginalData(generalResponse.data);
      setContests(contestsResponse.data || []);
      setAllContests(allContestsResponse.data || []);
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
      setOriginalData(data);
      setError("");
    } catch (err) {
      console.error("Failed to save general data:", err);
      setError("Failed to save general data");
    } finally {
      setSaving(false);
    }
  };

  const hasChanges = () => {
    return JSON.stringify(data) !== JSON.stringify(originalData);
  };

  const handleChange = (field, value) => {
    setData({ ...data, [field]: value });
  };

  const handleRemoveFromContest = async (contestId) => {
    try {
      await contestsAPI.removeProblemFromContest(contestId, taskId);
      setContests(contests.filter((c) => c.id !== contestId));
    } catch (err) {
      console.error("Failed to remove problem from contest:", err);
      setError("Failed to remove problem from contest");
    }
  };

  const handleAddToContest = async (contestId) => {
    try {
      await contestsAPI.addProblemToContest(contestId, taskId);
      const contest = allContests.find((c) => c.id === contestId);
      if (contest) {
        setContests([...contests, contest]);
      }
      setShowAddModal(false);
      setContestSearch("");
    } catch (err) {
      console.error("Failed to add problem to contest:", err);
      setError("Failed to add problem to contest");
    }
  };

  const handleContestSearchChange = (value) => {
    setContestSearch(value);
    const filtered = allContests.filter(
      (contest) =>
        contest.name.toLowerCase().includes(value.toLowerCase()) &&
        !contests.some((c) => c.id === contest.id),
    );
    setFilteredContests(filtered);
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="general-tab">
      <h2>General Settings</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="form-group">
        <label>Title:</label>
        <input
          type="text"
          value={data.title}
          onChange={(e) => handleChange("title", e.target.value)}
        />
      </div>
      <div className="form-group">
        <label>Input File:</label>
        <input
          type="text"
          value={data.inputFile}
          onChange={(e) => handleChange("inputFile", e.target.value)}
        />
      </div>
      <div className="form-group">
        <label>Output File:</label>
        <input
          type="text"
          value={data.outputFile}
          onChange={(e) => handleChange("outputFile", e.target.value)}
        />
      </div>
      <div className="form-group">
        <label>Time Limit (ms):</label>
        <input
          type="number"
          value={data.timeLimit}
          onChange={(e) => handleChange("timeLimit", parseInt(e.target.value))}
        />
      </div>
      <div className="form-group">
        <label>Memory Limit (MB):</label>
        <input
          type="number"
          value={data.memoryLimit}
          onChange={(e) =>
            handleChange("memoryLimit", parseInt(e.target.value))
          }
        />
      </div>

      <div className="contests-section">
        <h4>Contests</h4>
        <div className="contests-grid">
          {contests.map((contest) => (
            <div
              key={contest.id}
              className="contest-item"
              onClick={() => navigate(`/contests/${contest.id}`)}
            >
              <div className="contest-name">{contest.name}</div>
              <button
                className="remove-btn"
                onClick={(e) => {
                  e.stopPropagation();
                  handleRemoveFromContest(contest.id);
                }}
                title="Remove from contest"
              >
                ×
              </button>
            </div>
          ))}
          <div
            className="contest-item add-contest"
            onClick={() => setShowAddModal(true)}
          >
            <div className="add-icon">+</div>
          </div>
        </div>
      </div>

      <button
        onClick={handleSave}
        disabled={saving || !hasChanges()}
        className={`save-btn ${hasChanges() ? "has-changes" : "no-changes"}`}
      >
        {saving ? "Saving..." : "Save"}
      </button>

      {showAddModal && (
        <div className="modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Add to Contest</h3>
            <input
              type="text"
              placeholder="Search contests..."
              value={contestSearch}
              onChange={(e) => handleContestSearchChange(e.target.value)}
              className="contest-search-input"
            />
            <div className="contest-suggestions">
              {filteredContests.slice(0, 5).map((contest) => (
                <div
                  key={contest.id}
                  className="suggestion-item"
                  onClick={() => handleAddToContest(contest.id)}
                >
                  {contest.name}
                </div>
              ))}
              {filteredContests.length === 0 && contestSearch && (
                <div className="no-suggestions">No contests found</div>
              )}
            </div>
            <button
              onClick={() => setShowAddModal(false)}
              className="cancel-btn"
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default GeneralTab;
