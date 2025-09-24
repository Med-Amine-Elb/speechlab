package com.kasayko.meeting.minutes_api.controller;

import com.kasayko.meeting.minutes_api.dto.ProcessAudioResponse;
import com.kasayko.meeting.minutes_api.model.UserAccount;
import com.kasayko.meeting.minutes_api.repo.UserAccountRepository;
import com.kasayko.meeting.minutes_api.service.MeetingService;
import com.kasayko.meeting.minutes_api.service.QuotaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api")
public class AudioController {
	private final MeetingService meetingService;
    private final UserAccountRepository userRepo;
    private final QuotaService quotaService;

	public AudioController(MeetingService meetingService, UserAccountRepository userRepo, QuotaService quotaService) {
		this.meetingService = meetingService;
        this.userRepo = userRepo;
        this.quotaService = quotaService;
	}

	@PostMapping(path = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadAndProcess(@RequestPart("file") MultipartFile file,
	                                         @RequestParam(value = "lang", required = false) String lang) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<UserAccount> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        int estimatedMinutes = estimateMinutes(file);
        UserAccount user = userOpt.get();
        if (!quotaService.canConsume(user, estimatedMinutes)) {
            return ResponseEntity.status(429).body("Quota exceeded. Need " + estimatedMinutes + " min, remaining " + (user.monthlyQuotaMinutes - user.usedMinutesThisPeriod) + " min");
        }
        quotaService.consume(user, estimatedMinutes);

        ProcessAudioResponse resp = (lang == null || lang.isBlank())
                ? meetingService.processAudioWithOwner(file, user.email)
                : meetingService.processAudioWithLanguageAndOwner(file, lang, user.email);
        return ResponseEntity.ok(resp);
	}

    private int estimateMinutes(MultipartFile file) {
        long sizeBytes = file.getSize(); // bytes
        String ct = file.getContentType() != null ? file.getContentType() : "";

        // Rough default bitrates (kilobits per second)
        int bitrateKbps = 128; // default
        if (ct.contains("wav")) bitrateKbps = 1411; // uncompressed PCM 44.1kHz 16-bit stereo
        else if (ct.contains("webm") || ct.contains("ogg")) bitrateKbps = 96;
        else if (ct.contains("mp3")) bitrateKbps = 128;
        else if (ct.contains("m4a") || ct.contains("aac")) bitrateKbps = 128;

        double seconds = (sizeBytes * 8.0) / (bitrateKbps * 1000.0);
        int minutes = (int) Math.ceil(seconds / 60.0);
        return Math.max(minutes, 1);
    }
}


