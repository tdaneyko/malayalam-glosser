package de.ws1718.ismla.gloss.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.google.gwt.core.shared.GwtIncompatible;

/**
 * This class stores language-specific settings for the web interface.
 */
public class PageSettings implements Serializable {

	// Name of the language
	private String langName;
	// Identifiers and names of the supported scripts
	private Map<String, String> scripts;
	// Identifiers of the supported input scripts
	private String[] inScripts;
	// Identifiers of the supported gloss scripts
	private String[] outScripts;
	// Contents of the Help page
	private List<TextPageContents> helpPage;
	// Contents of the About page
	private List<TextPageContents> aboutPage;
	
	public PageSettings() {
		langName = "Language";
		scripts = new HashMap<>();
		inScripts = new String[0];
		outScripts = new String[0];
		helpPage = new ArrayList<>();
		aboutPage = new ArrayList<>();
	}
	
	public PageSettings(String langName, Map<String, String> scripts, String[] inScripts, String[] outScripts,
			List<TextPageContents> helpPage, List<TextPageContents> aboutPage) {
		this.langName = langName;
		this.scripts = scripts;
		this.inScripts = inScripts;
		this.outScripts = outScripts;
		this.helpPage = helpPage;
		this.aboutPage = aboutPage;
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
	
	public List<TextPageContents> getHelpPage() {
		return new ArrayList<>(helpPage);
	}
	
	public List<TextPageContents> getAboutPage() {
		return new ArrayList<>(aboutPage);
	}
	
	/**
	 * Parse a Help or About page definition.
	 * @param filepath Path to the page file
	 * @param servletContext Servlet Context
	 * @return The contents of the page
	 */
	@GwtIncompatible
	public static List<TextPageContents> parseTextPage(String filepath, ServletContext servletContext) {
		List<TextPageContents> page = new ArrayList<>();
		if (filepath.charAt(0) != '/') filepath = '/' + filepath;
		String folderpath = filepath.substring(0, filepath.lastIndexOf('/')+1);
		try (BufferedReader read = new BufferedReader(new InputStreamReader(servletContext.getResourceAsStream(filepath), "UTF-8"))) {
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				if (!line.isEmpty()) {
					if (line.startsWith("##"))
						page.add(new ParagraphHeader(line.substring(2).trim()));
					else if (line.startsWith("#table")) {
						boolean equalWidth = line.startsWith("#true", 6);
						int i = line.indexOf('#', 7);
						int j = line.indexOf('#', i+1);
						if (i >= 0 && j >= 0) {
							String tablepath = folderpath + line.substring(i+1, j);
							page.add(TableContents.readTable(tablepath, equalWidth, servletContext));
						}
						else
							page.add(new Paragraph(line));
					}
					else
						page.add(new Paragraph(line));
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return page;
	}

}
