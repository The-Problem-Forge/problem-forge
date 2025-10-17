import React from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/Layout.css";

/**
 * Layout component providing navigation and logout
 * @param {Object} props - Component props
 * @param {React.Component} props.children - Child components
 * @param {string} props.login - Current user login
 * @param {Function} props.onLogout - Logout handler
 * @returns {React.Component} Layout with nav and content
 */
const Layout = ({ children, login, onLogout }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("login");
    onLogout();
    navigate("/");
  };

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="nav-brand">
          <Link to="/">Problem Forge</Link>
        </div>
        <div className="nav-links">
          <Link to="/contests">My Contests</Link>
          <span className="nav-user">Welcome, {login}!</span>
          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </nav>
      <main className="main-content">{children}</main>
    </div>
  );
};

export default Layout;
