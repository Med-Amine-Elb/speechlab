package com.kasayko.meeting.minutes_api.service;

import com.kasayko.meeting.minutes_api.client.DeepgramClient;
import com.kasayko.meeting.minutes_api.client.GeminiClient;
import com.kasayko.meeting.minutes_api.dto.ProcessAudioResponse;
import com.kasayko.meeting.minutes_api.dto.SummaryResult;
import com.kasayko.meeting.minutes_api.dto.TranscriptResult;
import com.kasayko.meeting.minutes_api.model.MeetingRecord;
import com.kasayko.meeting.minutes_api.repo.MeetingRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingService {
	private final DeepgramClient deepgramClient;
	private final GeminiClient geminiClient;
	private final MeetingRecordRepository repository;

	public MeetingService(DeepgramClient deepgramClient, GeminiClient geminiClient, MeetingRecordRepository repository) {
		this.deepgramClient = deepgramClient;
		this.geminiClient = geminiClient;
		this.repository = repository;
	}

	public ProcessAudioResponse processAudio(MultipartFile file) {
		TranscriptResult transcript = deepgramClient.transcribe(file);
		String text = transcript != null && transcript.rawText != null ? transcript.rawText : "";
		SummaryResult summary = geminiClient.summarize(text, null);

		MeetingRecord rec = new MeetingRecord();
		rec.transcriptText = text;
		rec.summary = summary != null ? summary.summary : null;
		rec.actionItems = summary != null ? summary.actionItems : null;
		rec = repository.save(rec);

		ProcessAudioResponse resp = new ProcessAudioResponse();
		resp.transcript = transcript;
		resp.summary = summary;
		resp.recordId = rec.id;
		return resp;
	}

	public ProcessAudioResponse processAudioWithLanguage(MultipartFile file, String targetLanguage) {
		TranscriptResult transcript = deepgramClient.transcribe(file);
		String text = transcript != null && transcript.rawText != null ? transcript.rawText : "";
		SummaryResult summary = geminiClient.summarize(text, targetLanguage);

		MeetingRecord rec = new MeetingRecord();
		rec.transcriptText = text;
		rec.summary = summary != null ? summary.summary : null;
		rec.actionItems = summary != null ? summary.actionItems : null;
		rec = repository.save(rec);

		ProcessAudioResponse resp = new ProcessAudioResponse();
		resp.transcript = transcript;
		resp.summary = summary;
		resp.recordId = rec.id;
		return resp;
	}

	public ProcessAudioResponse processAudioWithOwner(MultipartFile file, String ownerEmail) {
		TranscriptResult transcript = deepgramClient.transcribe(file);
		String text = transcript != null && transcript.rawText != null ? transcript.rawText : "";
		SummaryResult summary = geminiClient.summarize(text, null);

		MeetingRecord rec = new MeetingRecord();
		rec.ownerEmail = ownerEmail;
		rec.transcriptText = text;
		rec.summary = summary != null ? summary.summary : null;
		rec.actionItems = summary != null ? summary.actionItems : null;
		rec = repository.save(rec);

		ProcessAudioResponse resp = new ProcessAudioResponse();
		resp.transcript = transcript;
		resp.summary = summary;
		resp.recordId = rec.id;
		return resp;
	}

	public ProcessAudioResponse processAudioWithLanguageAndOwner(MultipartFile file, String targetLanguage, String ownerEmail) {
		TranscriptResult transcript = deepgramClient.transcribe(file);
		String text = transcript != null && transcript.rawText != null ? transcript.rawText : "";
		SummaryResult summary = geminiClient.summarize(text, targetLanguage);

		MeetingRecord rec = new MeetingRecord();
		rec.ownerEmail = ownerEmail;
		rec.transcriptText = text;
		rec.summary = summary != null ? summary.summary : null;
		rec.actionItems = summary != null ? summary.actionItems : null;
		rec = repository.save(rec);

		ProcessAudioResponse resp = new ProcessAudioResponse();
		resp.transcript = transcript;
		resp.summary = summary;
		resp.recordId = rec.id;
		return resp;
	}
}


