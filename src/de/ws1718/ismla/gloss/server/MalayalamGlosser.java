package de.ws1718.ismla.gloss.server;

import java.util.ArrayList;
import java.util.List;

import de.ws1718.ismla.gloss.client.GlossedWord;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

public class MalayalamGlosser {
	
	private MalayalamDictionary dict;
	private MalayalamTranscriptor transcr;
	
	public MalayalamGlosser(MalayalamDictionary dict, MalayalamTranscriptor transcr) {
		this.dict = dict;
		this.transcr = transcr;
	}
	
	public List<GlossedWord> gloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat) {
		transcr.setFormats(inFormat, outFormat);
		
		List<GlossedWord> gl = new ArrayList<>();
		
		// Tokenize
		/// Split at whitespace, isolate punctuation
		/// Look up each token in dictionary
		//// If found -> yay!
		//// Else -> Recursively try sandhi splits and look up parts
		///// If split found -> yay! (& create new tokens)
		///// Else -> save complete token as <unknown>
		String[] words = text.split(" ");
		
		for (String word : words) {
			gl.add(dict.lookup(word, transcr));
		}
		
		// Transcribe tokens
		
		return gl;
	}
	
}
