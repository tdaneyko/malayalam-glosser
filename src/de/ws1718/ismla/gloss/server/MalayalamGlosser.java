package de.ws1718.ismla.gloss.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.ws1718.ismla.gloss.shared.GlossedSentence;
import de.ws1718.ismla.gloss.shared.GlossedWord;

public class MalayalamGlosser extends LanguageGlosser {

	// A regex matching sentence boundaries
	private static final Pattern SENT_SPLITTER = Pattern.compile("(?<=[\\.:;!?]) |\\n+");
	// A simple whitespace tokenizer regex
	private static final Pattern TOKENIZER = Pattern.compile(" |(?=[\\.,:;!?](\\z|\\n| ))");
	
	/**
	 * Create a Malayalam Glosser with a dictionary and a transliterator.
	 * @param dict The dictionary
	 * @param transcr The transliterator
	 */
	public MalayalamGlosser(UnfoldedDictionary dict, GlossTransliterator transcr) {
		super(dict, transcr);
	}

	/**
	 * Create glosses for a text, i.e. identify sentences, tokenize the sentences and create glosses for each token.
	 * @param text The text to be glossed
	 * @param inFormat The script in which the text was typed
	 * @param outFormat The script in which to display the glosses
	 * @return A list of glossed sentences
	 */
	public List<GlossedSentence> gloss(String text, String inFormat, String outFormat) {
		// Pass input and output scripts to transliterator
		transcr.setFormats(inFormat, outFormat);

		List<GlossedSentence> glS = new ArrayList<>();
		// Split sentences
		for (String sentence : SENT_SPLITTER.split(text)) {
			List<String> tok = new ArrayList<>();
			List<GlossedWord> gl = new ArrayList<>();

			// Do a simple whitespace and punctuation tokenization
			String[] words = TOKENIZER.split(sentence);

			// Properly tokenize individual words and convert them to dictionary transliteration
			for (String word : words) {
				String lookupWord = transcr.convertTo(word, dict.dictFormat);
				List<String> tokenizedWord = sandhiSplit(lookupWord);
				if (tokenizedWord == null)
					tok.add(lookupWord);
				else
					tok.addAll(tokenizedWord);
			}

			// Get glosses from the dictionary
			for (String token : tok)
				gl.add(dict.lookup(token, transcr));

			glS.add(new GlossedSentence(sentence, gl));
		}
		return glS;
	}
	
	/**
	 * Split a word possibly consisting of multiple individual words.
	 * @param word A Malayalam word
	 * @return The tokens contained in the word
	 */
	public List<String> sandhiSplit(String word) {
		List<String> tokens = new ArrayList<>();
		
		// If word is the empty string, return empty list
		if (word.isEmpty())
			return tokens;
		
		// Get start index of longest known suffix
		int i = dict.suffixSearch(word);
		
		// Case aa.n' -> aa
		if (i < 0 && word.endsWith("aa"))
			i = word.length()-2;
		
		// If whole word could be found in the dictionary, return word
		if (i == 0) {
			tokens.add(word);
			return tokens;
		}
		
		// If word has no known suffix, return null
		if (i < 0)
			return null;
		
		// Else split
		String head = word.substring(0, i);
		String tail = word.substring(i);
		
		// Case aa.n' -> aa
		if (tail.equals("aa"))
			tail = "aa.n^u";
		
		// Get candidates for prefix according to sandhi rules
		List<String> candidates = new ArrayList<>();
		candidates.add(head);
		int z = head.length()-1;

		// SPECIAL CASES
		// Question & emphasis particle
		if (tail.equals("oo") && head.equals("vee.n"))
			candidates.add("vee.na;m");
		if ((tail.equals("ee") || tail.equals("oo")) && !isVowel(head.charAt(z)))
			candidates.add(head + 'u');
		// Coordination clitic u;m
		if (tail.equals("u;m") && head.charAt(z) == 'v')
			candidates.add(head.substring(0, z) + ";m");
		// aa.n'
		if (tail.equals("aa.n^u")) {
			if (!isVowel(head.charAt(z)))
				candidates.add(head + 'a');
		}
		// Present tense
		if (head.endsWith("unn"))
			candidates.add(head + 'u');

		// REGULAR SANDHI EFFECTS
		// Glide insertion
		if ((head.charAt(z) == 'y' || head.charAt(z) == 'v') && isVowel(tail.charAt(0)))
			candidates.add(head.substring(0, z));
		// Anusvaaram -> m
		if (head.charAt(z) == 'm')
			candidates.add(head.substring(0, z) + ";m");
		// Anusvaaram deletion
		if (head.charAt(z) == 'a')
			candidates.add(head + ";m");
		// Candrakkala deletion
		if (isVowel(tail.charAt(0)) && !isVowel(head.charAt(z)))
			candidates.add(head + "^u");
		// Candrakkala -> u
		if (head.charAt(z) == 'u' && head.charAt(z-1) != '^' && !isVowel(tail.charAt(0)))
			candidates.add(head.substring(0, z) + "^u");
		// Gemination
		if (head.charAt(z) == tail.charAt(0))
			candidates.add(head.substring(0, z));
		// cillu r -> _r
		if (head.endsWith("_r"))
			candidates.add(head.substring(0, z-1) + "r");

		// Check whether any candidate yields another valid split
		for (String cand : candidates) {
			tokens = sandhiSplit(cand);
			if (tokens != null) {
				tokens.add(tail);
				return tokens;
			}
		}

		// No recognizable split found -> return null
		return null;
	}

	/**
	 * @param c A character
	 * @return true if c is a vowel, false if not
	 */
	private boolean isVowel(char c) {
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
	}
}
