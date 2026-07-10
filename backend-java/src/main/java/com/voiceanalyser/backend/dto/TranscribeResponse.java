package com.voiceanalyser.backend.dto;

public class TranscribeResponse {
    private String transcript;
    private boolean success;
    private String error;

    public TranscribeResponse() {}

    public TranscribeResponse(String transcript, boolean success, String error) {
        this.transcript = transcript;
        this.success = success;
        this.error = error;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
