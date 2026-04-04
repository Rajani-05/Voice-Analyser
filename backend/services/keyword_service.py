"""Keyword extraction and matching — no NLTK, fast static stopwords."""
import re
import string
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# Static English stopwords — no NLTK download needed
STOP_WORDS = {
    "i","me","my","we","our","you","your","he","she","it","they","them",
    "the","a","an","and","or","but","in","on","at","to","for","of","with",
    "is","are","was","were","be","been","have","has","had","do","does","did",
    "will","would","could","should","can","may","might","this","that","these",
    "those","am","not","no","so","if","as","by","up","out","more","also","very",
    "just","than","then","when","where","who","how","all","any","both","each",
    "few","other","into","through","during","before","after","above","below",
    "between","such","while","about","against","well","its","their",
}

_PUNCT_TABLE = str.maketrans("", "", string.punctuation)


def _clean(text: str) -> str:
    return re.sub(r"\s+", " ", text.lower().translate(_PUNCT_TABLE)).strip()


def _extract_jd_keywords(jd: str, top_n: int = 20) -> list[str]:
    sentences = [s.strip() for s in re.split(r"[.!?,;]", jd) if len(s.strip()) > 5]
    if len(sentences) >= 2:
        try:
            vec = TfidfVectorizer(ngram_range=(1, 2), stop_words="english", max_features=200)
            mat = vec.fit_transform(sentences)
            scores = mat.mean(axis=0).A1
            names = vec.get_feature_names_out()
            return [names[i] for i in scores.argsort()[::-1][:top_n]]
        except Exception:
            pass
    # Fallback: frequency-based
    words = [w for w in _clean(jd).split() if w not in STOP_WORDS and len(w) > 3]
    freq: dict[str, int] = {}
    for w in words:
        freq[w] = freq.get(w, 0) + 1
    return sorted(freq, key=freq.get, reverse=True)[:top_n]  # type: ignore[arg-type]


def compute_keyword_match(transcript: str, job_description: str, question: str = "") -> dict:
    """Compare transcript against the JD. Returns matched/missing keywords + cosine similarity."""
    jd_keywords = _extract_jd_keywords(job_description)
    answer_clean = _clean(transcript)

    matched = [kw for kw in jd_keywords if re.search(r"\b" + re.escape(kw) + r"\b", answer_clean)]
    missing  = [kw for kw in jd_keywords if kw not in matched]
    match_pct = round(len(matched) / len(jd_keywords) * 100, 2) if jd_keywords else 0.0

    # Cosine similarity between full JD and transcript
    try:
        vec = TfidfVectorizer(ngram_range=(1, 2), stop_words="english")
        mat = vec.fit_transform([job_description, transcript])
        sim = float(cosine_similarity(mat[0:1], mat[1:2])[0][0])
    except Exception:
        jd_set  = set(jd_keywords)
        tr_set  = set(answer_clean.split())
        union   = jd_set | tr_set
        sim     = len(jd_set & tr_set) / len(union) if union else 0.0

    return {
        "matched_keywords": matched,
        "missing_keywords": missing[:10],
        "match_percentage": match_pct,
        "cosine_similarity": round(sim, 4),
    }
