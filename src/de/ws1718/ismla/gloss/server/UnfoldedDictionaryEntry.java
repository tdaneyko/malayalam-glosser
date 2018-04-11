package de.ws1718.ismla.gloss.server;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A morpheme-split entry in the MalayalamDictionary.
 */
public class UnfoldedDictionaryEntry implements Serializable {

	// The morpheme-split string
	private String split;
	// The possible glosses for the entry's prefix(es)
	private String[] prefixes;
	// The possible glosses for the entry's suffix(es)
	private String[] suffixes;
	// The translations of the stem
	private String[] transl;
	
	public UnfoldedDictionaryEntry(String split, String[] prefixes, String[] transl, String[] suffixes) {
		this.split = split;
		this.prefixes = prefixes;
		this.suffixes = suffixes;
		this.transl = transl;
		Arrays.sort(this.transl);
	}
	
	public String getSplit() {
		return split;
	}

	/**
	 * @return All possible combinations of prefix glosses, suffix glosses and stem translations
	 */
	public String[] getTransl() {
		String[] fullTransl = new String[prefixes.length * transl.length];
		for (int i = 0; i < prefixes.length; i++) {
			for (int j = 0; j < transl.length; j++) {
				fullTransl[i*transl.length+j] = prefixes[i] + transl[j] + suffixes[i];
			}
		}
		return fullTransl;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UnfoldedDictionaryEntry) {
			UnfoldedDictionaryEntry other = (UnfoldedDictionaryEntry) obj;
			return this.split.equals(other.split) && this.prefixes.equals(other.prefixes)
					&& this.suffixes.equals(other.suffixes) && this.transl.equals(other.transl);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 19 + 17 * split.hashCode() + 13 * prefixes.hashCode() + 11 * suffixes.hashCode() + 7 * transl.hashCode();
	}
}
