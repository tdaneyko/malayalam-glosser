package de.ws1718.ismla.gloss.server;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.ws1718.ismla.gloss.client.GlossedSentence;
import de.ws1718.ismla.gloss.client.GlossedWord;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

public class MalayalamGlosser {

	private MalayalamDictionary dict;
	private MalayalamTranscriptor transcr;

	private static final Pattern SENT_SPLITTER = Pattern.compile("(?<=[\\.:;!?]) |\\n+");
	private static final Pattern TOKENIZER = Pattern.compile(" |(?=[\\.,:;!?](\\z|\\n| ))");

	public MalayalamGlosser(MalayalamDictionary dict, MalayalamTranscriptor transcr) {
		this.dict = dict;
		this.transcr = transcr;
	}

	public List<GlossedSentence> gloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat) {
		long start = System.currentTimeMillis();
		dict.setFormats(inFormat, outFormat);
		transcr.setFormats(inFormat, outFormat);

		List<GlossedSentence> glS = new ArrayList<>();

		for (String sentence : SENT_SPLITTER.split(text)) {
			List<String> tok = new ArrayList<>();
			List<GlossedWord> gl = new ArrayList<>();

			// Do a simple whitespace and punctuation tokenization
			String[] words = TOKENIZER.split(sentence);

			// Properly tokenize individual words and convert them to dictionary transliteration
			for (String word : words) {
				String lookupWord = transcr.convertTo(word, MalayalamDictionary.DICT_FORMAT);
				List<String> tokenizedWord = tokenizeWord(lookupWord);
				if (tokenizedWord == null)
					tok.add(lookupWord);
				else
					tok.addAll(tokenizedWord);
			}

			// Get glosses from the dictionary
			for (String token : tok) {
				//System.err.println(token);
				gl.add(dict.lookup(token, transcr));
			}

			glS.add(new GlossedSentence(sentence, gl));
		}
		System.err.println(System.currentTimeMillis() - start);
		return glS;
	}

//	public List<String> tokenizeWord(String word) {
//		List<String> tokens = new ArrayList<>();
//		if (!word.isEmpty()) {
//			// Word already constitutes a token
//			if (dict.contains(word))
//				tokens.add(word);
//			// Word consists of multiple tokens or is unknown -> split
//			else {				
//				// Recursively check suffix of string
//				for (int i = 0; i <word.length(); i++) {
//					String tail = word.substring(i);
//					// Case aa.n' -> aa
//					if (tail.equals("aa"))
//						tail = "aa.n'";
//					if (dict.contains(tail)) {
//						// Collect candidates for remaining string
//						String head = word.substring(0, i);
//						List<String> candidates = new ArrayList<>();
//						candidates.add(head);
//						int z = head.length()-1;
//
//						// SPECIAL CASES
//						// Question & emphasis particle
//						if (tail.equals("oo") && head.equals("vee.n"))
//							candidates.add("vee.na;m");
//						if ((tail.equals("ee") || tail.equals("oo")) && !isVowel(head.charAt(z)))
//							candidates.add(head + 'u');
//						// aa.n'
//						if (tail.equals("aa.n'")) {
//							if (!isVowel(head.charAt(z)))
//								candidates.add(head + 'a');
//						}
//						// Present tense
//						if (head.endsWith("unn"))
//							candidates.add(head + 'u');
//						
//						// SANDHI EFFECTS
//						// Glide insertion
//						if ((head.charAt(z) == 'y' || head.charAt(z) == 'v') && isVowel(tail.charAt(0)))
//							candidates.add(head.substring(0, z));
//						// Anusvaaram -> m
//						if (head.charAt(z) == 'm')
//							candidates.add(head.substring(0, z) + ";m");
//						// Anusvaaram deletion
//						if (head.charAt(z) == 'a')
//							candidates.add(head + ";m");
//						// Candrakkala deletion
//						if (isVowel(tail.charAt(0)) && !isVowel(head.charAt(z)))
//							candidates.add(head + '\'');
//						// Candrakkala -> u
//						if (head.charAt(z) == 'u' && !isVowel(tail.charAt(0)))
//							candidates.add(head.substring(0, z) + '\'');
//						// Gemination
//						if (head.charAt(z) == tail.charAt(0))
//							candidates.add(head.substring(0, z));
//
//						// Test candidates
//						for (String cand : candidates) {
//							tokens = tokenizeWord(cand);
//							if (tokens != null) {
//								tokens.add(tail);
//								return tokens;
//							}
//						}
//					}
//				}
//				// No recognizable split found -> return whole word, will be unknown by dictionary
//				if (tokens == null || tokens.isEmpty())
//					return null;
//			}
//		}
//		return tokens;
//	}
	
	public List<String> tokenizeWord(String word) {
		List<String> tokens = new ArrayList<>();
		if (!word.isEmpty()) {
			int i = dict.suffixSearch(word);
			// Case aa.n' -> aa
			if (i < 0 && word.endsWith("aa"))
				i = word.length()-2;
			if (i == 0) {
				tokens.add(word);
				return tokens;
			}
			if (i < 0)
				return null;
			else {
				String head = word.substring(0, i);
				String tail = word.substring(i);
				// Case aa.n' -> aa
				if (tail.equals("aa"))
					tail = "aa.n'";
				List<String> candidates = new ArrayList<>();
				candidates.add(head);
				int z = head.length()-1;

				// SPECIAL CASES
				// Question & emphasis particle
				if (tail.equals("oo") && head.equals("vee.n"))
					candidates.add("vee.na;m");
				if ((tail.equals("ee") || tail.equals("oo")) && !isVowel(head.charAt(z)))
					candidates.add(head + 'u');
				// aa.n'
				if (tail.equals("aa.n'")) {
					if (!isVowel(head.charAt(z)))
						candidates.add(head + 'a');
				}
				// Present tense
				if (head.endsWith("unn"))
					candidates.add(head + 'u');
				
				// SANDHI EFFECTS
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
					candidates.add(head + '\'');
				// Candrakkala -> u
				if (head.charAt(z) == 'u' && !isVowel(tail.charAt(0)))
					candidates.add(head.substring(0, z) + '\'');
				// Gemination
				if (head.charAt(z) == tail.charAt(0))
					candidates.add(head.substring(0, z));

				// Test candidates
				for (String cand : candidates) {
					tokens = tokenizeWord(cand);
					if (tokens != null) {
						tokens.add(tail);
						return tokens;
					}
				}
				
				// No recognizable split found -> return whole word, will be unknown by dictionary
				if (tokens == null || tokens.isEmpty())
					return null;
			}
		}
		return tokens;
	}

	private boolean isVowel(char c) {
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
	}
}
