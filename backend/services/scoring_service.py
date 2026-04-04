"""Composite scoring and feedback generation."""
from typing import List

WEIGHTS = {"keyword": 0.35, "similarity": 0.25, "sentiment": 0.15, "length": 0.15, "specificity": 0.10}
IDEAL_MIN, IDEAL_MAX = 50, 200


def _length_score(n: int) -> float:
    if n < 10:       return 1.0
    if n < IDEAL_MIN: return 1.0 + (n - 10) / (IDEAL_MIN - 10) * 6.0
    if n <= IDEAL_MAX: return 10.0
    return max(6.0, 10.0 - (n - IDEAL_MAX) / 50)


def _sentiment_score(compound: float, label: str) -> float:
    if label == "Positive": return min(10.0, 6.0 + compound * 4.0)
    if label == "Neutral":  return 6.0
    return max(0.0, 4.0 + compound * 4.0)


def calculate_score(
    transcript: str,
    match_percentage: float,
    cosine_similarity: float,
    sentiment_compound: float,
    sentiment_label: str,
    matched_keywords: List[str],
) -> dict:
    word_count = len(transcript.split())
    kw_score   = min(10.0, match_percentage / 10)
    sim_score  = min(10.0, cosine_similarity * 10)
    sent_score = _sentiment_score(sentiment_compound, sentiment_label)
    len_score  = _length_score(word_count)
    # Unique long words (>= 6 chars) as a specificity proxy
    long_words = {w for w in transcript.lower().split() if len(w) >= 6}
    spec_score = min(10.0, len(long_words) * 0.3 + min(5.0, len(matched_keywords) * 0.5))

    total = round(min(10.0, max(0.0,
        kw_score   * WEIGHTS["keyword"]
        + sim_score  * WEIGHTS["similarity"]
        + sent_score * WEIGHTS["sentiment"]
        + len_score  * WEIGHTS["length"]
        + spec_score * WEIGHTS["specificity"]
    )), 2)

    return {
        "keyword_score":     round(kw_score, 2),
        "similarity_score":  round(sim_score, 2),
        "sentiment_score":   round(sent_score, 2),
        "length_score":      round(len_score, 2),
        "specificity_score": round(spec_score, 2),
        "total": total,
    }


def generate_feedback(
    score: float,
    word_count: int,
    matched_keywords: List[str],
    missing_keywords: List[str],
    sentiment_label: str,
    match_percentage: float,
    cosine_similarity: float,
) -> dict:
    strengths, improvements, tips = [], [], []

    if match_percentage >= 60:
        strengths.append(f"Strong keyword coverage — {len(matched_keywords)} key terms from the JD mentioned.")
    if sentiment_label == "Positive":
        strengths.append("Confident and positive tone throughout.")
    if word_count >= IDEAL_MIN:
        strengths.append(f"Well-developed answer ({word_count} words).")
    if cosine_similarity >= 0.4:
        strengths.append("Answer closely aligns with the job description.")
    if matched_keywords:
        strengths.append(f"Good use of: {', '.join(matched_keywords[:4])}.")

    if match_percentage < 40:
        improvements.append(f"Only {match_percentage:.0f}% keyword match — add more job-specific terms.")
    if word_count < IDEAL_MIN:
        improvements.append(f"Too brief ({word_count} words). Aim for {IDEAL_MIN}+ words.")
    if cosine_similarity < 0.2:
        improvements.append("Re-read the JD and tailor your answer more closely.")
    if sentiment_label == "Negative":
        improvements.append("Reframe negatives as learning opportunities.")
    if missing_keywords:
        improvements.append(f"Missing topics: {', '.join(missing_keywords[:5])}.")
    if word_count > IDEAL_MAX:
        improvements.append(f"Too long ({word_count} words). Aim for under {IDEAL_MAX}.")

    if score < 5:
        tips.append("Use the STAR method: Situation → Task → Action → Result.")
    if score < 7:
        tips.append("Open with a headline statement, then back it with specific examples.")
    tips.append("Quantify achievements (e.g., 'reduced load time by 40%').")
    tips.append("End by connecting your experience to what the role needs.")

    if not strengths:
        strengths.append("You attempted the question — keep practising!")

    return {"strengths": strengths, "improvements": improvements, "tips": tips[:3]}


def get_grade(score: float) -> str:
    if score >= 8.5: return "Excellent"
    if score >= 7.0: return "Good"
    if score >= 5.0: return "Average"
    return "Needs Work"
