package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import de.ws1718.ismla.gloss.client.GlossedWord;
import de.ws1718.ismla.gloss.server.translit.GlossNormalizer;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

public class MalayalamDictionary {
	
	public static final MalayalamFormat DICT_FORMAT = MalayalamFormat.ISO15919_ASCII;
	private static final Pattern PUNCT = Pattern.compile("\\p{Punct}");
	
	private GlossNormalizer normalizer;
	
	private MalayalamFormat origFormat;
	private MalayalamFormat glossFormat;
	
	private Map<String, Set<String>> splits;
	private Map<String, Set<String>> glosses;

	// Read in single tokens from dictionary
	// Generate forms with splits and glosses
	
	// ea vs. o normalisieren!!!
	
	public MalayalamDictionary(BufferedReader read) {
		this.normalizer = new GlossNormalizer();
		this.origFormat = DICT_FORMAT;
		this.glossFormat = DICT_FORMAT;
		this.splits = new HashMap<>();
		this.glosses = new HashMap<>();
		try {
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				String[] fields = line.split("\t");
				if (fields.length == 3) {
					String surfaceForm = fields[0];
					String split = fields[1];
					String gloss = fields[2];
					if (!splits.containsKey(surfaceForm))
						splits.put(surfaceForm, new HashSet<>());
					splits.get(surfaceForm).add(split);
					if (!glosses.containsKey(split))
						glosses.put(split, new HashSet<>());
					glosses.get(split).add(gloss);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setFormats(MalayalamFormat origFormat, MalayalamFormat glossFormat) {
		this.origFormat = origFormat;
		this.glossFormat = glossFormat;
	}
	
	public boolean contains(String word) {
		return splits.containsKey(word);
	}
	
	public boolean contains(String word, MalayalamTranscriptor transcr) {
		return splits.containsKey(transcr.convertTo(word, DICT_FORMAT));
	}
	
	public GlossedWord lookup(String word) {
		return lookup(word, null);
	}
	
	public GlossedWord lookup(String word, MalayalamTranscriptor transcr) {
		String orig = (transcr == null) ? word : transcr.convertBetween(word, DICT_FORMAT, origFormat);
		String ipa = (transcr == null) ? word : transcr.transcribe(word, DICT_FORMAT);
		if (PUNCT.matcher(word).matches() || NumberUtils.isNumber(word))
			return new GlossedWord(orig, orig, new String[]{orig}, new String[][]{new String[]{orig}});
		if (!splits.containsKey(word)) {
			if (transcr != null)
				word = transcr.convertFrom(word, DICT_FORMAT);
			return new GlossedWord(orig, ipa, new String[]{word}, new String[][]{new String[]{"<unknown>"}});
		}
		String[] spl = splits.get(word).stream().toArray(String[]::new);
		String[][] gl = new String[spl.length][];
		for (int i = 0; i < spl.length; i++) {
			gl[i] = glosses.get(spl[i]).stream().map(g -> normalizer.convert(g)).toArray(String[]::new);
			if (transcr != null)
				spl[i] = normalizer.convert(transcr.convertFrom(spl[i], DICT_FORMAT));
		}
		return new GlossedWord(orig, ipa, spl, gl);
	}
	
	private boolean isNumber(String s) {
		if (s.isEmpty())
			return false;
		for (int i = (s.charAt(0) == '-') ? 1 : 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!Character.isDigit(c) || c != ',' || c != '.')
				return false;
		}
		return true;
	}
	
}
