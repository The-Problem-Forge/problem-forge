import React from "react";

/**
 * Reusable Table component for displaying data with customizable headers and rows
 * @param {Object} props - Component props
 * @param {Array} props.headers - Array of header objects with key and label
 * @param {Array} props.rows - Array of row data
 * @param {Function} props.renderCell - Function to render cell content (row, headerKey, index) => ReactNode
 * @param {Function} props.renderRow - Optional function to render entire row (row, index) => ReactNode
 * @param {string} props.emptyMessage - Message to show when no rows
 * @param {string} props.className - Additional CSS class
 * @returns {React.Component} Table component
 */
const Table = ({
  headers,
  rows,
  renderCell,
  renderRow,
  emptyMessage = "No data available",
  className = "",
}) => {
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
              {headers.map((header) => (
                <td key={header.key}>
                  {renderCell
                    ? renderCell(row, header.key, index)
                    : row[header.key] || "N/A"}
                </td>
              ))}
            </tr>
          );
        })}
      </tbody>
    </table>
  );
};

export default Table;
