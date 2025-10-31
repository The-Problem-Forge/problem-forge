import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { contestsAPI, problemsAPI } from "../services/api";
import "../styles/main.scss";

/**
 * ContestDetail component for showing contest info and tasks
 * @returns {React.Component} Contest detail
 */
const ContestDetail = () => {
  const { contestId } = useParams();
  const navigate = useNavigate();
  const [contest, setContest] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newTask, setNewTask] = useState({ title: "", description: "" });
  const [error, setError] = useState("");

  useEffect(() => {
    loadData();
  }, [contestId]);

  const loadData = async () => {
    try {
      const [contestRes, tasksRes] = await Promise.all([
        contestsAPI.get(contestId),
        contestsAPI.getProblems(contestId),
      ]);
      setContest(contestRes.data);
      setTasks(tasksRes.data);
    } catch (err) {
      console.error("Failed to load contest data:", err);
      setError("Failed to load contest data");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!newTask.title.trim()) return;

    try {
      const response = await problemsAPI.create(newTask, contestId);
      setNewTask({ title: "", description: "" });
      setShowModal(false);
      await loadData();
    } catch (err) {
      console.error("Failed to create task:", err);
      setError("Failed to create task");
    }
  };

  const handleReorder = async (taskId, direction) => {
    const currentIndex = tasks.findIndex((t) => t.id === taskId);
    if (direction === "up" && currentIndex > 0) {
      const newTasks = [...tasks];
      [newTasks[currentIndex - 1], newTasks[currentIndex]] = [
        newTasks[currentIndex],
        newTasks[currentIndex - 1],
      ];
      setTasks(newTasks);
      await updateOrder(newTasks);
    } else if (direction === "down" && currentIndex < tasks.length - 1) {
      const newTasks = [...tasks];
      [newTasks[currentIndex], newTasks[currentIndex + 1]] = [
        newTasks[currentIndex + 1],
        newTasks[currentIndex],
      ];
      setTasks(newTasks);
      await updateOrder(newTasks);
    }
  };

  const updateOrder = async (orderedTasks) => {
    try {
      const order = orderedTasks.map((t) => t.id);
      await contestsAPI.reorderProblems(contestId, order);
    } catch (err) {
      console.error("Failed to reorder tasks:", err);
      setError("Failed to reorder tasks");
      // Rollback
      loadData();
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  if (!contest) return <div>Contest not found</div>;

  return (
    <div>
      <div className="contest-header">
        <Link to="/contests" className="back-button">
          ← Back to Contests
        </Link>
        <h1>{contest.name}</h1>
        {contest.description && <p>{contest.description}</p>}
      </div>

      <div className="tasks-section">
        <h2>Problems</h2>
        <button className="add-task-btn" onClick={() => setShowModal(true)}>
          New Problem
        </button>
        <ul className="tasks-list">
          {tasks.map((task, index) => (
            <li key={task.id} className="task-item">
              <div
                className="task-info"
                onClick={() =>
                  navigate(`/contests/${contestId}/tasks/${task.id}`)
                }
                style={{ cursor: "pointer" }}
              >
                <h3>{task.title}</h3>
                {task.description && <p>{task.description}</p>}
              </div>
              <div className="task-actions">
                <button
                  className="reorder-btn"
                  onClick={() => handleReorder(task.id, "up")}
                  disabled={index === 0}
                >
                  ↑
                </button>
                <button
                  className="reorder-btn"
                  onClick={() => handleReorder(task.id, "down")}
                  disabled={index === tasks.length - 1}
                >
                  ↓
                </button>
              </div>
            </li>
          ))}
        </ul>
      </div>

      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <h3>Add New Task</h3>
            <form onSubmit={handleCreateTask}>
              <div>
                <input
                  type="text"
                  placeholder="Task Title"
                  value={newTask.title}
                  onChange={(e) =>
                    setNewTask({ ...newTask, title: e.target.value })
                  }
                  className="modal-input"
                  required
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ContestDetail;
