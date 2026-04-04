"""Evaluation route — POST /evaluate"""
import logging
from fastapi import APIRouter, HTTPException

from models.schemas import EvaluateRequest, EvaluationResult
from services.sentiment_service import analyze_sentiment
from services.keyword_service import compute_keyword_match
from services.scoring_service import calculate_score, generate_feedback, get_grade

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/evaluate", response_model=EvaluationResult)
async def evaluate_answer(request: EvaluateRequest):
    """
    Full NLP evaluation pipeline:
    1. Sentiment analysis
    2. Keyword matching + cosine similarity
    3. Composite scoring
    4. Feedback generation
    """
    transcript = request.transcript.strip()
    if not transcript:
        raise HTTPException(status_code=400, detail="Transcript cannot be empty.")

    if len(request.job_description.strip()) < 20:
        raise HTTPException(
            status_code=400,
            detail="Job description is too short. Please provide a detailed JD.",
        )

    try:
        # 1. Sentiment
        sentiment = analyze_sentiment(transcript)

        # 2. Keyword matching
        keywords = compute_keyword_match(
            transcript=transcript,
            job_description=request.job_description,
            question=request.question,
        )

        # 3. Score
        word_count = len(transcript.split())
        score_breakdown = calculate_score(
            transcript=transcript,
            match_percentage=keywords["match_percentage"],
            cosine_similarity=keywords["cosine_similarity"],
            sentiment_compound=sentiment["compound"],
            sentiment_label=sentiment["label"],
            matched_keywords=keywords["matched_keywords"],
        )

        # 4. Feedback
        feedback = generate_feedback(
            score=score_breakdown["total"],
            word_count=word_count,
            matched_keywords=keywords["matched_keywords"],
            missing_keywords=keywords["missing_keywords"],
            sentiment_label=sentiment["label"],
            match_percentage=keywords["match_percentage"],
            cosine_similarity=keywords["cosine_similarity"],
        )

        grade = get_grade(score_breakdown["total"])

        return EvaluationResult(
            transcript=transcript,
            sentiment=sentiment,
            keywords=keywords,
            score_breakdown=score_breakdown,
            final_score=score_breakdown["total"],
            grade=grade,
            feedback=feedback,
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Evaluation pipeline error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Evaluation failed: {str(e)}")
