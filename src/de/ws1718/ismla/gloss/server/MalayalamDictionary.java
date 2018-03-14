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

public class MalayalamDictionary {
	
	public static final MalayalamFormat DICT_FORMAT = MalayalamFormat.ISO15919_ASCII;
	private static final Pattern PUNCT = Pattern.compile("\\p{Punct}");
	
	private GlossNormalizer normalizer;
	
	private MalayalamFormat origFormat;
	private MalayalamFormat glossFormat;
	
	private Map<String, Set<Gloss>> glosses;
	private ReverseTrie trie;

	// Read in single tokens from dictionary
	// Generate forms with splits and glosses
	
	public MalayalamDictionary(BufferedReader read) {
		this.normalizer = new GlossNormalizer();
		this.origFormat = DICT_FORMAT;
		this.glossFormat = DICT_FORMAT;
		Runtime r = Runtime.getRuntime();
		System.gc();
		long memStart = r.totalMemory() - r.freeMemory();
		this.glosses = new HashMap<>();
		try {
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				String[] fields = StringUtils.split(line, '\t');
				if (fields.length == 5 || fields.length == 4) {
					String surfaceForm = fields[0];
					String split = fields[1];
					String[] prefix = StringUtils.split(fields[2], '/');
					String[] transl = StringUtils.split(fields[3], '/');
					String[] suffix = StringUtils.split(fields[4], '/');
					if (!glosses.containsKey(surfaceForm))
						glosses.put(surfaceForm, new HashSet<>());
					glosses.get(surfaceForm).add(new Gloss(split, prefix, transl, suffix));
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.gc();
		long memMap = r.totalMemory() - r.freeMemory();
		this.trie = new ReverseTrie(glosses);
		System.gc();
		long memTrie = r.totalMemory() - r.freeMemory();
		System.err.println(memStart + " " + memMap + " " + memTrie);
		System.err.println("Map memory: " + (memMap - memStart) + " Trie memory: " + (memTrie - memMap));
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
		System.err.println(timStart + " " + timMap + " " + timTrie);
		System.err.println("Map time: " + (timMap - timStart) + " Trie time: " + (timTrie - timMap));
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
		    glosses = (Map<String, Set<Gloss>>) in.readObject();
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
	
	public void setFormats(MalayalamFormat origFormat, MalayalamFormat glossFormat) {
		this.origFormat = origFormat;
		this.glossFormat = glossFormat;
	}
	
	public boolean contains(String word) {
		return glosses.containsKey(word);
	}
	
	public boolean contains(String word, MalayalamTranscriptor transcr) {
		return glosses.containsKey(transcr.convertTo(word, DICT_FORMAT));
	}
	
	public int suffixSearch(String word) {
		return trie.suffixSearch(word);
	}
	
	public GlossedWord lookup(String word) {
		return lookup(word, null);
	}
	
	public GlossedWord lookup(String word, MalayalamTranscriptor transcr) {
		String orig = (transcr == null) ? word : transcr.convertBetween(word, DICT_FORMAT, origFormat);
		String ipa = (transcr == null) ? word : transcr.transcribe(word, DICT_FORMAT);
		if (PUNCT.matcher(word).matches() || NumberUtils.isNumber(word))
			return new GlossedWord(orig, orig, new String[]{orig}, new String[][]{new String[]{orig}});
		if (!glosses.containsKey(word)) {
			if (transcr != null)
				word = transcr.convertFrom(word, DICT_FORMAT);
			return new GlossedWord(orig, ipa, new String[]{word}, new String[][]{new String[]{"<unknown>"}});
		}
		List<Gloss> gList = trie.get(word);
		String[][] gl = gList.stream()
				.map(gloss -> Arrays.stream(gloss.getTransl())
						.map(tr -> normalizer.convert(tr))
						.toArray(String[]::new))
				.toArray(String[][]::new);
		String[] spl = gList.stream().map(gloss -> normalizer.convert(transcr.convertFrom(gloss.getSplit(), DICT_FORMAT))).toArray(String[]::new);
//		String[] spl = splits.get(word).stream().toArray(String[]::new);
//		String[][] gl = new String[spl.length][];
//		for (int i = 0; i < spl.length; i++) {
//			gl[i] = glosses.get(spl[i]).stream().map(g -> normalizer.convert(g)).toArray(String[]::new);
//			if (transcr != null)
//				spl[i] = normalizer.convert(transcr.convertFrom(spl[i], DICT_FORMAT));
//		}
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
