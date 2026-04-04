import { useState } from "react";
import VoiceRecorder from "./components/VoiceRecorder";
import TranscriptDisplay from "./components/TranscriptDisplay";
import ScoreCard from "./components/ScoreCard";
import SentimentBadge from "./components/SentimentBadge";
import KeywordCloud from "./components/KeywordCloud";
import FeedbackPanel from "./components/FeedbackPanel";
import { evaluateAnswer } from "./services/api";

const JD_TEMPLATES = {
  custom: "",
  software_engineer: `We are looking for a Software Engineer with 3+ years of experience.
Required skills: Python, JavaScript, React, REST APIs, microservices, Docker, Kubernetes, CI/CD pipelines, Git.
Experience with cloud platforms (AWS/GCP/Azure), agile methodologies, and unit testing is required.
Strong problem-solving skills and ability to work in a collaborative team environment.`,

  data_scientist: `Seeking a Data Scientist with expertise in machine learning and statistical analysis.
Required: Python, R, TensorFlow, PyTorch, scikit-learn, pandas, NumPy, SQL.
Experience with NLP, deep learning, model deployment, feature engineering, and data visualization (Matplotlib, Seaborn, Tableau).
Strong background in statistics and ability to communicate insights to non-technical stakeholders.`,

  product_manager: `Looking for a Product Manager to lead product strategy and roadmap.
Required: product lifecycle management, user story writing, stakeholder management, agile/scrum, roadmap planning.
Experience with data analytics, A/B testing, user research, and cross-functional team leadership.
Strong communication, prioritization, and strategic thinking skills.`,

  devops_engineer: `DevOps Engineer needed for cloud infrastructure and automation.
Required: Docker, Kubernetes, Terraform, Jenkins, CI/CD, Linux, Bash scripting.
Experience with AWS/Azure/GCP, monitoring (Prometheus, Grafana), security best practices, and microservices architecture.
Strong understanding of networking, load balancing, and high-availability systems.`,
};

const SAMPLE_QUESTIONS = {
  software_engineer: "Tell me about your experience with React and frontend development.",
  data_scientist: "How have you applied machine learning to solve real business problems?",
  product_manager: "Describe a product you launched from scratch. What was your process?",
  devops_engineer: "How do you handle infrastructure as code and deployment automation?",
  custom: "Tell me about yourself and why you are a good fit for this role.",
};

const STATUS_MESSAGES = {
  idle: null,
  recording: "🔴 Recording… speak your answer clearly",
  transcribing: "⏳ Transcribing your speech…",
  evaluating: "🧠 Analysing your response…",
  done: null,
  error: "❌ An error occurred",
};

export default function App() {
  const [jdKey, setJdKey] = useState("software_engineer");
  const [customJD, setCustomJD] = useState("");
  const [question, setQuestion] = useState(SAMPLE_QUESTIONS["software_engineer"]);
  const [transcript, setTranscript] = useState("");
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [statusMsg, setStatusMsg] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const jdText = jdKey === "custom" ? customJD : JD_TEMPLATES[jdKey];

  const handleJDChange = (key) => {
    setJdKey(key);
    setQuestion(SAMPLE_QUESTIONS[key] || "");
    setResult(null);
    setTranscript("");
    setError(null);
  };

  const handleStatus = (s, msg = null) => {
    setStatus(s);
    setStatusMsg(msg || STATUS_MESSAGES[s]);
    if (s === "error") setError(msg || "Something went wrong.");
    else setError(null);
  };

  const handleEvaluate = async () => {
    if (!transcript.trim()) {
      setError("Please provide an answer first — record your voice or type your answer.");
      return;
    }
    if (!jdText.trim() || jdText.trim().length < 20) {
      setError("Please select or provide a job description.");
      return;
    }

    setLoading(true);
    setError(null);
    setResult(null);
    handleStatus("evaluating");

    try {
      const data = await evaluateAnswer(transcript, jdText, question);
      setResult(data);
      handleStatus("done");
    } catch (err) {
      const msg = err.response?.data?.detail || err.message || "Evaluation failed.";
      setError(msg);
      handleStatus("error", msg);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setTranscript("");
    setResult(null);
    setStatus("idle");
    setError(null);
    setStatusMsg(null);
  };

  return (
    <div className="app">
      {/* Background orbs */}
      <div className="bg-orb orb-1" />
      <div className="bg-orb orb-2" />
      <div className="bg-orb orb-3" />

      {/* Header */}
      <header className="header">
        <div className="header-inner">
          <div className="logo">
            <span className="logo-icon">🎙️</span>
            <div>
              <h1 className="logo-title">InterviewAI</h1>
              <p className="logo-sub">Intelligent Interview Evaluation System</p>
            </div>
          </div>
          <div className="header-badges">
            <span className="badge badge-ai">AI Powered</span>
            <span className="badge badge-nlp">NLP</span>
            <span className="badge badge-stt">Speech-to-Text</span>
          </div>
        </div>
      </header>

      <main className="main">
        {/* Setup Section */}
        <section className="setup-section glass-card">
          <h2 className="section-title">
            <span>⚙️</span> Interview Setup
          </h2>

          <div className="setup-grid">
            {/* Job Role Selector */}
            <div className="form-group">
              <label className="form-label" htmlFor="jd-select">Job Role Template</label>
              <select
                id="jd-select"
                className="form-select"
                value={jdKey}
                onChange={(e) => handleJDChange(e.target.value)}
              >
                <option value="software_engineer">💻 Software Engineer</option>
                <option value="data_scientist">📊 Data Scientist</option>
                <option value="product_manager">📦 Product Manager</option>
                <option value="devops_engineer">🔧 DevOps Engineer</option>
                <option value="custom">✏️ Custom JD</option>
              </select>
            </div>

            {/* Interview Question */}
            <div className="form-group">
              <label className="form-label" htmlFor="question-input">Interview Question</label>
              <input
                id="question-input"
                type="text"
                className="form-input"
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                placeholder="Enter the interview question…"
              />
            </div>

            {/* JD display / custom input */}
            <div className="form-group full-width">
              <label className="form-label">
                Job Description {jdKey !== "custom" && <span className="label-hint">(template)</span>}
              </label>
              <textarea
                id="jd-textarea"
                className="form-textarea"
                value={jdKey === "custom" ? customJD : jdText}
                onChange={(e) => jdKey === "custom" && setCustomJD(e.target.value)}
                readOnly={jdKey !== "custom"}
                rows={5}
                placeholder="Paste your Job Description here…"
              />
            </div>
          </div>
        </section>

        {/* Recorder Section */}
        <section className="recorder-section glass-card">
          <h2 className="section-title">
            <span>🎤</span> Record Your Answer
          </h2>
          <p className="section-desc">
            Click <strong>Start Recording</strong> and speak your answer, or type directly below.
          </p>
          <VoiceRecorder
            onTranscript={(t) => { setTranscript(t); setResult(null); }}
            onStatus={handleStatus}
          />

          <TranscriptDisplay
            transcript={transcript}
            onChange={(t) => { setTranscript(t); setResult(null); }}
          />

          {/* Status banner */}
          {statusMsg && !error && (
            <div className="status-banner">{statusMsg}</div>
          )}
          {error && (
            <div className="error-banner">{error}</div>
          )}

          {/* CTA buttons */}
          <div className="cta-row">
            <button
              id="btn-evaluate"
              className="btn-evaluate"
              onClick={handleEvaluate}
              disabled={loading || !transcript.trim()}
            >
              {loading ? (
                <><span className="spinner" /> Evaluating…</>
              ) : (
                <><span>🚀</span> Evaluate My Answer</>
              )}
            </button>
            {(transcript || result) && (
              <button id="btn-reset" className="btn-reset" onClick={handleReset}>
                🔄 Reset
              </button>
            )}
          </div>
        </section>

        {/* Results Section */}
        {result && (
          <section className="results-section">
            <div className="results-header">
              <h2 className="section-title results-title">
                <span>📊</span> Evaluation Results
              </h2>
              <span className="results-grade-badge" style={{
                background: result.grade === "Excellent" ? "#22c55e" :
                  result.grade === "Good" ? "#3b82f6" :
                  result.grade === "Average" ? "#f59e0b" : "#ef4444"
              }}>
                {result.grade}
              </span>
            </div>

            <div className="results-grid">
              {/* Score Card */}
              <div className="result-cell result-score">
                <ScoreCard result={result} />
              </div>

              {/* Sentiment */}
              <div className="result-cell result-sentiment">
                <SentimentBadge sentiment={result.sentiment} />
              </div>

              {/* Keywords — full width */}
              <div className="result-cell result-keywords">
                <KeywordCloud keywords={result.keywords} />
              </div>

              {/* Feedback — full width */}
              <div className="result-cell result-feedback">
                <FeedbackPanel feedback={result.feedback} />
              </div>
            </div>
          </section>
        )}
      </main>

      <footer className="footer">
        <p>InterviewAI — Built with FastAPI · VADER · TF-IDF · React</p>
      </footer>
    </div>
  );
}
