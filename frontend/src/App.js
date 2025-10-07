import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import TaskManager from './components/TaskManager';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const login = localStorage.getItem('login'); // key renamed
    if (token && login) {
      setUser(login); // set login instead of username
    }
  }, []);

  const handleLogin = (login) => { // parameter renamed
    setUser(login); // set login instead of username
  };

  const handleLogout = () => {
    setUser(null);
  };

  return (
    <div className="App">
      {user ? (
        <TaskManager login={user} onLogout={handleLogout} />
      ) : (
        <Login onLogin={handleLogin} />
      )}
    </div>
  );
}

export default App;