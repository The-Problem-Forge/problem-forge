import React from "react";
import { InlineMath, BlockMath } from "react-katex";
import "katex/dist/katex.min.css";

/**
 * KaTeXWrapper component for rendering LaTeX with KaTeX
 * @param {Object} props - Component props
 * @param {string} props.content - LaTeX content to render
 * @returns {React.Component} KaTeX rendered content
 */
const KaTeXWrapper = ({ content }) => {
  if (!content) return null;

  // Split content by math delimiters and render appropriately
  const parts = content.split(
    /(\$\$[\s\S]*?\$\$|\$[\s\S]*?\$|\\\[[\s\S]*?\\\]|\\\(.*?\\\))/g,
  );

  return (
    <div>
      {parts.map((part, index) => {
        if (part.startsWith("$$") && part.endsWith("$$")) {
          // Block math: $$...$$
          const math = part.slice(2, -2);
          return <BlockMath key={index} math={math} />;
        } else if (
          part.startsWith("$") &&
          part.endsWith("$") &&
          part.length > 2
        ) {
          // Inline math: $...$
          const math = part.slice(1, -1);
          return <InlineMath key={index} math={math} />;
        } else if (part.startsWith("\\[") && part.endsWith("\\]")) {
          // Block math: \[...\]
          const math = part.slice(2, -2);
          return <BlockMath key={index} math={math} />;
        } else if (part.startsWith("\\(") && part.endsWith("\\)")) {
          // Inline math: \(...\)
          const math = part.slice(2, -2);
          return <InlineMath key={index} math={math} />;
        } else {
          // Regular text
          return <span key={index}>{part}</span>;
        }
      })}
    </div>
  );
};

export default KaTeXWrapper;
