from pydantic import BaseModel
from typing import List, Optional


class TranscribeRequest(BaseModel):
    audio_base64: str  # Base64-encoded WAV audio


class TranscribeResponse(BaseModel):
    transcript: str
    success: bool
    error: Optional[str] = None


class EvaluateRequest(BaseModel):
    transcript: str
    job_description: str
    question: str


class SentimentResult(BaseModel):
    label: str          # Positive / Neutral / Negative
    compound: float     # -1.0 to 1.0
    positive: float
    neutral: float
    negative: float


class KeywordResult(BaseModel):
    matched_keywords: List[str]
    missing_keywords: List[str]
    match_percentage: float
    cosine_similarity: float


class ScoreBreakdown(BaseModel):
    keyword_score: float
    sentiment_score: float
    length_score: float
    similarity_score: float
    specificity_score: float
    total: float


class FeedbackResult(BaseModel):
    strengths: List[str]
    improvements: List[str]
    tips: List[str]


class EvaluationResult(BaseModel):
    transcript: str
    sentiment: SentimentResult
    keywords: KeywordResult
    score_breakdown: ScoreBreakdown
    final_score: float          # out of 10
    grade: str                  # Excellent / Good / Average / Needs Work
    feedback: FeedbackResult
