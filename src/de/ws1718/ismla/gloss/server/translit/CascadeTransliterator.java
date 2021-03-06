package de.ws1718.ismla.gloss.server.translit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import de.ws1718.ismla.gloss.shared.StringUtils;

/**
 * A transliterator based on the rule files specified in a cascade file.
 * 
 * Based on the language-specific transliterators written by Johannes Dellert.
 */
public class CascadeTransliterator extends Transliterator {

	ArrayList<Transliterator> translits;
	boolean verbose = false;

	public CascadeTransliterator(String cascadePath, boolean verbose, ServletContext servletContext) {
		this.verbose = verbose;
		translits = new ArrayList<Transliterator>();
		translits.add(new TerminalSymbolsAdder());
		if (cascadePath.charAt(0) != '/') cascadePath = '/' + cascadePath;
		try (BufferedReader read = new BufferedReader(new InputStreamReader(servletContext.getResourceAsStream(cascadePath), "UTF-8"))) {
			String folderpath = cascadePath.substring(0, cascadePath.lastIndexOf('/')+1);
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				if (!line.isEmpty()) {
					String[] fields = StringUtils.split(line, '\t');
					if (fields.length == 2) {
						if (fields[1].equals("greedy"))
							translits.add(new SimpleTransliterator(servletContext.getResourceAsStream(folderpath+fields[0]), false));
						else
							translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream(folderpath+fields[0]), false));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		translits.add(new TerminalSymbolsRemover());
	}

	@Override
	public String convert(String str)
	{
		for (Transliterator translit : translits)
		{
			str = translit.convert(str);
		}
		return str;
	}

	public String trace(String str)
	{
		String output = str;
		for (Transliterator translit : translits)
		{
			str = translit.convert(str);
			output += " => " + str;
		}
		return output;
	}

}
