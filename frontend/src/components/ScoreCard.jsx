import { useEffect, useRef, useState } from "react";

const GRADE_COLORS = {
  Excellent: { ring: "#22c55e", glow: "rgba(34,197,94,0.3)", label: "🏆" },
  Good:      { ring: "#3b82f6", glow: "rgba(59,130,246,0.3)", label: "👍" },
  Average:   { ring: "#f59e0b", glow: "rgba(245,158,11,0.3)", label: "📈" },
  "Needs Work": { ring: "#ef4444", glow: "rgba(239,68,68,0.3)", label: "💪" },
};

function CircleProgress({ score, grade }) {
  const [animated, setAnimated] = useState(0);
  const colors = GRADE_COLORS[grade] || GRADE_COLORS["Average"];
  const R = 60;
  const CIRC = 2 * Math.PI * R;
  const offset = CIRC - (animated / 10) * CIRC;

  useEffect(() => {
    let start = null;
    const duration = 1200;
    const animate = (ts) => {
      if (!start) start = ts;
      const prog = Math.min((ts - start) / duration, 1);
      // Ease out cubic
      const eased = 1 - Math.pow(1 - prog, 3);
      setAnimated(eased * score);
      if (prog < 1) requestAnimationFrame(animate);
    };
    requestAnimationFrame(animate);
  }, [score]);

  return (
    <div className="circle-progress" style={{ filter: `drop-shadow(0 0 16px ${colors.glow})` }}>
      <svg width="160" height="160" viewBox="0 0 160 160">
        <circle cx="80" cy="80" r={R} fill="none" stroke="rgba(255,255,255,0.07)" strokeWidth="12" />
        <circle
          cx="80" cy="80" r={R} fill="none"
          stroke={colors.ring} strokeWidth="12"
          strokeLinecap="round"
          strokeDasharray={CIRC}
          strokeDashoffset={offset}
          style={{ transition: "stroke-dashoffset 0.05s", transform: "rotate(-90deg)", transformOrigin: "80px 80px" }}
        />
        <text x="80" y="74" textAnchor="middle" fill="white" fontSize="28" fontWeight="700" fontFamily="Inter, sans-serif">
          {animated.toFixed(1)}
        </text>
        <text x="80" y="94" textAnchor="middle" fill="rgba(255,255,255,0.5)" fontSize="12" fontFamily="Inter, sans-serif">
          out of 10
        </text>
      </svg>
    </div>
  );
}

function BreakdownBar({ label, value, max = 10, color }) {
  const [width, setWidth] = useState(0);
  useEffect(() => {
    const t = setTimeout(() => setWidth((value / max) * 100), 200);
    return () => clearTimeout(t);
  }, [value]);

  return (
    <div className="breakdown-item">
      <div className="breakdown-label">
        <span>{label}</span>
        <span className="breakdown-value">{value.toFixed(1)}</span>
      </div>
      <div className="breakdown-track">
        <div
          className="breakdown-fill"
          style={{ width: `${width}%`, background: color, transition: "width 0.8s cubic-bezier(.4,0,.2,1)" }}
        />
      </div>
    </div>
  );
}

export default function ScoreCard({ result }) {
  const { final_score, grade, score_breakdown } = result;
  const colors = GRADE_COLORS[grade] || GRADE_COLORS["Average"];

  const breakdown = [
    { label: "Keyword Match",   value: score_breakdown.keyword_score,     color: "#818cf8" },
    { label: "JD Similarity",  value: score_breakdown.similarity_score,   color: "#38bdf8" },
    { label: "Sentiment",      value: score_breakdown.sentiment_score,    color: "#34d399" },
    { label: "Answer Length",  value: score_breakdown.length_score,       color: "#fbbf24" },
    { label: "Specificity",    value: score_breakdown.specificity_score,  color: "#f472b6" },
  ];

  return (
    <div className="score-card">
      <div className="card-header">
        <span className="card-icon">🎯</span>
        <h3>Final Score</h3>
      </div>

      <div className="score-main">
        <CircleProgress score={final_score} grade={grade} />
        <div className="score-grade" style={{ color: colors.ring }}>
          <span className="grade-emoji">{colors.label}</span>
          <span className="grade-label">{grade}</span>
        </div>
      </div>

      <div className="breakdown-section">
        <h4 className="breakdown-title">Score Breakdown</h4>
        {breakdown.map((b) => (
          <BreakdownBar key={b.label} {...b} />
        ))}
      </div>
    </div>
  );
}
