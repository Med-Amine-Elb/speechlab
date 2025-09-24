package com.kasayko.meeting.minutes_api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class UserAccount {
	@Id
	public String id;
	@Indexed(unique = true)
	public String email;
	public String passwordHash;
	public Role role = Role.USER;
	public int monthlyQuotaMinutes = 10;
	public int usedMinutesThisPeriod = 0;
	public Instant quotaPeriodStart = Instant.now();
}


