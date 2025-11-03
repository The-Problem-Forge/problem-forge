import React, { useState, useEffect } from "react";
import { Routes, Route, useParams, NavLink, Link } from "react-router-dom";
import { contestsAPI } from "../services/api";
import GeneralTab from "./tabs/GeneralTab";
import StatementTab from "./tabs/StatementTab";
import CheckerTab from "./tabs/CheckerTab";
import ValidatorTab from "./tabs/ValidatorTab";
import TestsTab from "./tabs/TestsTab";
import SolutionsTab from "./tabs/SolutionsTab";
import InvocationsTab from "./tabs/InvocationsTab";
import "../styles/main.scss";

/**
 * TaskEditor component for editing tasks with tabs
 * @returns {React.Component} Task editor
 */
const TaskEditor = () => {
  const { contestId, taskId } = useParams();

  const tabs = [
    { path: "", label: "General", index: true },
    { path: "statement", label: "Statement" },
    { path: "checker", label: "Checker" },
    { path: "validator", label: "Validator" },
    { path: "tests", label: "Tests" },
    { path: "solutions", label: "Solutions" },
    { path: "invocations", label: "Invocations" },
  ];

  return (
    <div className="task-editor">
      <div className="task-editor-header">
        <h1>Task Editor</h1>
      </div>
      <nav className="tab-nav">
        {tabs.map((tab) => (
          <NavLink
            key={tab.path}
            to={`/contests/${contestId}/tasks/${taskId}${tab.path ? `/${tab.path}` : ""}`}
            className={({ isActive }) => `tab-link ${isActive ? "active" : ""}`}
            end={tab.index}
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>
      <div className="tab-content">
        <Routes>
          <Route index element={<GeneralTab />} />
          <Route path="statement" element={<StatementTab />} />
          <Route path="checker" element={<CheckerTab />} />
          <Route path="validator" element={<ValidatorTab />} />
          <Route path="tests" element={<TestsTab />} />
          <Route path="solutions" element={<SolutionsTab />} />
          <Route path="invocations" element={<InvocationsTab />} />
        </Routes>
      </div>
    </div>
  );
};

export default TaskEditor;
