package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import de.ws1718.ismla.gloss.server.translit.GlossNormalizer;
import de.ws1718.ismla.gloss.shared.GlossedWord;
import de.ws1718.ismla.gloss.shared.StringUtils;

/**
 * A fully inflected dictionary.
 */
public class UnfoldedDictionary {
	// Use a trie or hash map to store the entries?
	private static final boolean TRIE = false;
	// Regular expression matching a single punctuation character
	private static final Pattern PUNCT = Pattern.compile("\\p{Punct}");
	
	// Script in which the dictionary entries are stored
	public final String dictFormat;
	
	// A Transliterator normalizing the gloss symbols (| -> -, & -> . and <> -> <>)
	private GlossNormalizer normalizer;
	
	// The actual dictionary as a map or trie
	private Map<String, Set<UnfoldedDictionaryEntry>> glosses;
	private ReverseTrie trie;

	/**
	 * Read the dictionary from a file.
	 * @param read A reader opened on the dictionary file
	 */
	public UnfoldedDictionary(BufferedReader read, String dictFormat) {
		this.dictFormat = dictFormat;
		this.normalizer = new GlossNormalizer();
		this.glosses = new HashMap<>();
		this.trie = new ReverseTrie();
		
		try {
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				String[] fields = StringUtils.split(line, '\t');
				if (fields.length == 5 || fields.length == 4) {
					String surfaceForm = fields[0];
					String split = fields[1];
					String[] prefix = StringUtils.split(fields[2], '/');
					String[] transl = StringUtils.split(fields[3], '/');
					String[] suffix = StringUtils.split(fields[4], '/');
					UnfoldedDictionaryEntry gloss = new UnfoldedDictionaryEntry(split, prefix, transl, suffix);
					if (TRIE) {
						trie.add(surfaceForm, gloss);
					}
					else {
						if (!glosses.containsKey(surfaceForm))
							glosses.put(surfaceForm, new HashSet<>());
						glosses.get(surfaceForm).add(gloss);
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param word A word in the dictionary script
	 * @return true if the word is known by dictionary, false if not
	 */
	public boolean contains(String word) {
		return (TRIE) ? trie.contains(word) : glosses.containsKey(word);
	}

	/**
	 * @param word A word
	 * @param transcr A transcriptor whose default input script conforms to the script the word is in
	 * @return true if the word is known by dictionary, false if not
	 */
	public boolean contains(String word, GlossTransliterator transcr) {
		return this.contains(transcr.convertTo(word, dictFormat));
	}
	
	/**
	 * @param word A word in the dictionary script
	 * @return The start index of the longest known suffix of the word
	 */
	public int suffixSearch(String word) {
		if (TRIE)
			return trie.suffixSearch(word);
		for (int i = 0; i < word.length(); i++) {
			if (glosses.containsKey(word.substring(i)))
				return i;
		}
		return -1;
	}
	
	/**
	 * @param word A word
	 * @param transcr A transcriptor whose default input script conforms to the script the word is in
	 * @return The start index of the longest known suffix of the word
	 */
	public int suffixSearch(String word, GlossTransliterator transcr) {
		return suffixSearch(transcr.convertTo(word, dictFormat));
	}
	
	/**
	 * @param word A word in the dictionary script
	 * @param transcr A transcriptor set to the original input script and the desired gloss script
	 * @return All glosses for the given word
	 */
	public GlossedWord lookup(String word, GlossTransliterator transcr) {
		// Convert word back to its original script
		String orig = (transcr == null) ? word : transcr.convertBetween(word, dictFormat, transcr.getDefaultInputFormat());
		// Get IPA transcription
		String ipa = (transcr == null) ? word : transcr.transcribe(word, dictFormat);
		
		// If word is a number or a punctuation character, do not gloss it
		if (PUNCT.matcher(word).matches() || NumberUtils.isNumber(word))
			return new GlossedWord(orig, orig, new String[]{orig}, new String[][]{new String[]{orig}});
		
		// If not known by the dictionary, gloss as <unknown>
		if (!this.contains(word)) {
			if (transcr != null)
				word = transcr.convertFrom(word, dictFormat);
			return new GlossedWord(orig, ipa, new String[]{word}, new String[][]{new String[]{"<unknown>"}});
		}
		
		// Else look it up and store the information in a GlossedWord object
		Collection<UnfoldedDictionaryEntry> gList = (TRIE) ? trie.get(word) : glosses.get(word);
		String[][] gl = gList.stream()
				.map(gloss -> Arrays.stream(gloss.getTransl())
						.map(tr -> normalizer.convert(tr))
						.toArray(String[]::new))
				.toArray(String[][]::new);
		String[] spl = gList.stream().map(gloss -> normalizer.convert(transcr.convertFrom(gloss.getSplit(), dictFormat))).toArray(String[]::new);
		return new GlossedWord(orig, ipa, spl, gl);
	}
	
}
