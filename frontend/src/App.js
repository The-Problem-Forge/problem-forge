import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from "./components/Home";
import Login from "./components/Login";
import Contests from "./components/Contests";
import Problems from "./components/Problems";
import ContestDetail from "./components/ContestDetail";
import TaskEditor from "./components/TaskEditor";
import AuthGuard from "./components/AuthGuard";
import Layout from "./components/Layout";

/**
 * Main App component with routing
 * @returns {React.Component} App with routes
 */
function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const login = localStorage.getItem("login");
    if (token && login) {
      setUser(login);
    }
  }, []);

  const handleLogin = (login) => {
    setUser(login);
  };

  const handleLogout = () => {
    setUser(null);
  };

  const isAuthenticated = !!user;

  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={<Home isAuthenticated={isAuthenticated} login={user} />}
        />
        <Route path="/login" element={<Login onLogin={handleLogin} />} />
        <Route
          path="/contests"
          element={
            <AuthGuard>
              <Layout login={user} onLogout={handleLogout}>
                <Contests />
              </Layout>
            </AuthGuard>
          }
        />
        <Route
          path="/problems"
          element={
            <AuthGuard>
              <Layout login={user} onLogout={handleLogout}>
                <Problems />
              </Layout>
            </AuthGuard>
          }
        />
        <Route
          path="/contests/:contestId"
          element={
            <AuthGuard>
              <Layout login={user} onLogout={handleLogout}>
                <ContestDetail />
              </Layout>
            </AuthGuard>
          }
        />
        <Route
          path="/contests/:contestId/tasks/:taskId/*"
          element={
            <AuthGuard>
              <Layout login={user} onLogout={handleLogout}>
                <TaskEditor />
              </Layout>
            </AuthGuard>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
