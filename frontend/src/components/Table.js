import React, { useState } from "react";
import EditableCell from "./EditableCell";

/**
 * Reusable Table component for displaying data with customizable headers and rows
 * @param {Object} props - Component props
 * @param {Array} props.headers - Array of header objects with key, label, and optional editable field
 * @param {Array} props.rows - Array of row data
 * @param {Function} props.renderCell - Function to render cell content (row, headerKey, index) => ReactNode
 * @param {Function} props.renderRow - Optional function to render entire row (row, index) => ReactNode
 * @param {Function} props.renderEditableCell - Function to render editable cell content (row, headerKey, index, isEditing, onEdit, onSave, onCancel) => ReactNode
 * @param {Function} props.renderActions - Function to render actions cell (row, index) => ReactNode
 * @param {Function} props.onEdit - Function called when editing starts (rowId, columnKey) => void
 * @param {Function} props.onSave - Function called when saving (rowId, columnKey, newValue) => void
 * @param {Function} props.onCancel - Function called when canceling edit
 * @param {string} props.emptyMessage - Message to show when no rows
 * @param {string} props.className - Additional CSS class
 * @returns {React.Component} Table component
 */
const Table = ({
  headers,
  rows,
  renderCell,
  renderRow,
  renderEditableCell,
  renderActions,
  onEdit,
  onSave,
  onCancel,
  emptyMessage = "No data available",
  className = "",
}) => {
  const [editingCell, setEditingCell] = useState(null);

  const handleEdit = (rowId, columnKey) => {
    setEditingCell({ rowId, columnKey });
    if (onEdit) onEdit(rowId, columnKey);
  };

  const handleSave = (rowId, columnKey, newValue) => {
    setEditingCell(null);
    if (onSave) onSave(rowId, columnKey, newValue);
  };

  const handleCancel = () => {
    setEditingCell(null);
    if (onCancel) onCancel();
  };

  if (rows.length === 0) {
    return <p>{emptyMessage}</p>;
  }

  return (
    <table
      className={`table ${className}`}
      style={{ tableLayout: "fixed", width: "100%" }}
    >
      <thead>
        <tr>
          {headers.map((header) => (
            <th key={header.key}>{header.label}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, index) => {
          if (renderRow) {
            return renderRow(row, index);
          }
          return (
            <tr key={row.id || index} style={{ height: "80px" }}>
              {headers.map((header) => {
                const isEditing =
                  editingCell?.rowId === row.id &&
                  editingCell?.columnKey === header.key;
                const isEditable = header.editable;

                if (header.key === "actions" && renderActions) {
                  return (
                    <td key={header.key} className="actions-cell">
                      {renderActions(row, index)}
                    </td>
                  );
                }

                if (isEditable && header.type === "select") {
                  return (
                    <td key={header.key}>
                      <select
                        value={row[header.key] || ""}
                        onChange={(e) =>
                          handleSave(row.id, header.key, e.target.value)
                        }
                        style={{ width: "100%", height: "100%" }}
                      >
                        {header.options?.map((option) => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                    </td>
                  );
                }

                if (isEditable && header.type === "checkbox") {
                  return (
                    <td key={header.key}>
                      <input
                        type="checkbox"
                        checked={row[header.key] || false}
                        onChange={(e) =>
                          handleSave(row.id, header.key, e.target.checked)
                        }
                        style={{ width: "100%", height: "100%" }}
                      />
                    </td>
                  );
                }

                return (
                  <td
                    key={header.key}
                    onClick={
                      isEditable && header.type !== "select"
                        ? () => handleEdit(row.id, header.key)
                        : undefined
                    }
                    style={
                      isEditable && header.type !== "select"
                        ? { cursor: "pointer" }
                        : {}
                    }
                  >
                    {isEditing ? (
                      <EditableCell
                        value={row[header.key] || ""}
                        type={header.type || "text"}
                        onSave={(newValue) =>
                          handleSave(row.id, header.key, newValue)
                        }
                        onCancel={handleCancel}
                        placeholder={header.placeholder || ""}
                        rows={header.rows || 3}
                        options={header.options || []}
                      />
                    ) : renderEditableCell ? (
                      renderEditableCell(
                        row,
                        header.key,
                        index,
                        isEditing,
                        () => handleEdit(row.id, header.key),
                        (newValue) => handleSave(row.id, header.key, newValue),
                        handleCancel,
                      )
                    ) : renderCell ? (
                      renderCell(row, header.key, index, isEditing, () =>
                        handleEdit(row.id, header.key),
                      )
                    ) : (
                      <span>
                        {row[header.key] !== undefined &&
                        row[header.key] !== null
                          ? row[header.key]
                          : "N/A"}
                      </span>
                    )}
                  </td>
                );
              })}
            </tr>
          );
        })}
      </tbody>
    </table>
  );
};

export default Table;
