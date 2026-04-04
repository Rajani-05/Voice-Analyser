import { useState, useEffect } from "react";

const SENTIMENT_CONFIG = {
  Positive: {
    emoji: "😊",
    label: "Positive Tone",
    color: "#22c55e",
    glow: "rgba(34,197,94,0.2)",
    desc: "Your answer comes across as confident and enthusiastic.",
  },
  Neutral: {
    emoji: "😐",
    label: "Neutral Tone",
    color: "#94a3b8",
    glow: "rgba(148,163,184,0.2)",
    desc: "Your tone is balanced. Try adding more enthusiasm.",
  },
  Negative: {
    emoji: "😟",
    label: "Negative Tone",
    color: "#ef4444",
    glow: "rgba(239,68,68,0.2)",
    desc: "Your answer sounds pessimistic. Reframe challenges positively.",
  },
};

export default function SentimentBadge({ sentiment }) {
  const [barWidth, setBarWidth] = useState(0);
  const { label, compound, positive, neutral, negative } = sentiment;
  const cfg = SENTIMENT_CONFIG[label] || SENTIMENT_CONFIG.Neutral;

  // Normalize compound (-1 to 1) → 0 to 100
  const compoundPct = Math.round(((compound + 1) / 2) * 100);

  useEffect(() => {
    const t = setTimeout(() => setBarWidth(compoundPct), 100);
    return () => clearTimeout(t);
  }, [compoundPct]);

  return (
    <div className="sentiment-card" style={{ boxShadow: `0 0 24px ${cfg.glow}` }}>
      <div className="card-header">
        <span className="card-icon">🧠</span>
        <h3>Sentiment Analysis</h3>
      </div>

      <div className="sentiment-main">
        <div className="sentiment-emoji" style={{ filter: `drop-shadow(0 0 12px ${cfg.glow})` }}>
          {cfg.emoji}
        </div>
        <div className="sentiment-info">
          <span className="sentiment-label" style={{ color: cfg.color }}>{cfg.label}</span>
          <p className="sentiment-desc">{cfg.desc}</p>
        </div>
      </div>

      <div className="sentiment-bar-section">
        <div className="sentiment-bar-label">
          <span>Compound Score</span>
          <span style={{ color: cfg.color }}>{compound >= 0 ? "+" : ""}{compound.toFixed(3)}</span>
        </div>
        <div className="sentiment-track">
          <div className="sentiment-mid-marker" />
          <div
            className="sentiment-fill"
            style={{
              width: `${barWidth}%`,
              background: `linear-gradient(90deg, #ef4444, #f59e0b, #22c55e)`,
              transition: "width 1s cubic-bezier(.4,0,.2,1)"
            }}
          />
        </div>
        <div className="sentiment-scale">
          <span>Negative</span>
          <span>Neutral</span>
          <span>Positive</span>
        </div>
      </div>

      <div className="sentiment-breakdown">
        {[
          { label: "Positive", value: positive, color: "#22c55e" },
          { label: "Neutral",  value: neutral,  color: "#94a3b8" },
          { label: "Negative", value: negative, color: "#ef4444" },
        ].map(({ label: l, value, color }) => (
          <div key={l} className="sentiment-mini">
            <span style={{ color }}>{l}</span>
            <span>{(value * 100).toFixed(0)}%</span>
          </div>
        ))}
      </div>
    </div>
  );
}
