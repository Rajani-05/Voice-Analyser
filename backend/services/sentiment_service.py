"""Sentiment analysis service using VADER."""
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

_analyzer = SentimentIntensityAnalyzer()


def analyze_sentiment(text: str) -> dict:
    """
    Analyze the sentiment of the given text using VADER.

    Returns:
        dict with keys: label, compound, positive, neutral, negative
    """
    if not text or not text.strip():
        return {
            "label": "Neutral",
            "compound": 0.0,
            "positive": 0.0,
            "neutral": 1.0,
            "negative": 0.0,
        }

    scores = _analyzer.polarity_scores(text)
    compound = scores["compound"]

    # Classification thresholds
    if compound >= 0.05:
        label = "Positive"
    elif compound <= -0.05:
        label = "Negative"
    else:
        label = "Neutral"

    return {
        "label": label,
        "compound": round(compound, 4),
        "positive": round(scores["pos"], 4),
        "neutral": round(scores["neu"], 4),
        "negative": round(scores["neg"], 4),
    }
