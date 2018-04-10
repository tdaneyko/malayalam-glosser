package de.ws1718.ismla.gloss.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PageSettings implements Serializable {

	private String langName;
	private Map<String, String> scripts;
	private String[] inScripts;
	private String[] outScripts;
	
	public PageSettings() {
		langName = "Language";
		scripts = new HashMap<>();
		inScripts = new String[0];
		outScripts = new String[0];
	}
	
	public PageSettings(String langName, Map<String, String> scripts, String[] inScripts, String[] outScripts) {
		this.langName = langName;
		this.scripts = scripts;
		this.inScripts = inScripts;
		this.outScripts = outScripts;
	}
	
	public String getLanguage() {
		return langName;
	}
	
	public String[] getInputScripts() {
		return Arrays.copyOf(inScripts, inScripts.length);
	}
	
	public String[] getOutputScripts() {
		return Arrays.copyOf(outScripts, outScripts.length);
	}
	
	public String getScriptName(String script) {
		return scripts.get(script);
	}

}
