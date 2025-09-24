package com.kasayko.meeting.minutes_api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "meetings")
public class MeetingRecord {
	@Id
	public String id;
	public Instant createdAt = Instant.now();
	public String ownerEmail;
	public String transcriptText;
	public String summary;
	public List<String> actionItems;
}


