"""
AI Interview Evaluation System — Single-file FastAPI backend
No NLTK. No heavy dependencies. Starts in seconds.
"""
import re
import string
import base64
import io
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from pydantic import BaseModel
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import speech_recognition as sr

# ── App ────────────────────────────────────────────────────────────────
app = FastAPI(title="Interview Evaluator", docs_url="/api/docs")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "*",
        "https://rajani-voice-analyser.onrender.com",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Serve the frontend index.html
import os, pathlib
FRONTEND = pathlib.Path(__file__).parent.parent / "frontend"
if FRONTEND.exists():
    app.mount("/static", StaticFiles(directory=str(FRONTEND)), name="static")

@app.get("/")
def root():
    idx = FRONTEND / "index.html"
    if idx.exists():
        return FileResponse(str(idx))
    return {"status": "ok", "docs": "/api/docs"}

# ── State ──────────────────────────────────────────────────────────────
_vader = SentimentIntensityAnalyzer()
_recognizer = sr.Recognizer()

STOP_WORDS = {
    "i","me","my","we","our","you","your","he","she","it","they","them",
    "the","a","an","and","or","but","in","on","at","to","for","of","with",
    "is","are","was","were","be","been","have","has","had","do","does","did",
    "will","would","could","should","can","may","might","this","that","these",
    "those","am","not","no","so","if","as","by","up","out","more","also","very",
    "just","than","then","when","where","who","how","all","any","both","each",
    "few","other","into","through","during","before","after","above","below",
    "between","such","while","about","against","well","been","its","their",
}

# ── Schemas ────────────────────────────────────────────────────────────
class EvalRequest(BaseModel):
    transcript: str
    job_description: str
    question: str = ""

# ── Helpers ────────────────────────────────────────────────────────────
def clean(text: str) -> str:
    text = text.lower()
    text = text.translate(str.maketrans("", "", string.punctuation))
    return re.sub(r"\s+", " ", text).strip()

def extract_keywords(text: str, top_n: int = 20) -> list[str]:
    """TF-IDF keyword extraction without NLTK."""
    sentences = [s.strip() for s in re.split(r"[.!?,;]", text) if len(s.strip()) > 4]
    if len(sentences) < 2:
        # Fallback: frequency-based
        words = [w for w in clean(text).split() if w not in STOP_WORDS and len(w) > 3]
        freq = {}
        for w in words:
            freq[w] = freq.get(w, 0) + 1
        return sorted(freq, key=freq.get, reverse=True)[:top_n]
    try:
        vec = TfidfVectorizer(ngram_range=(1, 2), stop_words="english", max_features=200)
        mat = vec.fit_transform(sentences)
        import numpy as np
        scores = mat.mean(axis=0).A1
        names = vec.get_feature_names_out()
        return [names[i] for i in scores.argsort()[::-1][:top_n]]
    except Exception:
        words = [w for w in clean(text).split() if w not in STOP_WORDS and len(w) > 3]
        return list(dict.fromkeys(words))[:top_n]

def cosine_sim(jd: str, answer: str) -> float:
    try:
        vec = TfidfVectorizer(stop_words="english")
        mat = vec.fit_transform([jd, answer])
        return float(cosine_similarity(mat[0:1], mat[1:2])[0][0])
    except Exception:
        return 0.0

def get_grade(score: float) -> str:
    if score >= 8.5: return "Excellent"
    if score >= 7.0: return "Good"
    if score >= 5.0: return "Average"
    return "Needs Work"

# ── Transcription ─────────────────────────────────────────────────────
@app.post("/transcribe")
async def transcribe(file: UploadFile = File(...)):
    """Accept a WAV/WebM audio upload and return transcribed text."""
    audio_bytes = await file.read()
    if not audio_bytes:
        raise HTTPException(status_code=400, detail="Empty audio file.")
    try:
        with sr.AudioFile(io.BytesIO(audio_bytes)) as src:
            _recognizer.adjust_for_ambient_noise(src, duration=0.3)
            audio_data = _recognizer.record(src)
        transcript = _recognizer.recognize_google(audio_data)
        return {"transcript": transcript, "success": True, "error": None}
    except sr.UnknownValueError:
        return {"transcript": "", "success": False, "error": "Could not understand audio. Please speak clearly."}
    except sr.RequestError as e:
        return {"transcript": "", "success": False, "error": f"Speech service unavailable: {e}"}
    except Exception as e:
        return {"transcript": "", "success": False, "error": f"Audio error: {e}"}


# ── Core pipeline ──────────────────────────────────────────────────────
@app.post("/evaluate")
def evaluate(req: EvalRequest):
    transcript = req.transcript.strip()
    jd = req.job_description.strip()

    if not transcript:
        return {"error": "Transcript is empty."}
    if len(jd) < 20:
        return {"error": "Job description too short."}

    # 1. Sentiment
    vs = _vader.polarity_scores(transcript)
    compound = vs["compound"]
    sentiment_label = "Positive" if compound >= 0.05 else ("Negative" if compound <= -0.05 else "Neutral")

    # 2. Keywords
    jd_keywords = extract_keywords(jd, top_n=20)
    answer_clean = clean(transcript)
    matched = [kw for kw in jd_keywords if re.search(r"\b" + re.escape(kw) + r"\b", answer_clean)]
    missing  = [kw for kw in jd_keywords if kw not in matched]
    match_pct = round(len(matched) / len(jd_keywords) * 100, 1) if jd_keywords else 0.0
    sim = round(cosine_sim(jd, transcript), 4)

    # 3. Score (weighted)
    word_count = len(transcript.split())
    kw_score   = min(10.0, match_pct / 10)
    sim_score  = min(10.0, sim * 10)
    sent_score = (7.0 + compound * 3) if sentiment_label == "Positive" else (6.0 if sentiment_label == "Neutral" else max(0.0, 4 + compound * 4))
    len_score  = 1.0 if word_count < 10 else min(10.0, 1 + (word_count - 10) / 8) if word_count < 90 else 10.0 if word_count <= 200 else max(6.0, 10 - (word_count - 200) / 50)
    spec_score = min(10.0, len({w for w in answer_clean.split() if len(w) > 5 and w not in STOP_WORDS}) * 0.4)

    final = round(kw_score*0.35 + sim_score*0.25 + sent_score*0.15 + len_score*0.15 + spec_score*0.10, 2)
    final = max(0.0, min(10.0, final))

    # 4. Feedback
    strengths, improvements, tips = [], [], []
    if match_pct >= 50:
        strengths.append(f"Good keyword coverage — {len(matched)} of {len(jd_keywords)} key terms mentioned.")
    if sentiment_label == "Positive":
        strengths.append("Confident and positive tone throughout your answer.")
    if word_count >= 60:
        strengths.append(f"Well-developed answer ({word_count} words) shows depth.")
    if sim >= 0.35:
        strengths.append("Your answer closely aligns with the job requirements.")
    if matched:
        strengths.append(f"Strong use of: {', '.join(matched[:4])}.")

    if match_pct < 40:
        improvements.append(f"Only {match_pct:.0f}% keyword match. Try incorporating more job-specific terms.")
    if word_count < 40:
        improvements.append(f"Answer too brief ({word_count} words). Aim for at least 60–80 words.")
    if missing:
        improvements.append(f"Missing key topics: {', '.join(missing[:5])}.")
    if sentiment_label == "Negative":
        improvements.append("Tone sounds negative. Reframe challenges as learning opportunities.")
    if sim < 0.2:
        improvements.append("Answer doesn't closely match the JD. Re-read it and tailor your response.")

    if final < 7:
        tips.append("Use the STAR method: Situation → Task → Action → Result.")
    tips.append("Quantify achievements where possible (e.g., 'reduced load time by 40%').")
    tips.append("End by connecting your experience directly to what the role needs.")

    if not strengths:
        strengths.append("You attempted the question — keep practising to improve!")

    return {
        "transcript": transcript,
        "word_count": word_count,
        "sentiment": {
            "label": sentiment_label,
            "compound": round(compound, 4),
            "positive": round(vs["pos"], 3),
            "neutral":  round(vs["neu"], 3),
            "negative": round(vs["neg"], 3),
        },
        "keywords": {
            "matched": matched,
            "missing": missing[:8],
            "match_pct": match_pct,
            "cosine_similarity": sim,
        },
        "score_breakdown": {
            "keyword":     round(kw_score, 2),
            "similarity":  round(sim_score, 2),
            "sentiment":   round(sent_score, 2),
            "length":      round(len_score, 2),
            "specificity": round(spec_score, 2),
        },
        "final_score": final,
        "grade": get_grade(final),
        "feedback": {
            "strengths":    strengths[:4],
            "improvements": improvements[:4],
            "tips":         tips[:3],
        },
    }

@app.get("/health")
def health():
    return {"status": "ok"}
