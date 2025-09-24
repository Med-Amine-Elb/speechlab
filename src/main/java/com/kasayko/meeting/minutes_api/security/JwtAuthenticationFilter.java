package com.kasayko.meeting.minutes_api.security;

import com.kasayko.meeting.minutes_api.model.UserAccount;
import com.kasayko.meeting.minutes_api.repo.UserAccountRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final UserAccountRepository userRepo;

	public JwtAuthenticationFilter(JwtService jwtService, UserAccountRepository userRepo) {
		this.jwtService = jwtService;
		this.userRepo = userRepo;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.startsWith("Bearer ")) {
			String token = auth.substring(7);
			try {
				String email = jwtService.extractSubject(token);
				Optional<UserAccount> user = userRepo.findByEmail(email);
				if (user.isPresent()) {
					UserDetails details = User.withUsername(email).password("").authorities("ROLE_" + user.get().role.name()).build();
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			} catch (Exception ignored) { }
		}
		filterChain.doFilter(request, response);
	}
}


