package de.ws1718.ismla.gloss.server;

import java.util.List;

import de.ws1718.ismla.gloss.client.GlossedSentence;

public abstract class LanguageGlosser {
	
	protected final UnfoldedDictionary dict;
	protected final GlossTransliterator transcr;
	
	protected LanguageGlosser(UnfoldedDictionary dict, GlossTransliterator transcr) {
		this.dict = dict;
		this.transcr = transcr;
	}
	
	public abstract List<GlossedSentence> gloss(String text, String inFormat, String outFormat);
	
}
