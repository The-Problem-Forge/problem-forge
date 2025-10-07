import React, { useState, useEffect } from 'react';
import { taskAPI } from '../services/api';

const TaskManager = ({ login, onLogout }) => { // prop renamed
  const [tasks, setTasks] = useState([]);
  const [newTask, setNewTask] = useState({ title: '', description: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadTasks();
  }, []);

  const loadTasks = async () => {
    try {
      const response = await taskAPI.getTasks();
      setTasks(response.data);
    } catch (err) {
      console.error('Failed to load tasks:', err);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!newTask.title.trim()) return;

    setLoading(true);
    try {
      const response = await taskAPI.createTask(newTask);
      setTasks([...tasks, response.data]);
      setNewTask({ title: '', description: '' });
    } catch (err) {
      console.error('Failed to create task:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('login'); // key renamed
    onLogout();
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Welcome, {login}!</h2> {/* variable renamed */}
        <button onClick={handleLogout}>Logout</button>
      </div>

      {/* Rest of the component remains the same */}
      <form onSubmit={handleCreateTask} style={{ marginBottom: '20px' }}>
        <h3>Create New Task</h3>
        <div style={{ marginBottom: '10px' }}>
          <input
            type="text"
            placeholder="Task title"
            value={newTask.title}
            onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
            style={{ width: '100%', padding: '8px' }}
            required
          />
        </div>
        <div style={{ marginBottom: '10px' }}>
          <textarea
            placeholder="Task description"
            value={newTask.description}
            onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
            style={{ width: '100%', padding: '8px', minHeight: '60px' }}
          />
        </div>
        <button type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create Task'}
        </button>
      </form>

      <div>
        <h3>Your Tasks ({tasks.length})</h3>
        {tasks.length === 0 ? (
          <p>No tasks yet. Create your first task!</p>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {tasks.map(task => (
              <li key={task.id} style={{ 
                border: '1px solid #ddd', 
                padding: '10px', 
                marginBottom: '10px',
                borderRadius: '4px'
              }}>
                <strong>{task.title}</strong>
                {task.description && <p>{task.description}</p>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default TaskManager;