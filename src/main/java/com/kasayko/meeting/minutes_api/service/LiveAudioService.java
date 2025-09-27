package com.kasayko.meeting.minutes_api.service;

import com.kasayko.meeting.minutes_api.client.DeepgramClient;
import com.kasayko.meeting.minutes_api.client.GeminiClient;
import com.kasayko.meeting.minutes_api.dto.LiveTranscriptResult;
import com.kasayko.meeting.minutes_api.dto.SummaryResult;
import com.kasayko.meeting.minutes_api.dto.TranscriptResult;
import com.kasayko.meeting.minutes_api.model.MeetingRecord;
import com.kasayko.meeting.minutes_api.repo.MeetingRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Simple MultipartFile implementation for in-memory audio data
class SimpleMultipartFile implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;
    
    public SimpleMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }
    
    @Override
    public String getName() { return name; }
    
    @Override
    public String getOriginalFilename() { return originalFilename; }
    
    @Override
    public String getContentType() { return contentType; }
    
    @Override
    public boolean isEmpty() { return content == null || content.length == 0; }
    
    @Override
    public long getSize() { return content != null ? content.length : 0; }
    
    @Override
    public byte[] getBytes() throws IOException { return content; }
    
    @Override
    public java.io.InputStream getInputStream() throws IOException {
        return new java.io.ByteArrayInputStream(content);
    }
    
    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        java.nio.file.Files.write(dest.toPath(), content);
    }
}

@Service
public class LiveAudioService {
    
    private final DeepgramClient deepgramClient;
    private final GeminiClient geminiClient;
    private final MeetingRecordRepository repository;
    
    // Store active recording sessions
    private final Map<String, ByteArrayOutputStream> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionOwners = new ConcurrentHashMap<>();
    
    public LiveAudioService(DeepgramClient deepgramClient, GeminiClient geminiClient, MeetingRecordRepository repository) {
        this.deepgramClient = deepgramClient;
        this.geminiClient = geminiClient;
        this.repository = repository;
    }
    
    public String startSession(String userEmail) {
        String sessionId = "session_" + System.currentTimeMillis() + "_" + userEmail.hashCode();
        activeSessions.put(sessionId, new ByteArrayOutputStream());
        sessionOwners.put(sessionId, userEmail);
        return sessionId;
    }
    
    public LiveTranscriptResult processAudioChunk(byte[] audioData, String userEmail) {
        // Find the user's active session
        String sessionId = findUserSession(userEmail);
        if (sessionId == null) {
            return new LiveTranscriptResult("error", "No active recording session", null);
        }
        
        try {
            // Accumulate audio data
            ByteArrayOutputStream sessionBuffer = activeSessions.get(sessionId);
            sessionBuffer.write(audioData);
            
            // Process every 2 seconds of audio (roughly 32KB for 16kHz mono)
            if (sessionBuffer.size() > 32000) {
                return processAccumulatedAudio(sessionId, userEmail);
            }
            
            return new LiveTranscriptResult("chunk_received", "Audio chunk received", sessionId);
            
        } catch (IOException e) {
            return new LiveTranscriptResult("error", "Failed to process audio chunk: " + e.getMessage(), sessionId);
        }
    }
    
    public LiveTranscriptResult stopSession(String userEmail) {
        String sessionId = findUserSession(userEmail);
        if (sessionId == null) {
            return new LiveTranscriptResult("error", "No active recording session", null);
        }
        
        try {
            // Process any remaining audio
            LiveTranscriptResult result = processAccumulatedAudio(sessionId, userEmail);
            
            // Clean up session
            activeSessions.remove(sessionId);
            sessionOwners.remove(sessionId);
            
            return new LiveTranscriptResult("stopped", "Recording stopped", sessionId, 
                result.transcript, result.summary);
                
        } catch (Exception e) {
            return new LiveTranscriptResult("error", "Failed to stop recording: " + e.getMessage(), sessionId);
        }
    }
    
    private LiveTranscriptResult processAccumulatedAudio(String sessionId, String userEmail) {
        try {
            ByteArrayOutputStream sessionBuffer = activeSessions.get(sessionId);
            byte[] audioBytes = sessionBuffer.toByteArray();
            
            if (audioBytes.length == 0) {
                return new LiveTranscriptResult("no_audio", "No audio data to process", sessionId);
            }
            
            // Create a temporary file for Deepgram processing
            MultipartFile audioFile = new SimpleMultipartFile(
                "audio", "audio.wav", "audio/wav", audioBytes);
            
            // Transcribe with Deepgram
            TranscriptResult transcript = deepgramClient.transcribe(audioFile);
            String transcriptText = transcript != null && transcript.rawText != null ? transcript.rawText : "";
            
            // Generate summary if we have transcript
            String summary = "";
            if (!transcriptText.trim().isEmpty()) {
                SummaryResult summaryResult = geminiClient.summarize(transcriptText, null);
                summary = summaryResult != null ? summaryResult.summary : "";
            }
            
            // Save to database
            MeetingRecord record = new MeetingRecord();
            record.ownerEmail = userEmail;
            record.transcriptText = transcriptText;
            record.summary = summary;
            repository.save(record);
            
            // Clear the buffer for next chunk
            sessionBuffer.reset();
            
            return new LiveTranscriptResult("processed", "Audio processed", sessionId, transcriptText, summary);
            
        } catch (Exception e) {
            return new LiveTranscriptResult("error", "Failed to process audio: " + e.getMessage(), sessionId);
        }
    }
    
    private String findUserSession(String userEmail) {
        return sessionOwners.entrySet().stream()
            .filter(entry -> userEmail.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
}
