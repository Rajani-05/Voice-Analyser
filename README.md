# 🎙️ Voice Analyser — AI Interview Evaluation System

An AI-powered web app that **records your voice**, **transcribes your speech**, and **evaluates your interview answer** based on keyword match, sentiment, and relevance to the job description.

---

## 📁 Folder Structure

```
Voice-Analyser/
│
├── 📂 backend/                        ← Backend (FastAPI API Server)
│   ├── main.py                        ← All API routes & AI logic
│   ├── requirements.txt               ← Python dependencies
│   └── (also serves the frontend)
│
├── 📂 frontend/                       ← Frontend (React + Vite Web App)
│   ├── index.html                     ← Main HTML entry point
│   ├── package.json                   ← Node dependencies
│   ├── vite.config.js                 ← Vite build config
│   └── 📂 src/                        ← React components & pages
│
├── render.yaml                        ← Render.com deployment config
└── .gitignore                         ← Git ignored files
```

---

## 🔄 How It Works

```
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
Backend analyses the answer:
  ✅ Keyword match (TF-IDF)
  ✅ Sentiment (VADER)
  ✅ Cosine similarity with JD
  ✅ Word count & specificity
         ↓
Returns Score (0–10), Grade & Feedback
         ↓
Frontend displays results to user
```

---

## 🧠 How the Score is Calculated

| Factor | Weight | What it checks |
|--------|--------|----------------|
| Keyword Match | 35% | How many JD keywords appear in your answer |
| Cosine Similarity | 25% | Overall relevance to the job description |
| Sentiment | 15% | Is your tone positive, neutral or negative? |
| Answer Length | 15% | Did you give a full answer (60–200 words)? |
| Specificity | 10% | Did you use specific, meaningful words? |

### Grades

| Score | Grade |
|-------|-------|
| 8.5 – 10.0 | ⭐ Excellent |
| 7.0 – 8.4 | ✅ Good |
| 5.0 – 6.9 | 🔶 Average |
| Below 5.0 | ❌ Needs Work |

---

## 🌐 API Endpoints (FastAPI)

| Method | Endpoint | What it does |
|--------|----------|-------------|
| `GET` | `/` | Serves the frontend app |
| `POST` | `/transcribe` | Receives audio → returns transcript text |
| `POST` | `/evaluate` | Receives transcript + JD → returns score & feedback |
| `GET` | `/health` | Health check → `{"status": "ok"}` |

### `/evaluate` — Request Body Example

```json
{
  "transcript": "I have 3 years of Python experience and led a team of 5...",
  "job_description": "Looking for a Python developer with team leadership skills...",
  "question": "Tell me about your experience"
}
```

### `/evaluate` — Response Example

```json
{
  "final_score": 7.85,
  "grade": "Good",
  "sentiment": { "label": "Positive", "compound": 0.62 },
  "keywords": {
    "matched": ["python", "leadership"],
    "match_pct": 65.0
  },
  "feedback": {
    "strengths": ["Good keyword coverage", "Confident tone"],
    "improvements": ["Add more specific examples"],
    "tips": ["Use the STAR method: Situation → Task → Action → Result"]
  }
}
```

---

## 🚀 How to Run Locally

### Step 1 — Start the Backend

```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

Backend runs at → http://localhost:8000

---

### Step 2 — Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at → http://localhost:5173

---

## 🌍 Live Deployment (Render)

| Service | URL |
|---------|-----|
| Frontend App | https://rajani-voice-analyser.onrender.com |
| Backend API | https://rajani-voice-analyser-api.onrender.com |

> ⚠️ Free tier may **sleep** after inactivity — first request takes ~30 seconds to wake up.

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Frontend | React + Vite | Web UI (voice recorder, results display) |
| Backend | FastAPI (Python) | REST API server |
| Speech-to-Text | Google Speech Recognition | Converts audio to text |
| Sentiment Analysis | VADER | Detects tone (positive/negative/neutral) |
| Keyword Matching | TF-IDF + Cosine Similarity | Matches answer to job description |
| Deployment | Render.com | Cloud hosting |

---

## 📦 Key Python Libraries

| Library | Purpose |
|---------|---------|
| `fastapi` | Build the REST API |
| `uvicorn` | Run the FastAPI server |
| `speechrecognition` | Convert voice audio to text |
| `vaderSentiment` | Sentiment analysis |
| `scikit-learn` | TF-IDF keyword extraction & cosine similarity |
| `pydantic` | Validate API request/response data |

---

## 🗂️ GitHub Repository

```
https://github.com/Rajani-05/Voice-Analyser
```
