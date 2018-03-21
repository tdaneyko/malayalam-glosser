package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import de.ws1718.ismla.gloss.client.GlossedWord;
import de.ws1718.ismla.gloss.server.translit.GlossNormalizer;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;
import de.ws1718.ismla.gloss.shared.StringUtils;

/**
 * A fully inflected Malayalam Dictionary.
 */
public class MalayalamDictionary {
	// Use a trie or hash map to store the entries?
	private static final boolean TRIE = false;
	// Script in which the dictionary entries are stored
	public static final MalayalamFormat DICT_FORMAT = MalayalamFormat.ISO15919_ASCII;
	// Regular expression matching a single punctuation character
	private static final Pattern PUNCT = Pattern.compile("\\p{Punct}");
	
	// A Transliterator normalizing the gloss symbols (| -> -, & -> . and <> -> <>)
	private GlossNormalizer normalizer;
	
	// The actual dictionary as a map or trie
	private Map<String, Set<MalayalamDictionaryEntry>> glosses;
	private ReverseTrie trie;

	/**
	 * Read the dictionary from a file.
	 * @param read A reader opened on the dictionary file
	 */
	public MalayalamDictionary(BufferedReader read) {
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
					MalayalamDictionaryEntry gloss = new MalayalamDictionaryEntry(split, prefix, transl, suffix);
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
	public boolean contains(String word, MalayalamTranscriptor transcr) {
		return this.contains(transcr.convertTo(word, DICT_FORMAT));
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
	public int suffixSearch(String word, MalayalamTranscriptor transcr) {
		return suffixSearch(transcr.convertTo(word, DICT_FORMAT));
	}
	
	/**
	 * @param word A word in the dictionary script
	 * @param transcr A transcriptor set to the original input script and the desired gloss script
	 * @return All glosses for the given word
	 */
	public GlossedWord lookup(String word, MalayalamTranscriptor transcr) {
		// Convert word back to its original script
		String orig = (transcr == null) ? word : transcr.convertBetween(word, DICT_FORMAT, transcr.getDefaultInputFormat());
		// Get IPA transcription
		String ipa = (transcr == null) ? word : transcr.transcribe(word, DICT_FORMAT);
		
		// If word is a number or a punctuation character, do not gloss it
		if (PUNCT.matcher(word).matches() || NumberUtils.isNumber(word))
			return new GlossedWord(orig, orig, new String[]{orig}, new String[][]{new String[]{orig}});
		
		// If not known by the dictionary, gloss as <unknown>
		if (!this.contains(word)) {
			if (transcr != null)
				word = transcr.convertFrom(word, DICT_FORMAT);
			return new GlossedWord(orig, ipa, new String[]{word}, new String[][]{new String[]{"<unknown>"}});
		}
		
		// Else look it up and store the information in a GlossedWord object
		Collection<MalayalamDictionaryEntry> gList = (TRIE) ? trie.get(word) : glosses.get(word);
		String[][] gl = gList.stream()
				.map(gloss -> Arrays.stream(gloss.getTransl())
						.map(tr -> normalizer.convert(tr))
						.toArray(String[]::new))
				.toArray(String[][]::new);
		String[] spl = gList.stream().map(gloss -> normalizer.convert(transcr.convertFrom(gloss.getSplit(), DICT_FORMAT))).toArray(String[]::new);
		return new GlossedWord(orig, ipa, spl, gl);
	}

	
	/**
	 * Method used to compare hash map and trie performance, as mentioned in paper. Kept for later experiments.
	 */
	private void compareMapAndTrie(BufferedReader read) {
		String test = "avi.teyu.l.lata_ri~n~nu";
		System.out.println(trie.suffixSearch(test) + " " + test.substring(trie.suffixSearch(test)));
		long timStart = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			for (int j = 0; j < test.length(); j++) {
				if (glosses.containsKey(test.substring(j)))
					break;
			}
		}
		long timMap = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			trie.suffixSearch(test);
		}
		long timTrie = System.currentTimeMillis();
		System.err.println("Loop - Map time: " + (timMap - timStart) + " Trie time: " + (timTrie - timMap));
		try {
			FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
			long wriStart = System.currentTimeMillis();
			OutputStream stream = new FileOutputStream(new File("map.ser"));
			FSTObjectOutput out = conf.getObjectOutput(stream);
		    out.writeObject(glosses);
		    out.close();
			long wriMap = System.currentTimeMillis();
			stream = new FileOutputStream(new File("trie.ser"));
			out = conf.getObjectOutput(stream);
		    out.writeObject(trie);
		    out.close();
			long wriTrie = System.currentTimeMillis();
			System.err.println("Serialization - Map time: " + (wriMap - wriStart) + " Trie time: " + (wriTrie - wriMap));

			long reaStart = System.currentTimeMillis();
			InputStream streamin = new FileInputStream(new File("map.ser"));
			FSTObjectInput in = conf.getObjectInput(streamin);
		    glosses = (Map<String, Set<MalayalamDictionaryEntry>>) in.readObject();
		    in.close();
			long reaMap = System.currentTimeMillis();
			streamin = new FileInputStream(new File("trie.ser"));
			in = conf.getObjectInput(streamin);
		    trie = (ReverseTrie) in.readObject();
		    in.close();
			long reaTrie = System.currentTimeMillis();
			System.err.println("Deserialization - Map time: " + (reaTrie - reaStart) + " Trie time: " + (reaTrie - reaMap));
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
