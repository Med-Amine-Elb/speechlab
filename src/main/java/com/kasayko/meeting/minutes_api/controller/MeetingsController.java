package com.kasayko.meeting.minutes_api.controller;

import com.kasayko.meeting.minutes_api.model.MeetingRecord;
import com.kasayko.meeting.minutes_api.repo.MeetingRecordRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingsController {
	private final MeetingRecordRepository repo;

	public MeetingsController(MeetingRecordRepository repo) { this.repo = repo; }

	@GetMapping
	public List<MeetingRecord> list() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth != null ? auth.getName() : null;
		return email == null ? List.of() : repo.findByOwnerEmailOrderByCreatedAtDesc(email);
	}
}


