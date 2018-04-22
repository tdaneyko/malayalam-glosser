package de.ws1718.ismla.gloss.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.ws1718.ismla.gloss.shared.GlossedSentence;
import de.ws1718.ismla.gloss.shared.GlossedWord;

public class RykaGlosser extends LanguageGlosser {

	// A regex matching sentence boundaries
	private static final Pattern SENT_SPLITTER = Pattern.compile("(?<=)|(?<=[\\.:;!?]) |\\n+");
	// A simple whitespace tokenizer regex
	private static final Pattern TOKENIZER = Pattern.compile(" |(?=[\\.,:;!?](\\z|\\n| ))|(?=[])|(?<=[])");

	public RykaGlosser(UnfoldedDictionary dict, GlossTransliterator transcr) {
		super(dict, transcr);
	}

	@Override
	public List<GlossedSentence> gloss(String text, String inFormat, String outFormat) {
		// Pass input and output scripts to transliterator
		transcr.setFormats(inFormat, outFormat);
		
		List<GlossedSentence> glS = new ArrayList<>();
		// Split sentences
		for (String sentence : SENT_SPLITTER.split(text)) {
			List<GlossedWord> gl = new ArrayList<>();

			// Do a simple whitespace and punctuation tokenization
			String[] words = TOKENIZER.split(sentence);

			// Convert words to dictionary transliteration and look them up
			for (String word : words) {
				if (word.equals(""))
					word = ".";
				else if (word.equals("") || word.equals(""))
					word = ",";
				else
					word = transcr.convertTo(word, dict.dictFormat);
//				if (!dict.contains(word) && word.indexOf('-') >= 0) {
//					
//				}
				gl.add(dict.lookup(word, transcr));
			}
			
			glS.add(new GlossedSentence(sentence, gl));
		}
		
		return glS;
	}

}
