package com.voiceanalyser.backend.controller;

import com.voiceanalyser.backend.dto.EvalRequest;
import com.voiceanalyser.backend.dto.EvalResponse;
import com.voiceanalyser.backend.dto.TranscribeResponse;
import com.voiceanalyser.backend.service.GeminiService;
import com.voiceanalyser.backend.service.LocalEvaluatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class InterviewController {

    private final GeminiService geminiService;
    private final LocalEvaluatorService localEvaluatorService;

    @Autowired
    public InterviewController(GeminiService geminiService, LocalEvaluatorService localEvaluatorService) {
        this.geminiService = geminiService;
        this.localEvaluatorService = localEvaluatorService;
    }

    @GetMapping("/")
    public ResponseEntity<?> serveIndex() {
        File distIndex = new File("../frontend/dist/index.html");
        if (distIndex.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(distIndex));
        }
        File indexFile = new File("../frontend/index.html");
        if (indexFile.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(indexFile));
        }
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("docs", "/api/docs");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/transcribe")
    public ResponseEntity<TranscribeResponse> transcribe(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new TranscribeResponse("", false, "Empty audio file."));
        }

        try {
            if (!geminiService.isApiKeyAvailable()) {
                return ResponseEntity.ok(new TranscribeResponse("", false, "Gemini API key is not configured. Please set the GEMINI_API_KEY environment variable."));
            }

            byte[] bytes = file.getBytes();
            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "audio/webm";
            }

            String transcript = geminiService.transcribe(bytes, contentType);
            return ResponseEntity.ok(new TranscribeResponse(transcript, true, null));

        } catch (Exception e) {
            return ResponseEntity.ok(new TranscribeResponse("", false, "Transcription error: " + e.getMessage()));
        }
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody EvalRequest request) {
        String transcript = request.getTranscript() != null ? request.getTranscript().trim() : "";
        String jd = request.getJob_description() != null ? request.getJob_description().trim() : "";
        String question = request.getQuestion() != null ? request.getQuestion().trim() : "";

        if (transcript.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Transcript is empty.");
            return ResponseEntity.ok(error);
        }
        if (jd.length() < 20) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Job description too short.");
            return ResponseEntity.ok(error);
        }

        try {
            if (geminiService.isApiKeyAvailable()) {
                EvalResponse response = geminiService.evaluate(transcript, jd, question);
                return ResponseEntity.ok(response);
            } else {
                // Fallback to local evaluation
                EvalResponse response = localEvaluatorService.evaluateLocal(transcript, jd, question);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            // If Gemini evaluation fails, log and fallback to local evaluation
            System.err.println("Gemini evaluation failed, falling back to local: " + e.getMessage());
            EvalResponse response = localEvaluatorService.evaluateLocal(transcript, jd, question);
            return ResponseEntity.ok(response);
        }
    }
}
