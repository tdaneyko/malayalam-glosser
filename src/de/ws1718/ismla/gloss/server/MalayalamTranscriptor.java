package de.ws1718.ismla.gloss.server;

import javax.servlet.ServletContext;

import de.ws1718.ismla.gloss.server.translit.MalayalamAsciiISOToUniISOTransliterator;
import de.ws1718.ismla.gloss.server.translit.MalayalamMozhiToUniISOTransliterator;
import de.ws1718.ismla.gloss.server.translit.MalayalamScriptToUniISOTransliterator;
import de.ws1718.ismla.gloss.server.translit.MalayalamUniISOToAsciiISOTransliterator;
import de.ws1718.ismla.gloss.server.translit.MalayalamUniISOToIPATransliterator;
import de.ws1718.ismla.gloss.server.translit.MalayalamUniISOToMozhiTransliterator;
import de.ws1718.ismla.gloss.server.translit.MalayalamUniISOToScriptTransliterator;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

/**
 * A transliterator for Malayalam that can convert between different Malayalam scripts
 * and provide IPA transcription for Malayalam.
 */
public class MalayalamTranscriptor {
	
	// The default input script
	private MalayalamFormat defFrom;
	// The default output script
	private MalayalamFormat defTo;

	// Transliterators to Unicode ISO-15919
	private MalayalamScriptToUniISOTransliterator script2uni;
	private MalayalamAsciiISOToUniISOTransliterator ascii2uni;
	private MalayalamMozhiToUniISOTransliterator mozhi2uni;

	// Transliterators from Unicode ISO-15919
	private MalayalamUniISOToScriptTransliterator uni2script;
	private MalayalamUniISOToAsciiISOTransliterator uni2ascii;
	private MalayalamUniISOToMozhiTransliterator uni2mozhi;
	
	// IPA transcriptor from Unicode ISO-15919
	private MalayalamUniISOToIPATransliterator uni2ipa;
	
	/**
	 * Load transliterators for this transcriptor
	 * @param servletContext The servlet context to load the resources from
	 */
	public MalayalamTranscriptor(ServletContext servletContext) {
		defFrom = MalayalamFormat.ISO15919_UNICODE;
		defTo = MalayalamFormat.ISO15919_UNICODE;
		
		script2uni = new MalayalamScriptToUniISOTransliterator(false, servletContext);
		ascii2uni = new MalayalamAsciiISOToUniISOTransliterator(false, servletContext);
		mozhi2uni = new MalayalamMozhiToUniISOTransliterator(false, servletContext);

		uni2script = new MalayalamUniISOToScriptTransliterator(false, servletContext);
		uni2ascii = new MalayalamUniISOToAsciiISOTransliterator(false, servletContext);
		uni2mozhi = new MalayalamUniISOToMozhiTransliterator(false, servletContext);
		
		uni2ipa = new MalayalamUniISOToIPATransliterator(false, servletContext);
	}
	
	/**
	 * Change the default input and output scripts
	 * @param from Input script
	 * @param to Output script
	 */
	public void setFormats(MalayalamFormat from, MalayalamFormat to) {
		defFrom = from;
		defTo = to;
	}
	
	/**
	 * @return The default input script
	 */
	public MalayalamFormat getDefaultInputFormat() {
		return defFrom;
	}
	
	/**
	 * @return The default output script
	 */
	public MalayalamFormat getDefaultOutputFormat() {
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
	public String convertFrom(String s, MalayalamFormat from) {
		return convertBetween(s, from, defTo);
	}

	/**
	 * Convert between the default input script and a specified output script
	 * @param s A string in the default input script
	 * @param to The script to convert s to
	 * @return The string in the specified output script
	 */
	public String convertTo(String s, MalayalamFormat to) {
		return convertBetween(s, defFrom, to);
	}

	/**
	 * Convert between a specified input and output script
	 * @param s A string in the specified input script
	 * @param from The script in which s is written
	 * @param to The script to convert s to
	 * @return The string in the specified output script
	 */
	public String convertBetween(String s, MalayalamFormat from, MalayalamFormat to) {
		// If input == output script: Do nothing
		if (from.equals(to))
			return s;
		
		// Convert from Unicode ISO-15919
		if (from.equals(MalayalamFormat.ISO15919_UNICODE)) {
			if (to.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return uni2script.convert(s);
			if (to.equals(MalayalamFormat.ISO15919_ASCII))
				return uni2ascii.convert(s);
			if (to.equals(MalayalamFormat.MOZHI))
				return uni2mozhi.convert(s);
		}

		// Convert to Unicode ISO-15919
		if (to.equals(MalayalamFormat.ISO15919_UNICODE)) {
			if (from.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return script2uni.convert(s);
			if (from.equals(MalayalamFormat.ISO15919_ASCII))
				return ascii2uni.convert(s);
			if (from.equals(MalayalamFormat.MOZHI))
				return mozhi2uni.convert(s);
		}

		// Convert from ASCII ISO-15919
		if (from.equals(MalayalamFormat.ISO15919_ASCII)) {
			if (to.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return uni2script.convert(ascii2uni.convert(s));
			if (to.equals(MalayalamFormat.MOZHI))
				return uni2mozhi.convert(ascii2uni.convert(s));
		}

		// Convert to ASCII ISO-15919
		if (to.equals(MalayalamFormat.ISO15919_ASCII)) {
			if (from.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return uni2ascii.convert(script2uni.convert(s));
			if (from.equals(MalayalamFormat.MOZHI))
				return uni2ascii.convert(mozhi2uni.convert(s));
		}
		
		// Unknown formats
		return s + "<unconverted>";
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
	public String transcribe(String s, MalayalamFormat from) {
		return uni2ipa.convert(convertBetween(s, from, MalayalamFormat.ISO15919_UNICODE));
	}
}
