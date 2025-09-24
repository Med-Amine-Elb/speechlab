package com.kasayko.meeting.minutes_api.dto;

import java.util.List;

public class TranscriptResult {
	public static class Utterance {
		public String speakerLabel;
		public String text;
	}

	public String language;
	public List<Utterance> utterances;
	public String rawText;
}


