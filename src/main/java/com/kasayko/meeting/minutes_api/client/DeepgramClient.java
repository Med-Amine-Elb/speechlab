package com.kasayko.meeting.minutes_api.client;

import com.kasayko.meeting.minutes_api.config.ApiProperties;
import com.kasayko.meeting.minutes_api.dto.TranscriptResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DeepgramClient {
	private final WebClient webClient;
	private final ApiProperties properties;

	public DeepgramClient(ApiProperties properties, WebClient.Builder builder) {
		this.properties = properties;
		this.webClient = builder.build();
	}

	public TranscriptResult transcribe(MultipartFile audio) {
		if (properties.getDeepgram().getApiKey() == null || properties.getDeepgram().getApiKey().isBlank()) {
			TranscriptResult fallback = new TranscriptResult();
			fallback.language = "en";
			fallback.rawText = "[Mock transcript because DEEPGRAM API KEY is not set]";
			fallback.utterances = new ArrayList<>();
			return fallback;
		}
		String url = properties.getDeepgram().getUrl()
				+ "?smart_format=true&punctuate=true&diarize=true&utterances=true";
		return webClient.post()
				.uri(url)
				.header("Authorization", "Token " + properties.getDeepgram().getApiKey())
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(BodyInserters.fromValue(getBytes(audio)))
				.retrieve()
				.bodyToMono(Map.class)
				.map(this::mapDeepgram)
				.block();
	}

	private byte[] getBytes(MultipartFile file) {
		try { return file.getBytes(); } catch (Exception e) { throw new RuntimeException(e); }
	}

    private TranscriptResult mapDeepgram(Map<?,?> dg) {
        TranscriptResult result = new TranscriptResult();
        try {
            Map<?,?> results = (Map<?,?>) dg.get("results");
            List<?> channels = (List<?>) results.get("channels");
            if (channels != null && !channels.isEmpty()) {
                Map<?,?> ch0 = (Map<?,?>) channels.get(0);
                List<?> alts = (List<?>) ch0.get("alternatives");
                if (alts != null && !alts.isEmpty()) {
                    Map<?,?> alt0 = (Map<?,?>) alts.get(0);
                    Object transcript = alt0.get("transcript");
                    result.rawText = transcript != null ? transcript.toString() : null;
                }
            }
            // Utterances diarization (optional)
            List<TranscriptResult.Utterance> utterances = new ArrayList<>();
            Map<?,?> resultsMap = dg.get("results") instanceof Map<?,?> rm ? rm : Map.of();
            Object uttObj = resultsMap.get("utterances");
            if (uttObj instanceof List<?>) {
                for (Object u : (List<?>) uttObj) {
                    if (u instanceof Map<?,?> m) {
                        TranscriptResult.Utterance uu = new TranscriptResult.Utterance();
                        Object spk = m.get("speaker");
                        uu.speakerLabel = spk != null ? spk.toString() : null;
                        Object txt = m.get("transcript");
                        uu.text = txt != null ? txt.toString() : null;
                        utterances.add(uu);
                    }
                }
            }
            result.utterances = utterances;
            // Language (if provided)
            Map<?,?> metaMap = dg.get("metadata") instanceof Map<?,?> mm ? mm : Map.of();
            Object lang = metaMap.get("language");
            result.language = lang != null ? lang.toString() : "en";
        } catch (Exception ignore) { }
        return result;
    }

}
