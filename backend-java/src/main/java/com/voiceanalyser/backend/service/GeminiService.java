package com.voiceanalyser.backend.service;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.voiceanalyser.backend.dto.EvalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKeyConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private String getApiKey() {
        if (apiKeyConfig != null && !apiKeyConfig.trim().isEmpty()) {
            return apiKeyConfig.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String googleKey = System.getenv("GOOGLE_API_KEY");
        if (googleKey != null && !googleKey.trim().isEmpty()) {
            return googleKey.trim();
        }
        return "";
    }

    public boolean isApiKeyAvailable() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }

    public String transcribe(byte[] audioBytes, String mimeType) throws Exception {
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String base64Data = Base64.getEncoder().encodeToString(audioBytes);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct request payload
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mimeType", mimeType);
        inlineData.put("data", base64Data);

        Map<String, Object> inlineDataPart = new HashMap<>();
        inlineDataPart.put("inlineData", inlineData);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", "Please transcribe this audio recording accurately. Return only the transcription text, nothing else. Do not add any conversational remarks or metadata.");

        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("parts", List.of(inlineDataPart, textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentPart));

        String requestJson = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String transcript = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();
                return transcript.trim();
            }
        }
        throw new RuntimeException("Failed to transcribe audio via Gemini API: " + response.getStatusCode());
    }

    public EvalResponse evaluate(String transcript, String jd, String question) throws Exception {
        String apiKey = getApiKey();
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // System Instruction
        String systemInstruction = "You are an expert AI Interview Evaluator. Your task is to evaluate the user's interview answer (transcript) based on its relevance to the job description (job_description) and the interview question (question).\n"
                + "\n"
                + "Calculate a score from 0.0 to 10.0 for each of the following components:\n"
                + "- keyword: score based on keyword match percentage.\n"
                + "- similarity: score based on semantic relevance to the job description.\n"
                + "- sentiment: score (0.0 to 10.0) where positive/enthusiastic tone is high (8-10), neutral is moderate (6-7), negative is lower.\n"
                + "- length: score based on word count (60-200 words is 10.0; under 40 words is low).\n"
                + "- specificity: score based on depth and use of meaningful terms.\n"
                + "\n"
                + "Calculate the final_score as a weighted average:\n"
                + "final_score = keyword * 0.35 + similarity * 0.25 + sentiment * 0.15 + length * 0.15 + specificity * 0.10.\n"
                + "Ensure final_score is between 0.0 and 10.0, rounded to 2 decimal places.\n"
                + "\n"
                + "Assign a grade based on final_score:\n"
                + "- >= 8.5: \"Excellent\"\n"
                + "- >= 7.0: \"Good\"\n"
                + "- >= 5.0: \"Average\"\n"
                + "- < 5.0: \"Needs Work\"\n"
                + "\n"
                + "Provide structured feedback:\n"
                + "- strengths: list 1-4 specific strengths of the answer.\n"
                + "- improvements: list 1-4 specific ways to improve.\n"
                + "- tips: list 1-3 general interview tips.\n"
                + "\n"
                + "You MUST return the output in the following JSON format (do not add any markdown formatting or extra text outside the JSON block):\n"
                + "{\n"
                + "  \"transcript\": \"...\",\n"
                + "  \"word_count\": 123,\n"
                + "  \"sentiment\": {\n"
                + "    \"label\": \"Positive\" | \"Neutral\" | \"Negative\",\n"
                + "    \"compound\": 0.85,\n"
                + "    \"positive\": 0.3,\n"
                + "    \"neutral\": 0.7,\n"
                + "    \"negative\": 0.0\n"
                + "  },\n"
                + "  \"keywords\": {\n"
                + "    \"matched\": [\"python\", \"react\"],\n"
                + "    \"missing\": [\"kubernetes\", \"docker\"],\n"
                + "    \"match_pct\": 50.0,\n"
                + "    \"cosine_similarity\": 0.45\n"
                + "  },\n"
                + "  \"score_breakdown\": {\n"
                + "    \"keyword\": 8.0,\n"
                + "    \"similarity\": 7.5,\n"
                + "    \"sentiment\": 9.0,\n"
                + "    \"length\": 10.0,\n"
                + "    \"specificity\": 8.5\n"
                + "  },\n"
                + "  \"final_score\": 8.25,\n"
                + "  \"grade\": \"Good\",\n"
                + "  \"feedback\": {\n"
                + "    \"strengths\": [\"...\", \"...\"],\n"
                + "    \"improvements\": [\"...\", \"...\"],\n"
                + "    \"tips\": [\"...\", \"...\"]\n"
                + "  }\n"
                + "}";

        // Prompt
        String userPrompt = "job_description: " + jd + "\n\n"
                + "question: " + question + "\n\n"
                + "transcript: " + transcript;

        // Construct request payload
        Map<String, Object> sysParts = new HashMap<>();
        sysParts.put("text", systemInstruction);

        Map<String, Object> sysInstructionMap = new HashMap<>();
        sysInstructionMap.put("parts", List.of(sysParts));

        Map<String, Object> userParts = new HashMap<>();
        userParts.put("text", userPrompt);

        Map<String, Object> contentsMap = new HashMap<>();
        contentsMap.put("parts", List.of(userParts));

        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("responseMimeType", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentsMap));
        requestBody.put("systemInstruction", sysInstructionMap);
        requestBody.put("generationConfig", genConfig);

        String requestJson = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                String jsonResponseText = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText();
                return objectMapper.readValue(jsonResponseText, EvalResponse.class);
            }
        }
        throw new RuntimeException("Failed to evaluate transcript via Gemini API: " + response.getStatusCode());
    }
}
