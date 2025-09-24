package com.kasayko.meeting.minutes_api.service;

import com.kasayko.meeting.minutes_api.model.UserAccount;
import com.kasayko.meeting.minutes_api.repo.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class QuotaService {
	private final UserAccountRepository repo;

	public QuotaService(UserAccountRepository repo) { this.repo = repo; }

	public boolean canConsume(UserAccount user, int minutes) {
		resetWindowIfNeeded(user);
		return user.usedMinutesThisPeriod + minutes <= user.monthlyQuotaMinutes;
	}

	public void consume(UserAccount user, int minutes) {
		resetWindowIfNeeded(user);
		user.usedMinutesThisPeriod += minutes;
		repo.save(user);
	}

	private void resetWindowIfNeeded(UserAccount user) {
		// Monthly window: reset after 30 days
		if (Duration.between(user.quotaPeriodStart, Instant.now()).toDays() >= 30) {
			user.quotaPeriodStart = Instant.now();
			user.usedMinutesThisPeriod = 0;
			repo.save(user);
		}
	}
}


