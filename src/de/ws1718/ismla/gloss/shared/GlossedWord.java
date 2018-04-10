package de.ws1718.ismla.gloss.shared;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A word with IPA transcription and all possible splits and glosses.
 */
public class GlossedWord implements Serializable {

	// The original word
	private String orig;
	// The IPA transcription of the word
	private String ipa;
	// The possible morpheme splits for this word
	private String[] splits;
	// The possible glosses for each of the morpheme splits
	private String[][] glosses;
	
	/**
	 * The default constructor for an empty word.
	 */
	public GlossedWord() {
		this.orig = "";
		this.ipa = "";
		this.splits = new String[]{""};
		this.glosses = new String[][]{new String[]{""}};
	}
	
	/**
	 * @param orig The original word
	 * @param ipa The IPA transcription of the word
	 * @param splits The possible morpheme splits for this word
	 * @param glosses The possible glosses for each of the morpheme splits
	 */
	public GlossedWord(String orig, String ipa, String[] splits, String[][] glosses) {
		this.orig = orig;
		this.ipa = ipa;
		this.splits = splits;
		this.glosses = glosses;
	}
	
	/**
	 * @return The original word
	 */
	public String getOrig() {
		return this.orig;
	}
	
	/**
	 * @return The IPA transcription of the word
	 */
	public String getIpa() {
		return this.ipa;
	}
	
	/**
	 * @return The possible morpheme splits for this word
	 */
	public String[] getSplits() {
		return Arrays.copyOf(this.splits, this.splits.length);
	}
	
	/**
	 * Get the glosses for a certain morpheme split on this word
	 * @param i Index of the split
	 * @return The glosses for that split
	 */
	public String[] getGlosses(int i) {
		if (i >= 0 && i < glosses.length)
			return Arrays.copyOf(this.glosses[i], this.glosses[i].length);
		return new String[]{};
	}
	
	/**
	 * Get the glosses for a certain morpheme split on this word
	 * @param split The split
	 * @return The glosses for that split
	 */
	public String[] getGlosses(String split) {
		return getGlosses(arrayIndexOf(split, this.splits));
	}
	
	/**
	 * @param s A String
	 * @param a A String array
	 * @return The index of s in a, or -1 if not contained
	 */
	private int arrayIndexOf(String s, String[] a) {
		for (int i = 0; i < a.length; i++)
			if (a[i].equals(s))
				return i;
		return -1;
	}
}
