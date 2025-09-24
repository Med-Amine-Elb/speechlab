package com.kasayko.meeting.minutes_api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
	private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	public String generateToken(String subject, Map<String, Object> claims, long expiresSeconds) {
		Instant now = Instant.now();
		return Jwts.builder()
				.setSubject(subject)
				.addClaims(claims)
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plusSeconds(expiresSeconds)))
				.signWith(secretKey)
				.compact();
	}

	public String extractSubject(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
}


