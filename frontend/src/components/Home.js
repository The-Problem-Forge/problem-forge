import React, { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/Home.css";

/**
 * Home component showing landing or redirecting to contests based on auth
 * @param {Object} props - Component props
 * @param {boolean} isAuthenticated - Whether user is authenticated
 * @param {string} login - User login if authenticated
 * @returns {React.Component} Home content
 */
const Home = ({ isAuthenticated, login }) => {
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/contests");
    }
  }, [isAuthenticated, navigate]);

  return (
    <div className="home-landing">
      <h1>Welcome to Problem Forge</h1>
      <p>Create and manage programming contest problems.</p>
      <div>
        <Link to="/login">
          <button className="new-contest-btn">Login / Register</button>
        </Link>
      </div>
    </div>
  );
};

export default Home;
