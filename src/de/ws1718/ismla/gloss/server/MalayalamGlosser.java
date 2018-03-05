package de.ws1718.ismla.gloss.server;

import java.util.ArrayList;
import java.util.List;

import de.ws1718.ismla.gloss.client.GlossedWord;

public class MalayalamGlosser {
	
	private MalayalamDictionary dict;
	private MalayalamTranscriptor transcr;
	
	public MalayalamGlosser(MalayalamDictionary dict) {
		this.dict = dict;
	}
	
	public List<GlossedWord> gloss(String text) {
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
			gl.add(dict.lookup(word));
		}
		
		// Transcribe tokens
		
		return gl;
	}
	
}
