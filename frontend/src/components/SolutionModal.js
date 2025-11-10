import React, { useState, useEffect } from "react";

/**
 * Modal component for adding/editing solutions
 * @param {Object} props - Component props
 * @param {boolean} props.isOpen - Whether the modal is open
 * @param {Object} props.solution - Solution object for editing (null for adding)
 * @param {Function} props.onSave - Callback when saving the solution
 * @param {Function} props.onClose - Callback when closing the modal
 * @returns {React.Component} SolutionModal component
 */
const SolutionModal = ({ isOpen, solution, onSave, onClose }) => {
  const [formData, setFormData] = useState({
    name: "",
    language: "cpp",
    solutionType: "Main correct solution",
    file: null,
    source: "",
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (solution) {
      setFormData({
        name: solution.name || "",
        language: solution.language || "cpp",
        solutionType: solution.solutionType || "Main correct solution",
        file: null,
        source: "",
      });
    } else {
      setFormData({
        name: "",
        language: "cpp",
        solutionType: "Main correct solution",
        file: null,
        source: "",
      });
    }
    setErrors({});
  }, [solution, isOpen]);

  /**
   * Handles input changes
   * @param {Event} e - Input change event
   */
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  /**
   * Handles file input changes
   * @param {Event} e - File input change event
   */
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFormData((prev) => ({ ...prev, file }));
    if (errors.file) {
      setErrors((prev) => ({ ...prev, file: "" }));
    }
  };

  /**
   * Validates the form
   * @returns {boolean} Whether the form is valid
   */
  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "Name is required";
    }

    if (!solution && !formData.file) {
      newErrors.file = "File is required for new solutions";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  /**
   * Handles form submission
   * @param {Event} e - Form submit event
   */
  const handleSubmit = (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    onSave(formData);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h3>{solution ? "Edit Solution" : "Add Solution"}</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              placeholder="Enter solution name"
            />
            {errors.name && <span className="error">{errors.name}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="language">Language</label>
            <select
              id="language"
              name="language"
              value={formData.language}
              onChange={handleInputChange}
            >
              <option value="cpp">C++</option>
              <option value="python">Python</option>
              <option value="java">Java</option>
              <option value="c">C</option>
              <option value="js">JavaScript</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="solutionType">Type</label>
            <select
              id="solutionType"
              name="solutionType"
              value={formData.solutionType}
              onChange={handleInputChange}
            >
              <option value="Main correct solution">
                Main correct solution
              </option>
              <option value="Correct">Correct</option>
              <option value="Incorrect">Incorrect</option>
              <option value="Time limit exceeded">Time limit exceeded</option>
              <option value="Time limit exceeded or correct">
                Time limit exceeded or correct
              </option>
              <option value="Time limit exceeded or memory limit exceeded">
                Time limit exceeded or memory limit exceeded
              </option>
              <option value="Wrong answer">Wrong answer</option>
              <option value="Presentation error">Presentation error</option>
              <option value="Memory limit exceeded">
                Memory limit exceeded
              </option>
              <option value="Do not run">Do not run</option>
              <option value="Failed">Failed</option>
            </select>
          </div>

          {!solution && (
            <div className="form-group">
              <label htmlFor="file">Solution File</label>
              <input
                type="file"
                id="file"
                accept=".cpp,.py,.java,.c,.js"
                onChange={handleFileChange}
              />
              {errors.file && <span className="error">{errors.file}</span>}
            </div>
          )}

          <div className="modal-buttons">
            <button type="button" className="cancel-btn" onClick={onClose}>
              Cancel
            </button>
            <button type="submit">
              {solution ? "Update" : "Add"} Solution
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SolutionModal;
