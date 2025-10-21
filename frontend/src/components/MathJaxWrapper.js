import React, { useEffect, useRef } from "react";

/**
 * MathJaxWrapper component for rendering LaTeX with MathJax
 * @param {Object} props - Component props
 * @param {string} props.content - LaTeX content to render
 * @returns {React.Component} MathJax rendered content
 */
const MathJaxWrapper = ({ content }) => {
  const containerRef = useRef(null);

  useEffect(() => {
    // Load MathJax if not already loaded
    if (!window.MathJax) {
      window.MathJax = {
        tex: {
          inlineMath: [
            ["$", "$"],
            ["\\(", "\\)"],
          ],
          displayMath: [
            ["$$", "$$"],
            ["\\[", "\\]"],
          ],
        },
      };
      const script = document.createElement("script");
      script.src =
        "https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js";
      script.async = true;
      document.head.appendChild(script);

      script.onload = () => {
        renderMath();
      };
    } else {
      renderMath();
    }

    function renderMath() {
      if (window.MathJax && window.MathJax.startup && containerRef.current) {
        window.MathJax.startup.promise.then(() => {
          window.MathJax.typesetPromise([containerRef.current]).catch((err) =>
            console.error("MathJax rendering error:", err),
          );
        });
      }
    }
  }, [content]);

  return <div ref={containerRef}>{content}</div>;
};

export default MathJaxWrapper;
