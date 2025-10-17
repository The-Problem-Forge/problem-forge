import React, { useState } from "react";

/**
 * Reusable EditableCell component for inline editing in tables
 * @param {Object} props - Component props
 * @param {string|number} props.value - Current value
 * @param {string} props.type - Input type ('text', 'textarea')
 * @param {Function} props.onSave - Function called when saving (newValue) => void
 * @param {Function} props.onCancel - Function called when canceling
 * @param {string} props.placeholder - Placeholder text
 * @param {number} props.rows - Number of rows for textarea
 * @returns {React.Component} EditableCell component
 */
const EditableCell = ({
  value,
  type = "text",
  onSave,
  onCancel,
  placeholder = "",
  rows = 3,
}) => {
  const [editValue, setEditValue] = useState(value || "");

  const handleSave = () => {
    onSave(editValue);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && type === "text") {
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
