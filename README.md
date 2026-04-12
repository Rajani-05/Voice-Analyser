🎙️ Voice Analyser — AI Interview Evaluation System
An AI-powered web app that records your voice, transcribes your speech, and evaluates your interview answer based on keyword match, sentiment, and relevance to the job description.

📁 Folder Structure
Voice-Analyser/
│
├── 📂 backend/                        ← Backend (FastAPI API Server)
│   ├── main.py                        ← All API routes & AI logic
│   ├── requirements.txt               ← Python dependencies
│   └── (serves frontend too)
│
├── 📂 frontend/                       ← Frontend (React + Vite Web App)
│   ├── index.html                     ← Main HTML entry point
│   ├── package.json                   ← Node dependencies
│   ├── vite.config.js                 ← Vite build config
│   └── 📂 src/                        ← React components & pages
│
├── render.yaml                        ← Render.com deployment config
└── .gitignore
🔄 How It Works
User opens the web app (React UI)
         ↓
Records voice answer using microphone
         ↓
Audio sent to FastAPI backend → /transcribe
         ↓
Google Speech Recognition → converts audio to text
         ↓
Text + Job Description sent to → /evaluate
         ↓
Backend analyses:
  ✅ Keyword match (TF-IDF)
  ✅ Sentiment (VADER)
  ✅ Cosine similarity with JD
  ✅ Word count & specificity
         ↓
Returns Score (0–10), Grade & Feedback
