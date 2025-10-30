import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { problemsAPI } from "../services/api";
import "../styles/main.scss";

/**
 * Problems component for listing user's problems
 * @returns {React.Component} Problems list
 */
const Problems = () => {
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [newProblem, setNewProblem] = useState({ title: "", description: "" });
  const [error, setError] = useState("");

  useEffect(() => {
    loadProblems();
  }, []);

  const loadProblems = async () => {
    try {
      const response = await problemsAPI.list();
      setProblems(response.data);
    } catch (err) {
      console.error("Failed to load problems:", err);
      setError("Failed to load problems");
    }
  };

  const handleCreateProblem = async (e) => {
    e.preventDefault();
    if (!newProblem.title.trim()) return;

    setLoading(true);
    try {
      await problemsAPI.create(newProblem);
      setNewProblem({ title: "", description: "" });
      await loadProblems(); // Reload the list to ensure consistency
    } catch (err) {
      console.error("Failed to create problem:", err);
      setError("Failed to create problem");
    } finally {
      setLoading(false);
      setShowModal(false);
    }
  };

  return (
    <div className="home-contests">
      <div className="contest-header">
        <h1>My Problems</h1>
      </div>
      <button className="new-contest-btn" onClick={() => setShowModal(true)}>
        New Problem
      </button>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="contests-list">
        {problems.map((problem) => (
          <Link
            key={problem.id}
            to={`/contests/${problem.contestId}/tasks/${problem.id}`}
            className="contest-card"
          >
            <h3>{problem.title}</h3>
            {problem.description && <p>{problem.description}</p>}
          </Link>
        ))}
      </div>
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <h3>Create New Problem</h3>
            <form onSubmit={handleCreateProblem}>
              <div>
                <input
                  type="text"
                  placeholder="Problem Title"
                  value={newProblem.title}
                  onChange={(e) =>
                    setNewProblem({ ...newProblem, title: e.target.value })
                  }
                  className="modal-input"
                  required
                />
              </div>
              <div>
                <textarea
                  placeholder="Description (optional)"
                  value={newProblem.description}
                  onChange={(e) =>
                    setNewProblem({
                      ...newProblem,
                      description: e.target.value,
                    })
                  }
                  className="modal-input"
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit" disabled={loading}>
                  {loading ? "Creating..." : "Create"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Problems;
