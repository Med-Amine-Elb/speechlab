package com.kasayko.meeting.minutes_api.client;

import com.kasayko.meeting.minutes_api.config.ApiProperties;
import com.kasayko.meeting.minutes_api.dto.SummaryResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
	private final WebClient webClient;
	private final ApiProperties properties;

	public GeminiClient(ApiProperties properties, WebClient.Builder builder) {
		this.properties = properties;
		this.webClient = builder.build();
	}

	public SummaryResult summarize(String transcriptText, String targetLanguage) {
		if (properties.getGemini().getApiKey() == null || properties.getGemini().getApiKey().isBlank() || properties.getGemini().getUrl() == null) {
			SummaryResult s = new SummaryResult();
			s.summary = "[Mock summary because GEMINI API KEY/URL not set]";
			s.actionItems = List.of("[Mock action 1]", "[Mock action 2]");
			return s;
		}
		String lang = (targetLanguage == null || targetLanguage.isBlank()) ? "en" : targetLanguage;
		Map<String, Object> payload = Map.of(
				"contents", List.of(Map.of(
					"role", "user",
					"parts", List.of(Map.of("text", "You are a helpful assistant. Summarize this meeting in language '" + lang + "' and extract clear bullet-point action items (start bullets with '-' or '*'). Keep it concise. Transcript:\n\n" + transcriptText))
				))
		);
		return webClient.post()
				.uri(properties.getGemini().getUrl() + "?key=" + properties.getGemini().getApiKey())
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(payload))
				.retrieve()
				.bodyToMono(Map.class)
				.map(this::mapGemini)
				.block();
	}

    private SummaryResult mapGemini(Map<?,?> resp) {
        SummaryResult s = new SummaryResult();
        try {
            List<?> candidates = (List<?>) resp.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?,?> c0 = (Map<?,?>) candidates.get(0);
                Map<?,?> content = (Map<?,?>) c0.get("content");
                List<?> parts = (List<?>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    Map<?,?> p0 = (Map<?,?>) parts.get(0);
                    Object text = p0.get("text");
                    if (text != null) {
                        String t = text.toString();
                        s.summary = t;
                        // naive action items extraction: lines starting with -, *
                        s.actionItems = t.lines()
                                .filter(line -> line.strip().startsWith("-") || line.strip().startsWith("*"))
                                .map(line -> line.replaceFirst("^[*-]\\s?", "").strip())
                                .filter(line -> !line.isBlank())
                                .toList();
                    }
                }
            }
        } catch (Exception ignore) { }
        return s;
    }
}


