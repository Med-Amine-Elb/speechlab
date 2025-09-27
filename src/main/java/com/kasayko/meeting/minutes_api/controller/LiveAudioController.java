package com.kasayko.meeting.minutes_api.controller;

import com.kasayko.meeting.minutes_api.dto.LiveTranscriptResult;
import com.kasayko.meeting.minutes_api.service.LiveAudioService;
import com.kasayko.meeting.minutes_api.security.JwtService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Controller;

import java.util.Base64;

@Controller
public class LiveAudioController {
    
    private final LiveAudioService liveAudioService;
    private final JwtService jwtService;
    
    public LiveAudioController(LiveAudioService liveAudioService, JwtService jwtService) {
        this.liveAudioService = liveAudioService;
        this.jwtService = jwtService;
    }
    
    @MessageMapping("/audio/start")
    @SendTo("/topic/transcript")
    public LiveTranscriptResult startRecording(@Header("Authorization") String authHeader) {
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) : authHeader;
            String email = jwtService.extractSubject(token);
            
            String sessionId = liveAudioService.startSession(email);
            return new LiveTranscriptResult("started", "Recording started", sessionId);
        } catch (Exception e) {
            return new LiveTranscriptResult("error", "Unauthorized: " + e.getMessage(), null);
        }
    }
    
    @MessageMapping("/audio/chunk")
    @SendTo("/topic/transcript")
    public LiveTranscriptResult processAudioChunk(String audioData, @Header("Authorization") String authHeader) {
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) : authHeader;
            String email = jwtService.extractSubject(token);
            
            byte[] audioBytes = Base64.getDecoder().decode(audioData);
            return liveAudioService.processAudioChunk(audioBytes, email);
        } catch (Exception e) {
            return new LiveTranscriptResult("error", "Failed to process audio: " + e.getMessage(), null);
        }
    }
    
    @MessageMapping("/audio/stop")
    @SendTo("/topic/transcript")
    public LiveTranscriptResult stopRecording(@Header("Authorization") String authHeader) {
        try {
            String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) : authHeader;
            String email = jwtService.extractSubject(token);
            
            return liveAudioService.stopSession(email);
        } catch (Exception e) {
            return new LiveTranscriptResult("error", "Unauthorized: " + e.getMessage(), null);
        }
    }
}
