import React from "react";
import "../styles/InvocationModal.scss";

/**
 * Modal component for creating new invocations
 * @param {Object} props - Component props
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Function to close modal
 * @param {Array} props.solutions - Available solutions
 * @param {Array} props.tests - Available tests
 * @param {Array} props.selectedSolutions - Currently selected solution IDs
 * @param {Array} props.selectedTests - Currently selected test IDs
 * @param {Function} props.onToggleSolution - Toggle solution selection
 * @param {Function} props.onToggleTest - Toggle test selection
 * @param {Function} props.onSelectAllSolutions - Select/deselect all solutions
 * @param {Function} props.onSelectAllTests - Select/deselect all tests
 * @param {Function} props.onCreate - Create invocation function
 * @param {boolean} props.creating - Whether creation is in progress
 * @param {string} props.error - Error message to display
 * @returns {React.Component} Modal component
 */
const InvocationModal = ({
  isOpen,
  onClose,
  solutions,
  tests,
  selectedSolutions,
  selectedTests,
  onToggleSolution,
  onToggleTest,
  onSelectAllSolutions,
  onSelectAllTests,
  onCreate,
  creating,
  error,
}) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Create Invocation</h3>
          <button className="modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          {error && <p className="modal-error">{error}</p>}

          <div className="modal-selectors">
            <div className="solutions-selector">
              <h4>Select Solutions</h4>
              {solutions.length > 0 && (
                <button
                  onClick={onSelectAllSolutions}
                  className="select-all-btn"
                >
                  {solutions.every((solution) =>
                    selectedSolutions.includes(solution.id),
                  )
                    ? "Deselect All"
                    : "Select All"}
                </button>
              )}
              {solutions.length === 0 ? (
                <p>No solutions available</p>
              ) : (
                <div className="selector-list">
                  {solutions.map((solution) => (
                    <div
                      key={solution.id}
                      className={`selector-item ${
                        selectedSolutions.includes(solution.id)
                          ? "selected"
                          : ""
                      }`}
                      onClick={() => onToggleSolution(solution.id)}
                    >
                      <div>{solution.name}</div>
                      <div className="subtext">
                        {solution.language}, {solution.solutionType}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="tests-selector">
              <h4>Select Tests</h4>
              {tests.length > 0 && (
                <button onClick={onSelectAllTests} className="select-all-btn">
                  {tests.every((test) => selectedTests.includes(test.id))
                    ? "Deselect All"
                    : "Select All"}
                </button>
              )}
              {tests.length === 0 ? (
                <p>No tests available</p>
              ) : (
                <div className="selector-list">
                  {tests.map((test) => (
                    <div
                      key={test.id}
                      className={`selector-item ${
                        selectedTests.includes(test.id) ? "selected" : ""
                      }`}
                      onClick={() => onToggleTest(test.id)}
                    >
                      Test {test.id}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button
            onClick={onCreate}
            disabled={creating}
            className="create-invocation-btn"
          >
            {creating ? "Creating..." : "Create Invocation"}
          </button>
          <button onClick={onClose} className="cancel-btn">
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default InvocationModal;
