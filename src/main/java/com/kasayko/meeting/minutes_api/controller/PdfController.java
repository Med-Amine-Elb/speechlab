package com.kasayko.meeting.minutes_api.controller;

import com.kasayko.meeting.minutes_api.model.MeetingRecord;
import com.kasayko.meeting.minutes_api.repo.MeetingRecordRepository;
import com.kasayko.meeting.minutes_api.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
	private final MeetingRecordRepository repository;
	private final PdfService pdfService;

	public PdfController(MeetingRecordRepository repository, PdfService pdfService) {
		this.repository = repository;
		this.pdfService = pdfService;
	}

	@GetMapping("/{id}")
	public ResponseEntity<byte[]> export(@PathVariable String id) {
		Optional<MeetingRecord> opt = repository.findById(id);
		if (opt.isEmpty()) return ResponseEntity.notFound().build();
		MeetingRecord r = opt.get();
		byte[] pdf = pdfService.generate("Meeting Minutes", r.transcriptText, r.summary);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=minutes-" + id + ".pdf")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdf);
	}
}


