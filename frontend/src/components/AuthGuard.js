import React, { useState, useEffect } from "react";
import { Navigate } from "react-router-dom";
import { authAPI } from "../services/api";

/**
 * AuthGuard component that protects routes requiring authentication
 * @param {Object} props - Component props
 * @param {React.Component} props.children - Child components to render if authenticated
 * @returns {React.Component} Either children or redirect to home
 */
const AuthGuard = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null); // null = loading

  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem("token");
      if (!token) {
        setIsAuthenticated(false);
        return;
      }

      try {
        await authAPI.me();
        setIsAuthenticated(true);
      } catch (error) {
        if (error.response?.status === 401) {
          // Clear invalid token
          localStorage.removeItem("token");
          localStorage.removeItem("login");
          setIsAuthenticated(false);
        } else {
          // If /me is not implemented (404/501), fall back to token presence
          setIsAuthenticated(true);
        }
      }
    };

    checkAuth();
  }, []);

  if (isAuthenticated === null) {
    return <div>Loading...</div>; // Or a proper loading component
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default AuthGuard;
