import { useEffect, useState } from "react";

const SECTIONS = [
  {
    key: "strengths",
    title: "Strengths",
    icon: "✅",
    cls: "feedback-strengths",
    emptyMsg: "Keep practising to identify your strengths!",
  },
  {
    key: "improvements",
    title: "Areas to Improve",
    icon: "⚠️",
    cls: "feedback-improvements",
    emptyMsg: "Great job — no major improvement areas found!",
  },
  {
    key: "tips",
    title: "Pro Tips",
    icon: "💡",
    cls: "feedback-tips",
    emptyMsg: "",
  },
];

export default function FeedbackPanel({ feedback }) {
  const [visible, setVisible] = useState([]);

  useEffect(() => {
    // Stagger reveal
    SECTIONS.forEach((_, i) => {
      setTimeout(() => setVisible((v) => [...v, i]), i * 200);
    });
  }, [feedback]);

  return (
    <div className="feedback-card">
      <div className="card-header">
        <span className="card-icon">📋</span>
        <h3>Detailed Feedback</h3>
      </div>

      <div className="feedback-sections">
        {SECTIONS.map(({ key, title, icon, cls, emptyMsg }, idx) => {
          const items = feedback[key] || [];
          return (
            <div
              key={key}
              className={`feedback-section ${cls} ${visible.includes(idx) ? "section-visible" : "section-hidden"}`}
              style={{ transitionDelay: `${idx * 150}ms` }}
            >
              <h4 className="feedback-section-title">
                <span>{icon}</span> {title}
              </h4>
              {items.length > 0 ? (
                <ul className="feedback-list">
                  {items.map((item, i) => (
                    <li key={i} className="feedback-item">
                      <span className="feedback-bullet" />
                      {item}
                    </li>
                  ))}
                </ul>
              ) : (
                emptyMsg && <p className="feedback-empty">{emptyMsg}</p>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
