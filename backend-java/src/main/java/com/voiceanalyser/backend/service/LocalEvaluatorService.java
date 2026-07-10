package com.voiceanalyser.backend.service;

import com.voiceanalyser.backend.dto.EvalResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LocalEvaluatorService {

    private static final Set<String> STOP_WORDS = Set.of(
        "i","me","my","we","our","you","your","he","she","it","they","them",
        "the","a","an","and","or","but","in","on","at","to","for","of","with",
        "is","are","was","were","be","been","have","has","had","do","does","did",
        "will","would","could","should","can","may","might","this","that","these",
        "those","am","not","no","so","if","as","by","up","out","more","also","very",
        "just","than","then","when","where","who","how","all","any","both","each",
        "few","other","into","through","during","before","after","above","below",
        "between","such","while","about","against","well","its","their"
    );

    private static final Set<String> POSITIVE_LEXICON = Set.of(
        "good", "great", "excellent", "lead", "led", "success", "successful", "successfully",
        "experience", "strong", "positive", "skills", "help", "helped", "achieved", "improvement",
        "improve", "improved", "solve", "solved", "team", "collaborate", "collaborated", "collaboration",
        "manage", "managed", "management", "passion", "passionate", "creative", "efficient", "efficiency",
        "learn", "learned", "growth", "grow", "opportunity", "confident", "confidence", "leadership"
    );

    private static final Set<String> NEGATIVE_LEXICON = Set.of(
        "bad", "worst", "poor", "fail", "failed", "failure", "error", "issue", "issues",
        "difficult", "difficulty", "hard", "problem", "problems", "unable", "cannot", "lost",
        "delay", "delayed", "conflict", "conflicts", "weak", "weakness", "scared", "fear"
    );

    public EvalResponse evaluateLocal(String transcript, String jd, String question) {
        String answerClean = clean(transcript);
        String jdClean = clean(jd);

        int wordCount = transcript.trim().isEmpty() ? 0 : transcript.trim().split("\\s+").length;

        // 1. Sentiment Heuristics
        double compound = 0.0;
        int posCount = 0;
        int negCount = 0;
        String[] words = answerClean.split("\\s+");
        for (String w : words) {
            if (POSITIVE_LEXICON.contains(w)) posCount++;
            else if (NEGATIVE_LEXICON.contains(w)) negCount++;
        }
        if (posCount + negCount > 0) {
            compound = (double) (posCount - negCount) / (posCount + negCount);
        }
        String sentimentLabel = "Neutral";
        if (compound >= 0.05) sentimentLabel = "Positive";
        else if (compound <= -0.05) sentimentLabel = "Negative";

        double totalLex = posCount + negCount + 10.0; // padding for neutral
        double positiveRatio = Math.round((posCount / totalLex) * 1000.0) / 1000.0;
        double negativeRatio = Math.round((negCount / totalLex) * 1000.0) / 1000.0;
        double neutralRatio = Math.round((1.0 - positiveRatio - negativeRatio) * 1000.0) / 1000.0;

        double sentScore = 6.0; // Neutral
        if ("Positive".equals(sentimentLabel)) {
            sentScore = 7.0 + compound * 3.0;
        } else if ("Negative".equals(sentimentLabel)) {
            sentScore = Math.max(0.0, 4.0 + compound * 4.0);
        }
        sentScore = Math.round(sentScore * 100.0) / 100.0;

        // 2. Keywords
        List<String> jdKeywords = extractKeywords(jdClean, 20);
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String kw : jdKeywords) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(kw) + "\\b");
            Matcher matcher = pattern.matcher(answerClean);
            if (matcher.find()) {
                matched.add(kw);
            } else {
                missing.add(kw);
            }
        }
        double matchPct = jdKeywords.isEmpty() ? 0.0 : Math.round(((double) matched.size() / jdKeywords.size() * 100.0) * 10.0) / 10.0;
        double cosineSimilarity = Math.round(calculateCosineSimilarity(jdClean, answerClean) * 10000.0) / 10000.0;

        double kwScore = Math.min(10.0, matchPct / 10.0);
        double simScore = Math.min(10.0, cosineSimilarity * 10.0);

        // 3. Length Score
        double lenScore;
        if (wordCount < 10) {
            lenScore = 1.0;
        } else if (wordCount < 90) {
            lenScore = 1.0 + (wordCount - 10) / 8.0;
        } else if (wordCount <= 200) {
            lenScore = 10.0;
        } else {
            lenScore = Math.max(6.0, 10.0 - (wordCount - 200) / 50.0);
        }
        lenScore = Math.round(lenScore * 100.0) / 100.0;

        // 4. Specificity Score
        Set<String> uniqueSpecificWords = new HashSet<>();
        for (String w : words) {
            if (w.length() > 5 && !STOP_WORDS.contains(w)) {
                uniqueSpecificWords.add(w);
            }
        }
        double specScore = Math.min(10.0, uniqueSpecificWords.size() * 0.4);
        specScore = Math.round(specScore * 100.0) / 100.0;

        // 5. Final Score
        double finalScore = kwScore * 0.35 + simScore * 0.25 + sentScore * 0.15 + lenScore * 0.15 + specScore * 0.10;
        finalScore = Math.max(0.0, Math.min(10.0, Math.round(finalScore * 100.0) / 100.0));

        // 6. Grade
        String grade = "Needs Work";
        if (finalScore >= 8.5) grade = "Excellent";
        else if (finalScore >= 7.0) grade = "Good";
        else if (finalScore >= 5.0) grade = "Average";

        // 7. Feedback Generation
        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();
        List<String> tips = new ArrayList<>();

        if (matchPct >= 50.0) {
            strengths.add("Good keyword coverage — " + matched.size() + " of " + jdKeywords.size() + " key terms mentioned.");
        }
        if ("Positive".equals(sentimentLabel)) {
            strengths.add("Confident and positive tone throughout your answer.");
        }
        if (wordCount >= 60) {
            strengths.add("Well-developed answer (" + wordCount + " words) shows depth.");
        }
        if (cosineSimilarity >= 0.35) {
            strengths.add("Your answer closely aligns with the job requirements.");
        }
        if (!matched.isEmpty()) {
            int limit = Math.min(4, matched.size());
            strengths.add("Strong use of: " + String.join(", ", matched.subList(0, limit)) + ".");
        }
        if (strengths.isEmpty()) {
            strengths.add("You attempted the question — keep practising to improve!");
        }

        if (matchPct < 40.0) {
            improvements.add("Only " + Math.round(matchPct) + "% keyword match. Try incorporating more job-specific terms.");
        }
        if (wordCount < 40) {
            improvements.add("Answer too brief (" + wordCount + " words). Aim for at least 60–80 words.");
        }
        if (!missing.isEmpty()) {
            int limit = Math.min(5, missing.size());
            improvements.add("Missing key topics: " + String.join(", ", missing.subList(0, limit)) + ".");
        }
        if ("Negative".equals(sentimentLabel)) {
            improvements.add("Tone sounds negative. Reframe challenges as learning opportunities.");
        }
        if (cosineSimilarity < 0.2) {
            improvements.add("Answer doesn't closely match the JD. Re-read it and tailor your response.");
        }

        if (finalScore < 7.0) {
            tips.add("Use the STAR method: Situation → Task → Action → Result.");
        }
        tips.add("Quantify achievements where possible (e.g., 'reduced load time by 40%').");
        tips.add("End by connecting your experience directly to what the role needs.");

        // Build Response
        EvalResponse res = new EvalResponse();
        res.setTranscript(transcript);
        res.setWord_count(wordCount);

        EvalResponse.Sentiment sent = new EvalResponse.Sentiment();
        sent.setLabel(sentimentLabel);
        sent.setCompound(Math.round(compound * 10000.0) / 10000.0);
        sent.setPositive(positiveRatio);
        sent.setNeutral(neutralRatio);
        sent.setNegative(negativeRatio);
        res.setSentiment(sent);

        EvalResponse.Keywords kws = new EvalResponse.Keywords();
        kws.setMatched(matched);
        kws.setMissing(missing.subList(0, Math.min(8, missing.size())));
        kws.setMatch_pct(matchPct);
        kws.setCosine_similarity(cosineSimilarity);
        res.setKeywords(kws);

        EvalResponse.ScoreBreakdown breakdown = new EvalResponse.ScoreBreakdown();
        breakdown.setKeyword(Math.round(kwScore * 100.0) / 100.0);
        breakdown.setSimilarity(Math.round(simScore * 100.0) / 100.0);
        breakdown.setSentiment(sentScore);
        breakdown.setLength(lenScore);
        breakdown.setSpecificity(specScore);
        res.setScore_breakdown(breakdown);

        res.setFinal_score(finalScore);
        res.setGrade(grade);

        EvalResponse.Feedback feedback = new EvalResponse.Feedback();
        feedback.setStrengths(strengths.subList(0, Math.min(4, strengths.size())));
        feedback.setImprovements(improvements.subList(0, Math.min(4, improvements.size())));
        feedback.setTips(tips.subList(0, Math.min(3, tips.size())));
        res.setFeedback(feedback);

        return res;
    }

    private String clean(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[\\p{Punct}]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<String> extractKeywords(String cleanText, int topN) {
        String[] words = cleanText.split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            if (w.length() > 3 && !STOP_WORDS.contains(w)) {
                freq.put(w, freq.getOrDefault(w, 0) + 1);
            }
        }
        return freq.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateCosineSimilarity(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        Map<String, Integer> freq1 = new HashMap<>();
        Map<String, Integer> freq2 = new HashMap<>();
        Set<String> vocab = new HashSet<>();

        for (String w : words1) {
            if (w.length() > 3 && !STOP_WORDS.contains(w)) {
                freq1.put(w, freq1.getOrDefault(w, 0) + 1);
                vocab.add(w);
            }
        }
        for (String w : words2) {
            if (w.length() > 3 && !STOP_WORDS.contains(w)) {
                freq2.put(w, freq2.getOrDefault(w, 0) + 1);
                vocab.add(w);
            }
        }

        if (vocab.isEmpty()) return 0.0;

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String w : vocab) {
            int v1 = freq1.getOrDefault(w, 0);
            int v2 = freq2.getOrDefault(w, 0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
