package com.kasayko.meeting.minutes_api.repo;

import com.kasayko.meeting.minutes_api.model.MeetingRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MeetingRecordRepository extends MongoRepository<MeetingRecord, String> {
	List<MeetingRecord> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
}


