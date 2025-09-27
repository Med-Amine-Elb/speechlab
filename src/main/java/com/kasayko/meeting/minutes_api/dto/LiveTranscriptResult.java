package com.kasayko.meeting.minutes_api.dto;

public class LiveTranscriptResult {
    public String status;
    public String message;
    public String sessionId;
    public String transcript;
    public String summary;
    
    public LiveTranscriptResult() {}
    
    public LiveTranscriptResult(String status, String message, String sessionId) {
        this.status = status;
        this.message = message;
        this.sessionId = sessionId;
    }
    
    public LiveTranscriptResult(String status, String message, String sessionId, String transcript, String summary) {
        this.status = status;
        this.message = message;
        this.sessionId = sessionId;
        this.transcript = transcript;
        this.summary = summary;
    }
}
