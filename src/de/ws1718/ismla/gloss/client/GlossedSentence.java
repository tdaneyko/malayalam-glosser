package de.ws1718.ismla.gloss.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GlossedSentence implements Serializable {

	private String sentence;
	private List<GlossedWord> glosses;
	
	public GlossedSentence() {
		this.sentence = "";
		this.glosses = new ArrayList<>();
	}
	
	public GlossedSentence(String sentence, List<GlossedWord> glosses) {
		this.sentence = sentence;
		this.glosses = glosses;
	}
	
	public String getSentence() {
		return sentence;
	}
	
	public List<GlossedWord> getGlosses() {
		return glosses;
	}
}
