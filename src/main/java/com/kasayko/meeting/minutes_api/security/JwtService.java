package com.kasayko.meeting.minutes_api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
	
	@Value("${jwt.secret:myDefaultSecretKey123456789012345678901234567890}")
	private String secretString;
	
	private SecretKey secretKey;
	
	private SecretKey getSecretKey() {
		if (secretKey == null) {
			secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
		}
		return secretKey;
	}

	public String generateToken(String subject, Map<String, Object> claims, long expiresSeconds) {
		Instant now = Instant.now();
		return Jwts.builder()
				.setSubject(subject)
				.addClaims(claims)
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plusSeconds(expiresSeconds)))
				.signWith(getSecretKey())
				.compact();
	}

	public String extractSubject(String token) {
		return Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
}


