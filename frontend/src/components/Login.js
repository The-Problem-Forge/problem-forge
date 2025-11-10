import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authAPI } from "../services/api";
import "../styles/main.scss";

/**
 * Login/Register component
 * @param {Object} props - Component props
 * @param {Function} props.onLogin - Callback when login succeeds
 * @returns {React.Component} Login form
 */
const Login = ({ onLogin }) => {
  const navigate = useNavigate();
  const [login, setLogin] = useState(""); // state renamed
  const [password, setPassword] = useState("");
  const [isRegister, setIsRegister] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = isRegister
        ? await authAPI.register(login, password) // parameter renamed
        : await authAPI.login(login, password); // parameter renamed

      localStorage.setItem("token", response.data.token);
      localStorage.setItem("login", response.data.login);
      onLogin(response.data.login);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Authentication failed");
    }
  };

  return (
    <div className="login-container">
      <h2>{isRegister ? "Register" : "Login"}</h2>
      <form onSubmit={handleSubmit} className="login-form">
        <div>
          <input
            type="text"
            placeholder="Login"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            required
            className="login-input"
          />
        </div>
        <div>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="login-input"
          />
        </div>
        {error && <div className="login-error">{error}</div>}
        <button type="submit" className="login-button">
          {isRegister ? "Register" : "Login"}
        </button>
      </form>
      <button
        onClick={() => setIsRegister(!isRegister)}
        className="toggle-button"
      >
        {isRegister
          ? "Already have an account? Login"
          : "Need an account? Register"}
      </button>
    </div>
  );
};

export default Login;
