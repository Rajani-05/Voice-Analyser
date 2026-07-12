# 🎙️ Voice Analyser — AI Interview Evaluation System

An AI-powered web app that **records your voice**, **transcribes your speech**, and **evaluates your interview answer** based on keyword match, sentiment, and relevance to the job description.

Migrated from Python FastAPI to a highly scalable **Java Spring Boot** backend.

---


🚀 Live URL
👉 https://voice-analyser-rajani.netlify.app

## 📁 Folder Structure

```
Voice-Analyser/
│
├── 📂 backend-java/                  ← Backend (Spring Boot API Server)
│   ├── pom.xml                       ← Maven configuration
│   ├── mvnw / mvnw.cmd               ← Maven wrapper executables
│   └── 📂 src/                       ← Spring Boot controllers, services, and DTOs
│
├── 📂 frontend/                      ← Frontend (React + Vite Web App)
│   ├── index.html                    ← Vite entry point
│   ├── package.json                  ← Node dependencies
│   ├── vite.config.js                ← Vite build config with dev proxy
│   └── 📂 src/                       ← React components & pages
│
├── Dockerfile                        ← Multi-stage Docker build (production ready)
├── render.yaml                       ← Render.com deployment config
└── .gitignore                        ← Git ignored files
```

---

## 🔄 How It Works

```
User opens the web app (React UI)
         ↓
Records voice answer using microphone
         ↓
Audio sent to Spring Boot backend → /transcribe
         ↓
Gemini API → converts audio to text transcript
         ↓
Text + Job Description sent to → /evaluate
         ↓
Backend analyses the answer:
   ✅ Keyword match (Local heuristics / Gemini NLP)
   ✅ Cosine similarity with JD (Local Cosine Sim / Gemini semantic relevance)
   ✅ Sentiment (Local polarity / Gemini sentiment analysis)
   ✅ Word count & specificity
         ↓
Returns Score (0–10), Grade & Feedback
         ↓
Frontend displays results to user (React dashboard)
```

---
## Live Demo

Frontend:
https://voice-analyser-rajani.netlify.app

Backend API:
https://voice-analyser-rajani.onrender.com

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

## 🌐 API Endpoints (Spring Boot)

| Method | Endpoint | What it does |
|--------|----------|-------------|
| `GET` | `/` | Serves the built frontend app |
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
  "transcript": "I have 3 years of Python experience and led a team of 5...",
  "word_count": 12,
  "sentiment": {
    "label": "Positive",
    "compound": 0.62,
    "positive": 0.45,
    "neutral": 0.55,
    "negative": 0.0
  },
  "keywords": {
    "matched": ["python", "leadership"],
    "missing": ["kubernetes", "docker"],
    "match_pct": 50.0,
    "cosine_similarity": 0.45
  },
  "score_breakdown": {
    "keyword": 8.0,
    "similarity": 7.5,
    "sentiment": 9.0,
    "length": 10.0,
    "specificity": 8.5
  },
  "final_score": 8.25,
  "grade": "Good",
  "feedback": {
    "strengths": ["Good keyword coverage", "Confident tone"],
    "improvements": ["Add more specific examples"],
    "tips": ["Use the STAR method: Situation → Task → Action → Result"]
  }
}
```

---

## 🚀 How to Run Locally

### Step 1 — Start the Spring Boot Backend

Set the `GEMINI_API_KEY` environment variable to use Gemini features, otherwise the system will use a local fallback engine for evaluation.

```bash
cd backend-java
# Set your API Key (Powershell)
$env:GEMINI_API_KEY="your-gemini-key"

# Run Spring Boot
./mvnw.cmd spring-boot:run
```

The Spring Boot backend runs at → http://localhost:8000

---

### Step 2 — Start the React Frontend

```bash
cd frontend
npm install
npm run dev
```

The React frontend starts at → http://localhost:5173 (which automatically proxies api requests to port 8000).

---

## 🌍 Production Build & Deployment

The application features a root-level `Dockerfile` using a multi-stage configuration:
1. **Frontend Builder**: Installs dependencies and compiles React assets into `/frontend/dist`.
2. **Backend Builder**: Builds the Spring Boot `.jar` package.
3. **Runner**: Bundles the `.jar` package with the built frontend assets and exposes port `8000`.

To deploy on platforms like Render.com, create a **Web Service** referencing this repository and select the **Docker** runtime.
