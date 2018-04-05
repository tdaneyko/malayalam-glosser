package de.ws1718.ismla.gloss.server;

import java.util.List;

import de.ws1718.ismla.gloss.client.GlossedSentence;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

public interface LanguageGlosser {
	public List<GlossedSentence> gloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat);
}
