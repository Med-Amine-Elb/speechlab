package com.kasayko.meeting.minutes_api.controller;

import com.kasayko.meeting.minutes_api.model.Role;
import com.kasayko.meeting.minutes_api.model.UserAccount;
import com.kasayko.meeting.minutes_api.repo.UserAccountRepository;
import com.kasayko.meeting.minutes_api.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserAccountRepository repo;
	private final PasswordEncoder encoder;
	private final JwtService jwtService;

	public AuthController(UserAccountRepository repo, PasswordEncoder encoder, JwtService jwtService) {
		this.repo = repo;
		this.encoder = encoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String password = body.get("password");
		UserAccount user = new UserAccount();
		user.email = email;
		user.passwordHash = encoder.encode(password);
		if ("premium".equalsIgnoreCase(body.getOrDefault("plan", ""))) {
			user.role = Role.PREMIUM;
			user.monthlyQuotaMinutes = 60;
		}
		repo.save(user);
		return ResponseEntity.ok(Map.of("status", "registered"));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
		return repo.findByEmail(body.get("email"))
				.filter(u -> encoder.matches(body.get("password"), u.passwordHash))
				.map(u -> ResponseEntity.ok(Map.of("token", jwtService.generateToken(u.email, Map.of("role", u.role.name()), 3600*12))))
				.orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "invalid_credentials")));
	}
}


