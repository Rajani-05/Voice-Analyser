import { useEffect, useState } from "react";

export default function TranscriptDisplay({ transcript, onChange }) {
  const [displayed, setDisplayed] = useState("");
  const [typing, setTyping] = useState(false);

  // Typewriter effect when a new transcript arrives
  useEffect(() => {
    if (!transcript) { setDisplayed(""); return; }
    setTyping(true);
    let i = 0;
    setDisplayed("");
    const interval = setInterval(() => {
      if (i < transcript.length) {
        setDisplayed(transcript.slice(0, i + 1));
        i++;
      } else {
        clearInterval(interval);
        setTyping(false);
      }
    }, 18);
    return () => clearInterval(interval);
  }, [transcript]);

  return (
    <div className="transcript-card">
      <div className="card-header">
        <span className="card-icon">📝</span>
        <h3>Transcribed Answer</h3>
        {typing && <span className="badge badge-live">LIVE</span>}
      </div>

      <textarea
        id="transcript-textarea"
        className="transcript-area"
        value={displayed}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Your transcribed answer will appear here…
You can also type or edit your answer directly."
        rows={6}
      />

      {displayed && (
        <div className="word-count">
          {displayed.split(/\s+/).filter(Boolean).length} words
        </div>
      )}
    </div>
  );
}
