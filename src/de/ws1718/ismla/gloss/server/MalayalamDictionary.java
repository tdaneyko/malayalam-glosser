package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ws1718.ismla.gloss.client.GlossedWord;

public class MalayalamDictionary {
	
	private Map<String, Set<String>> splits;
	private Map<String, Set<String>> glosses;

	// Read in single tokens from dictionary
	// Generate forms with splits and glosses
	
	// ea vs. o normalisieren!!!
	
	public MalayalamDictionary(BufferedReader read) {
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
	
	public GlossedWord lookup(String word) {
		if (!splits.containsKey(word))
			return new GlossedWord(word, word, new String[]{word}, new String[][]{new String[]{"<unknown>"}});
		String[] spl = splits.get(word).stream().toArray(String[]::new);
		String[][] gl = new String[spl.length][];
		for (int i = 0; i < spl.length; i++)
			gl[i] = glosses.get(spl[i]).stream().toArray(String[]::new);
		return new GlossedWord(word, word, spl, gl);
	}
	
}
