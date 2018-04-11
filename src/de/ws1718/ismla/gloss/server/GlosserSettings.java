package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.Pair;

import de.ws1718.ismla.gloss.shared.PageSettings;
import de.ws1718.ismla.gloss.shared.StringUtils;

/**
 * This class stores language-specific settings for the glosser.
 */
public class GlosserSettings {
	
	// Name of the language
	private String langName;
	// Path to the Help page definition
	private String helpPath;
	// Path to the About page definition
	private String aboutPath;
	// Class name of the glosser
	private String glosser;
	// Path to the dictionary
	private String dictPath;
	// Identifiers and names of the supported scripts
	private Map<String, String> scripts;
	// Identifiers of the supported input scripts
	private String[] inScripts;
	// Identifiers of the supported gloss scripts
	private String[] outScripts;
	// Identifier of the script the dictionary entries are in
	private String dictScript;
	// Identifier of the intermediate transliterator script
	private String translScript;
	// The cascade files of the transliterators for each script that is not translScript
	private Map<String, Pair<String, String>> translits;
	// The cascade file for the phonetic transliterator
	private String transcr;
	// The settings for the web interface
	private PageSettings pageSettings;
	
	public GlosserSettings(String filepath, ServletContext servletContext) throws GlosserSettingsException {
		try (BufferedReader read = new BufferedReader(new InputStreamReader(
				servletContext.getResourceAsStream(filepath), "UTF-8"))) {
			String folderpath = filepath.substring(0, filepath.lastIndexOf('/')+1);
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
							helpPath = folderpath + fields[1];
							break;
						case "#aboutpage":
							aboutPath = folderpath + fields[1];
							break;
						case "#glossclass":
							glosser = fields[1];
							break;
						case "#dict":
							dictPath = folderpath + fields[1];
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
							transcr = folderpath + fields[1];
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
						translits.put(fields[1], Pair.of(folderpath + fields[2], folderpath + fields[3]));
					}
					pageSettings = new PageSettings(langName, scripts, inScripts, outScripts,
							PageSettings.parseTextPage(helpPath, servletContext), PageSettings.parseTextPage(aboutPath, servletContext));
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
	
	public PageSettings getPageSettings() {
		return pageSettings;
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
	
	public String getDictScript() {
		return dictScript;
	}
	
	public String getTranslScript() {
		return translScript;
	}
	
	public String getScriptName(String script) {
		return scripts.get(script);
	}

	/**
	 * @param servletContext Servlet Context
	 * @return A glosser of the specified class
	 */
	public LanguageGlosser getGlosser(ServletContext servletContext) {
		try {
			Class<?> glossClass = Class.forName(glosser);
			if (LanguageGlosser.class.isAssignableFrom(glossClass)) {
				Constructor<?> constructor = glossClass.getConstructor(UnfoldedDictionary.class, GlossTransliterator.class);
				UnfoldedDictionary dict = new UnfoldedDictionary(new BufferedReader(new InputStreamReader(
						servletContext.getResourceAsStream(dictPath), "UTF-8")), dictScript);
				return (LanguageGlosser) constructor.newInstance(dict, getTransliterator(servletContext));
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param servletContext Servlet Context
	 * @return A transliterator for the specified scripts
	 */
	public GlossTransliterator getTransliterator(ServletContext servletContext) {
		return new GlossTransliterator(translits, transcr, translScript, servletContext);
	}
}
