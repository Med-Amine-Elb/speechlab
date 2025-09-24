package com.kasayko.meeting.minutes_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiProperties {
	private Deepgram deepgram = new Deepgram();
	private Gemini gemini = new Gemini();

	public Deepgram getDeepgram() { return deepgram; }
	public void setDeepgram(Deepgram deepgram) { this.deepgram = deepgram; }

	public Gemini getGemini() { return gemini; }
	public void setGemini(Gemini gemini) { this.gemini = gemini; }

	public static class Deepgram {
		private String apiKey;
		private String url = "https://api.deepgram.com/v1/listen";

		public String getApiKey() { return apiKey; }
		public void setApiKey(String apiKey) { this.apiKey = apiKey; }

		public String getUrl() { return url; }
		public void setUrl(String url) { this.url = url; }
	}

	public static class Gemini {
		private String apiKey;
		private String url;

		public String getApiKey() { return apiKey; }
		public void setApiKey(String apiKey) { this.apiKey = apiKey; }

		public String getUrl() { return url; }
		public void setUrl(String url) { this.url = url; }
	}
}


