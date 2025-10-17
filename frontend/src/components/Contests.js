import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { contestsAPI } from "../services/api";
import "../styles/Home.css";

/**
 * Contests component for listing user's contests
 * @returns {React.Component} Contests list
 */
const Contests = () => {
  const [contests, setContests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [newContest, setNewContest] = useState({ name: "", description: "" });
  const [error, setError] = useState("");

  useEffect(() => {
    loadContests();
  }, []);

  const loadContests = async () => {
    try {
      const response = await contestsAPI.list();
      setContests(response.data);
    } catch (err) {
      console.error("Failed to load contests:", err);
      setError("Failed to load contests");
    }
  };

  const handleCreateContest = async (e) => {
    e.preventDefault();
    if (!newContest.name.trim()) return;

    setLoading(true);
    try {
      const response = await contestsAPI.create(newContest);
      setContests([...contests, response.data]);
      setNewContest({ name: "", description: "" });
      setShowModal(false);
    } catch (err) {
      console.error("Failed to create contest:", err);
      setError("Failed to create contest");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="home-contests">
      <div className="contest-header">
        <h1>My Contests</h1>
      </div>
      <button className="new-contest-btn" onClick={() => setShowModal(true)}>
        New Contest
      </button>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <div className="contests-list">
        {contests.map((contest) => (
          <Link
            key={contest.id}
            to={`/contests/${contest.id}`}
            className="contest-card"
          >
            <h3>{contest.name}</h3>
            {contest.description && <p>{contest.description}</p>}
          </Link>
        ))}
      </div>
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <h3>Create New Contest</h3>
            <form onSubmit={handleCreateContest}>
              <div>
                <input
                  type="text"
                  placeholder="Contest Name"
                  value={newContest.name}
                  onChange={(e) =>
                    setNewContest({ ...newContest, name: e.target.value })
                  }
                  className="modal-input"
                  required
                />
              </div>
              <div>
                <textarea
                  placeholder="Description (optional)"
                  value={newContest.description}
                  onChange={(e) =>
                    setNewContest({
                      ...newContest,
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

export default Contests;
