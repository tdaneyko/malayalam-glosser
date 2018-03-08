package de.ws1718.ismla.gloss.client;

import java.io.Serializable;
import java.util.Arrays;

public class GlossedWord implements Serializable {

	private String orig;
	private String ipa;
	private String[] splits;
	private String[][] glosses;
	
	public GlossedWord() {
		this.orig = "";
		this.ipa = "";
		this.splits = new String[]{""};
		this.glosses = new String[][]{new String[]{""}};
	}
	
	public GlossedWord(String orig, String ipa, String[] splits, String[][] glosses) {
		this.orig = orig;
		this.ipa = ipa;
		this.splits = splits;
		this.glosses = glosses;
	}
	
	public String getOrig() {
		return this.orig;
	}
	
	public String getIpa() {
		return this.ipa;
	}
	
	public void setIpa(String ipa) {
		this.ipa = ipa;
	}
	
	public String[] getSplits() {
		return this.splits;
	}
	
	public String[] getGlosses(String split) {
		int i = arrayIndexOf(split, this.splits);
		if (i >= 0)
			return Arrays.copyOf(this.glosses[i], this.glosses[i].length);
		return new String[]{};
	}
	
	private int arrayIndexOf(String s, String[] a) {
		for (int i = 0; i < a.length; i++)
			if (a[i].equals(s))
				return i;
		return -1;
	}
}
