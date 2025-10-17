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
        tex2jax: {
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
        "https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.9/MathJax.js?config=TeX-AMS_HTML";
      script.async = true;
      document.head.appendChild(script);

      script.onload = () => {
        renderMath();
      };
    } else {
      renderMath();
    }

    function renderMath() {
      if (window.MathJax && window.MathJax.Hub && containerRef.current) {
        window.MathJax.Hub.Queue([
          "Typeset",
          window.MathJax.Hub,
          containerRef.current,
        ]);
      }
    }
  }, [content]);

  return <div ref={containerRef}>{content}</div>;
};

export default MathJaxWrapper;
