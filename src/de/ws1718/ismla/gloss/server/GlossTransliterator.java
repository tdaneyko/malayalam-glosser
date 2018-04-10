package de.ws1718.ismla.gloss.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.tuple.Pair;

import de.ws1718.ismla.gloss.server.translit.CascadeTransliterator;
import de.ws1718.ismla.gloss.server.translit.NotATransliterator;
import de.ws1718.ismla.gloss.server.translit.Transliterator;

public class GlossTransliterator {

	// The intermediate transliterator script
	public final String translScript;
	// The default input script
	private String defFrom;
	// The default output script
	private String defTo;
	
	private Map<String, Pair<Transliterator, Transliterator>> translits;
	private Transliterator transcr;
	
	public GlossTransliterator(Map<String, Pair<String, String>> cascades, String transcrCsc, String translScript, ServletContext servletContext) {
		this.translScript = translScript;
		this.defFrom = translScript;
		this.defTo = translScript;
		this.translits = new HashMap<>();
		for (String script : cascades.keySet()) {
			Pair<String, String> csc = cascades.get(script);
			System.err.println(csc.getLeft() + " " + csc.getRight());
			CascadeTransliterator lt = new CascadeTransliterator(csc.getLeft(), false, servletContext);
			CascadeTransliterator rt = new CascadeTransliterator(csc.getRight(), false, servletContext);
			translits.put(script, Pair.of(lt, rt));
		}
		this.transcr = (transcrCsc.isEmpty()) ? new NotATransliterator() : new CascadeTransliterator(transcrCsc, false, servletContext);
	}
	
	/**
	 * Change the default input and output scripts
	 * @param from Input script
	 * @param to Output script
	 */
	public void setFormats(String from, String to) {
		defFrom = from;
		defTo = to;
	}
	
	/**
	 * @return The default input script
	 */
	public String getDefaultInputFormat() {
		return defFrom;
	}
	
	/**
	 * @return The default output script
	 */
	public String getDefaultOutputFormat() {
		return defTo;
	}
	
	/**
	 * Convert between the default input and output scripts
	 * @param s A string in the default input script
	 * @return The string in the default output script
	 */
	public String convert(String s) {
		return convertBetween(s, defFrom, defTo);
	}
	
	/**
	 * Convert between a specified input script and the default output script
	 * @param s A string in the specified input script
	 * @param from The script in which s is written
	 * @return The string in the default output script
	 */
	public String convertFrom(String s, String from) {
		return convertBetween(s, from, defTo);
	}

	/**
	 * Convert between the default input script and a specified output script
	 * @param s A string in the default input script
	 * @param to The script to convert s to
	 * @return The string in the specified output script
	 */
	public String convertTo(String s, String to) {
		return convertBetween(s, defFrom, to);
	}

	/**
	 * Convert between a specified input and output script
	 * @param s A string in the specified input script
	 * @param from The script in which s is written
	 * @param to The script to convert s to
	 * @return The string in the specified output script
	 */
	public String convertBetween(String s, String from, String to) {
		if (!from.equals(translScript))
			s = translits.get(from).getLeft().convert(s);

		if (!to.equals(translScript))
			s = translits.get(to).getRight().convert(s);

		return s;
	}
	
	/**
	 * Get the IPA transcription for a string in the default input script
	 * @param s A string in the default input script
	 * @return The IPA transcription of s
	 */
	public String transcribe(String s) {
		return transcribe(s, defFrom);
	}

	/**
	 * Get the IPA transcription for a string in a specified input script
	 * @param s A string in the specified input script
	 * @param from The script in which s is written
	 * @return The IPA transcription of s
	 */
	public String transcribe(String s, String from) {
		return transcr.convert(convertBetween(s, from, translScript));
	}
}
