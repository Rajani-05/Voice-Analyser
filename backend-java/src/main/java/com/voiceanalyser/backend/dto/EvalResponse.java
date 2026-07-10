package com.voiceanalyser.backend.dto;

import java.util.List;

public class EvalResponse {
    private String transcript;
    private int word_count;
    private Sentiment sentiment;
    private Keywords keywords;
    private ScoreBreakdown score_breakdown;
    private double final_score;
    private String grade;
    private Feedback feedback;

    // Getters and Setters
    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public int getWord_count() {
        return word_count;
    }

    public void setWord_count(int word_count) {
        this.word_count = word_count;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public Keywords getKeywords() {
        return keywords;
    }

    public void setKeywords(Keywords keywords) {
        this.keywords = keywords;
    }

    public ScoreBreakdown getScore_breakdown() {
        return score_breakdown;
    }

    public void setScore_breakdown(ScoreBreakdown score_breakdown) {
        this.score_breakdown = score_breakdown;
    }

    public double getFinal_score() {
        return final_score;
    }

    public void setFinal_score(double final_score) {
        this.final_score = final_score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public static class Sentiment {
        private String label;
        private double compound;
        private double positive;
        private double neutral;
        private double negative;

        // Getters and Setters
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getCompound() {
            return compound;
        }

        public void setCompound(double compound) {
            this.compound = compound;
        }

        public double getPositive() {
            return positive;
        }

        public void setPositive(double positive) {
            this.positive = positive;
        }

        public double getNeutral() {
            return neutral;
        }

        public void setNeutral(double neutral) {
            this.neutral = neutral;
        }

        public double getNegative() {
            return negative;
        }

        public void setNegative(double negative) {
            this.negative = negative;
        }
    }

    public static class Keywords {
        private List<String> matched;
        private List<String> missing;
        private double match_pct;
        private double cosine_similarity;

        // Getters and Setters
        public List<String> getMatched() {
            return matched;
        }

        public void setMatched(List<String> matched) {
            this.matched = matched;
        }

        public List<String> getMissing() {
            return missing;
        }

        public void setMissing(List<String> missing) {
            this.missing = missing;
        }

        public double getMatch_pct() {
            return match_pct;
        }

        public void setMatch_pct(double match_pct) {
            this.match_pct = match_pct;
        }

        public double getCosine_similarity() {
            return cosine_similarity;
        }

        public void setCosine_similarity(double cosine_similarity) {
            this.cosine_similarity = cosine_similarity;
        }
    }

    public static class ScoreBreakdown {
        private double keyword;
        private double similarity;
        private double sentiment;
        private double length;
        private double specificity;

        // Getters and Setters
        public double getKeyword() {
            return keyword;
        }

        public void setKeyword(double keyword) {
            this.keyword = keyword;
        }

        public double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }

        public double getSentiment() {
            return sentiment;
        }

        public void setSentiment(double sentiment) {
            this.sentiment = sentiment;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public double getSpecificity() {
            return specificity;
        }

        public void setSpecificity(double specificity) {
            this.specificity = specificity;
        }
    }

    public static class Feedback {
        private List<String> strengths;
        private List<String> improvements;
        private List<String> tips;

        // Getters and Setters
        public List<String> getStrengths() {
            return strengths;
        }

        public void setStrengths(List<String> strengths) {
            this.strengths = strengths;
        }

        public List<String> getImprovements() {
            return improvements;
        }

        public void setImprovements(List<String> improvements) {
            this.improvements = improvements;
        }

        public List<String> getTips() {
            return tips;
        }

        public void setTips(List<String> tips) {
            this.tips = tips;
        }
    }
}
