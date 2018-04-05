package de.ws1718.ismla.gloss.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.Pair;

import de.ws1718.ismla.gloss.server.LanguageGlosser;
import de.ws1718.ismla.gloss.server.MalayalamDictionary;
import de.ws1718.ismla.gloss.server.MalayalamTranscriptor;

public class GlosserSettings {
	
	private String langName;
	private String helpPath;
	private String aboutPath;
	private String glosser;
	private String dictPath;
	private Map<String, String> scripts;
	private String[] inScripts;
	private String[] outScripts;
	private String dictScript;
	private String translScript;
	private Map<String, Pair<String, String>> translits;
	private String transcr;
	
	public GlosserSettings(String filepath, ServletContext servletContext) throws GlosserSettingsException {
		try (BufferedReader read = new BufferedReader(new InputStreamReader(
				servletContext.getResourceAsStream(filepath), "UTF-8"))) {
			langName = "Language";
			helpPath = Config.DEFAULT_HELP;
			aboutPath = Config.DEFAULT_ABOUT;
			transcr = "";
			scripts = new HashMap<>();
			translits = new HashMap<>();
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				if (!line.isEmpty()) {
					String[] fields = StringUtils.split(line, '\t');
					if (fields.length == 2) {
						switch (fields[0]) {
						case "#lang":
							langName = fields[1];
							break;
						case "#helppage":
							helpPath = fields[1];
							break;
						case "#aboutpage":
							aboutPath = fields[1];
							break;
						case "#glossclass":
							glosser = fields[1];
							break;
						case "#dict":
							dictPath = fields[1];
							break;
						case "#informats":
							inScripts = StringUtils.split(fields[1], ',');
							break;
						case "#outformats":
							outScripts = StringUtils.split(fields[1], ',');
							break;
						case "#dictscript":
							dictScript = fields[1];
							break;
						case "#translitscript":
							translScript = fields[1];
							break;
						case "#transcr":
							transcr = fields[1];
							break;
						default: System.err.println("Unknown tag " + fields[0]);
						}
					}
					if (fields.length == 3 && fields[0].equals("#script")) {
						scripts.put(fields[1], fields[2]);
						if (dictScript == null)
							dictScript = fields[1];
						if (translScript == null)
							translScript = fields[1];
					}
					if (fields.length == 4 && fields[0].equals("#transl")) {
						translits.put(fields[1], Pair.of(fields[2], fields[3]));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (dictPath == null)
			throw new GlosserSettingsException("No dictionary (#dict) defined.");
		if (glosser == null)
			throw new GlosserSettingsException("No glosser class (#glossclass) defined.");
		if (scripts.isEmpty())
			throw new GlosserSettingsException("You must define at least one script (#script).");
		if (inScripts == null)
			inScripts = scripts.values().toArray(new String[scripts.size()]);
		if (outScripts == null)
			outScripts = scripts.values().toArray(new String[scripts.size()]);
		for (String script : inScripts)
			if (!script.equals(translScript) && !translits.containsKey(script))
				throw new GlosserSettingsException("No transliterators (#transl) for script " + script);
		for (String script : outScripts)
			if (!script.equals(translScript) && !translits.containsKey(script))
				throw new GlosserSettingsException("No transliterators (#transl) for script " + script);
	}
	
	public String getLanguage() {
		return langName;
	}

	public LanguageGlosser getGlosser(ServletContext servletContext) {
		try {
			Class<?> glossClass = Class.forName(glosser);
			if (glossClass.isInstance(LanguageGlosser.class)) {
				Constructor<?> constructor = glossClass.getConstructor(MalayalamDictionary.class, MalayalamTranscriptor.class);
				MalayalamDictionary dict = new MalayalamDictionary(new BufferedReader(new InputStreamReader(
						servletContext.getResourceAsStream(dictPath), "UTF-8")));
				return (LanguageGlosser) constructor.newInstance(dict, new MalayalamTranscriptor(servletContext));
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
