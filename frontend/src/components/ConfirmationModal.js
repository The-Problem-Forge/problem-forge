import React from "react";

/**
 * Modal component for confirmation dialogs
 * @param {Object} props - Component props
 * @param {boolean} props.isOpen - Whether the modal is open
 * @param {string} props.message - Confirmation message to display
 * @param {Function} props.onConfirm - Callback when confirming the action
 * @param {Function} props.onCancel - Callback when canceling the action
 * @param {string} props.confirmText - Text for the confirm button (default: "Confirm")
 * @param {string} props.cancelText - Text for the cancel button (default: "Cancel")
 * @returns {React.Component} ConfirmationModal component
 */
const ConfirmationModal = ({
  isOpen,
  message,
  onConfirm,
  onCancel,
  confirmText = "Confirm",
  cancelText = "Cancel",
}) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content confirmation-modal">
        <div className="modal-header">
          <h3>Confirm Action</h3>
        </div>
        <div className="modal-body">
          <p>{message}</p>
        </div>
        <div className="modal-footer">
          <button className="cancel-btn" onClick={onCancel}>
            {cancelText}
          </button>
          <button className="confirm-btn" onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
