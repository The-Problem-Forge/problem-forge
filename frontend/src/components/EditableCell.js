import React, { useState, useEffect, useRef } from "react";

/**
 * Reusable EditableCell component for inline editing in tables
 * @param {Object} props - Component props
 * @param {string|number} props.value - Current value
 * @param {string} props.type - Input type ('text', 'textarea', 'select')
 * @param {Function} props.onSave - Function called when saving (newValue) => void
 * @param {Function} props.onCancel - Function called when canceling
 * @param {string} props.placeholder - Placeholder text
 * @param {number} props.rows - Number of rows for textarea
 * @param {Array} props.options - Options for select type [{value, label}]
 * @returns {React.Component} EditableCell component
 */
const EditableCell = ({
  value,
  type = "text",
  onSave,
  onCancel,
  placeholder = "",
  rows = 3,
  options = [],
}) => {
  const [editValue, setEditValue] = useState(value || "");
  const selectRef = useRef(null);

  useEffect(() => {
    if (type === "select" && selectRef.current) {
      selectRef.current.focus();
    }
  }, [type]);

  const handleSave = () => {
    onSave(editValue);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && (type === "text" || type === "select")) {
      handleSave();
    } else if (e.key === "Escape") {
      onCancel();
    }
  };

  if (type === "textarea") {
    return (
      <textarea
        value={editValue}
        onChange={(e) => setEditValue(e.target.value)}
        placeholder={placeholder}
        rows={rows}
        autoFocus
        onBlur={handleSave}
        onKeyDown={handleKeyDown}
      />
    );
  }

  if (type === "select") {
    return (
      <select
        ref={selectRef}
        value={editValue}
        onChange={(e) => setEditValue(e.target.value)}
        onBlur={handleSave}
        onKeyDown={handleKeyDown}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    );
  }

  return (
    <input
      type="text"
      value={editValue}
      onChange={(e) => setEditValue(e.target.value)}
      placeholder={placeholder}
      autoFocus
      onBlur={handleSave}
      onKeyDown={handleKeyDown}
    />
  );
};

export default EditableCell;
