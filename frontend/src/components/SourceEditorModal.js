import React, { useState, useEffect } from "react";

/**
 * Modal component for editing solution source code
 * @param {Object} props - Component props
 * @param {boolean} props.isOpen - Whether the modal is open
 * @param {string} props.solutionId - Solution ID being edited
 * @param {string} props.initialContent - Initial source code content
 * @param {Function} props.onSave - Callback when saving the source code
 * @param {Function} props.onClose - Callback when closing the modal
 * @param {boolean} props.saving - Whether the save operation is in progress
 * @returns {React.Component} SourceEditorModal component
 */
const SourceEditorModal = ({
  isOpen,
  solutionId,
  initialContent,
  onSave,
  onClose,
  saving,
}) => {
  const [content, setContent] = useState("");

  useEffect(() => {
    if (isOpen) {
      setContent(initialContent || "");
    }
  }, [isOpen, initialContent]);

  /**
   * Handles content changes in the textarea
   * @param {Event} e - Textarea change event
   */
  const handleContentChange = (e) => {
    setContent(e.target.value);
  };

  /**
   * Handles save button click
   */
  const handleSave = () => {
    onSave(content);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content source-editor-modal">
        <div className="source-editor-header">
          <h3>Edit Solution Source</h3>
          <div className="source-editor-info">
            <small>Solution ID: {solutionId}</small>
          </div>
        </div>

        <div className="source-editor-body">
          <textarea
            value={content}
            onChange={handleContentChange}
            className="source-editor-textarea"
            placeholder="Enter solution source code..."
            spellCheck={false}
            autoFocus
          />
        </div>

        <div className="source-editor-footer">
          <div className="modal-buttons">
            <button
              type="button"
              className="cancel-btn"
              onClick={onClose}
              disabled={saving}
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleSave}
              disabled={saving || content === initialContent}
            >
              {saving ? "Saving..." : "Save"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SourceEditorModal;
