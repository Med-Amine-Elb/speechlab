package com.kasayko.meeting.minutes_api.controller;

import com.kasayko.meeting.minutes_api.dto.LiveTranscriptResult;
import com.kasayko.meeting.minutes_api.service.LiveAudioService;
import com.kasayko.meeting.minutes_api.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
public class LiveRecordingController {
    
    private final LiveAudioService liveAudioService;
    private final JwtService jwtService;
    
    public LiveRecordingController(LiveAudioService liveAudioService, JwtService jwtService) {
        this.liveAudioService = liveAudioService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/start")
    public ResponseEntity<LiveTranscriptResult> startRecording(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmailFromToken(authHeader);
            String sessionId = liveAudioService.startSession(email);
            return ResponseEntity.ok(new LiveTranscriptResult("started", "Recording started", sessionId));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new LiveTranscriptResult("error", "Unauthorized: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/chunk")
    public ResponseEntity<LiveTranscriptResult> processAudioChunk(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String email = extractEmailFromToken(authHeader);
            String audioData = request.get("audioData");
            
            if (audioData == null || audioData.isEmpty()) {
                return ResponseEntity.badRequest().body(new LiveTranscriptResult("error", "Audio data is required", null));
            }
            
            byte[] audioBytes = Base64.getDecoder().decode(audioData);
            LiveTranscriptResult result = liveAudioService.processAudioChunk(audioBytes, email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new LiveTranscriptResult("error", "Failed to process audio: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/stop")
    public ResponseEntity<LiveTranscriptResult> stopRecording(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmailFromToken(authHeader);
            LiveTranscriptResult result = liveAudioService.stopSession(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new LiveTranscriptResult("error", "Failed to stop recording: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/upload-chunk")
    public ResponseEntity<LiveTranscriptResult> uploadAudioChunk(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            String email = extractEmailFromToken(authHeader);
            byte[] audioBytes = file.getBytes();
            LiveTranscriptResult result = liveAudioService.processAudioChunk(audioBytes, email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new LiveTranscriptResult("error", "Failed to process audio: " + e.getMessage(), null));
        }
    }
    
    private String extractEmailFromToken(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        return jwtService.extractSubject(token);
    }
}
