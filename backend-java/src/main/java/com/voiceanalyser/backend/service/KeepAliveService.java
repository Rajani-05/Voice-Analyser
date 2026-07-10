package com.voiceanalyser.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * KeepAliveService — prevents Render free tier cold starts by pinging
 * the app's own /health endpoint every 10 minutes.
 * Uses System.getenv() directly for reliable env var reading on Render.
 */
@Service
public class KeepAliveService {

    private final RestTemplate restTemplate = new RestTemplate();

    private String getSelfUrl() {
        // Render injects RENDER_EXTERNAL_URL automatically on all web services
        String renderUrl = System.getenv("RENDER_EXTERNAL_URL");
        if (renderUrl != null && !renderUrl.isBlank()) {
            return renderUrl.trim();
        }
        return "http://localhost:8000";
    }

    /**
     * Fires every 10 minutes (600,000 ms). First ping after 2 min (app fully started).
     * Keeps Render free-tier container alive — avoids 30-60s cold start delays.
     */
    @Scheduled(fixedRate = 600_000, initialDelay = 120_000)
    public void keepAlive() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String url = getSelfUrl() + "/health";
        try {
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("[KeepAlive] " + timestamp + " → OK: " + response);
        } catch (Exception e) {
            // Never crash the app — just log
            System.out.println("[KeepAlive] " + timestamp + " → ping failed (non-fatal): " + e.getMessage());
        }
    }
}
