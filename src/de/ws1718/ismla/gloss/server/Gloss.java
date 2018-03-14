package de.ws1718.ismla.gloss.server;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

public class Gloss implements Serializable {

	private String split;
	private String[] prefixes;
	private String[] suffixes;
	private String[] transl;
	
//	public Gloss(String split, String[] transl) {
//		this.split = split;
//		this.transl = transl;
//		Arrays.sort(this.transl);
//	}
	
	public Gloss(String split, String[] prefixes, String[] transl, String[] suffixes) {
		this.split = split;
		this.prefixes = prefixes;
		this.suffixes = suffixes;
		this.transl = transl;
		Arrays.sort(this.transl);
	}
	
//	public Gloss(String split, Collection<String> transl) {
//		this(split, transl.toArray(new String[transl.size()]));
//	}
	
	public String getSplit() {
		return split;
	}

	public String[] getTransl() {
		String[] fullTransl = new String[prefixes.length * transl.length];
		for (int i = 0; i < prefixes.length; i++) {
			for (int j = 0; j < transl.length; j++) {
				fullTransl[i*transl.length+j] = prefixes[i] + transl[j] + suffixes[i];
			}
		}
		return fullTransl;
	}
	
}
