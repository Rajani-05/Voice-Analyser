export default function KeywordCloud({ keywords }) {
  const { matched_keywords, missing_keywords, match_percentage, cosine_similarity } = keywords;

  return (
    <div className="keyword-card">
      <div className="card-header">
        <span className="card-icon">🔍</span>
        <h3>Keyword Analysis</h3>
      </div>

      {/* Stats row */}
      <div className="kw-stats-row">
        <div className="kw-stat">
          <span className="kw-stat-value" style={{ color: "#22c55e" }}>
            {match_percentage.toFixed(0)}%
          </span>
          <span className="kw-stat-label">Match Rate</span>
        </div>
        <div className="kw-stat">
          <span className="kw-stat-value" style={{ color: "#38bdf8" }}>
            {matched_keywords.length}
          </span>
          <span className="kw-stat-label">Matched</span>
        </div>
        <div className="kw-stat">
          <span className="kw-stat-value" style={{ color: "#f87171" }}>
            {missing_keywords.length}
          </span>
          <span className="kw-stat-label">Missing</span>
        </div>
        <div className="kw-stat">
          <span className="kw-stat-value" style={{ color: "#c084fc" }}>
            {(cosine_similarity * 100).toFixed(0)}%
          </span>
          <span className="kw-stat-label">Similarity</span>
        </div>
      </div>

      {/* Match bar */}
      <div className="kw-match-bar-container">
        <div
          className="kw-match-bar-fill"
          style={{ width: `${match_percentage}%` }}
        />
      </div>

      {/* Matched keywords */}
      {matched_keywords.length > 0 && (
        <div className="kw-section">
          <p className="kw-section-title">
            <span className="kw-dot dot-green" /> Matched Keywords
          </p>
          <div className="kw-chips">
            {matched_keywords.map((kw) => (
              <span key={kw} className="chip chip-matched">{kw}</span>
            ))}
          </div>
        </div>
      )}

      {/* Missing keywords */}
      {missing_keywords.length > 0 && (
        <div className="kw-section">
          <p className="kw-section-title">
            <span className="kw-dot dot-red" /> Missing from Your Answer
          </p>
          <div className="kw-chips">
            {missing_keywords.map((kw) => (
              <span key={kw} className="chip chip-missing">{kw}</span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
