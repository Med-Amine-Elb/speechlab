package com.kasayko.meeting.minutes_api.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {
	public byte[] generate(String title, String transcript, String summary) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Document doc = new Document();
			PdfWriter.getInstance(doc, out);
			doc.open();
			doc.add(new Paragraph(title));
			doc.add(new Paragraph("Summary:"));
			doc.add(new Paragraph(summary != null ? summary : ""));
			doc.add(new Paragraph("Transcript:"));
			doc.add(new Paragraph(transcript != null ? transcript : ""));
			doc.close();
			return out.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}


