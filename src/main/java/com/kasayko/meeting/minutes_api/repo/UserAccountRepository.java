package com.kasayko.meeting.minutes_api.repo;

import com.kasayko.meeting.minutes_api.model.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
	Optional<UserAccount> findByEmail(String email);
}


