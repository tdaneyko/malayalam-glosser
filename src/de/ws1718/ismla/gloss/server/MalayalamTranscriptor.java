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

public class MalayalamTranscriptor {
	
	private MalayalamFormat defFrom;
	private MalayalamFormat defTo;

	private MalayalamScriptToUniISOTransliterator script2uni;
	private MalayalamAsciiISOToUniISOTransliterator ascii2uni;
	private MalayalamMozhiToUniISOTransliterator mozhi2uni;

	private MalayalamUniISOToScriptTransliterator uni2script;
	private MalayalamUniISOToAsciiISOTransliterator uni2ascii;
	private MalayalamUniISOToMozhiTransliterator uni2mozhi;
	
	private MalayalamUniISOToIPATransliterator uni2ipa;
	
	
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
	
	public void setFormats(MalayalamFormat from, MalayalamFormat to) {
		defFrom = from;
		defTo = to;
	}
	
	public String convert(String s) {
		return convertBetween(s, defFrom, defTo);
	}
	
	public String convertFrom(String s, MalayalamFormat from) {
		return convertBetween(s, from, defTo);
	}
	
	public String convertTo(String s, MalayalamFormat to) {
		return convertBetween(s, defFrom, to);
	}

	public String convertBetween(String s, MalayalamFormat from, MalayalamFormat to) {
		//System.err.println(s + " in: " + from + " out: " + to);
		if (from.equals(to))
			return s;
		if (from.equals(MalayalamFormat.ISO15919_UNICODE)) {
			if (to.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return uni2script.convert(s);
			if (to.equals(MalayalamFormat.ISO15919_ASCII))
				return uni2ascii.convert(s);
			if (to.equals(MalayalamFormat.MOZHI))
				return uni2mozhi.convert(s);
		}
		if (to.equals(MalayalamFormat.ISO15919_UNICODE)) {
			if (from.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return script2uni.convert(s);
			if (from.equals(MalayalamFormat.ISO15919_ASCII))
				return ascii2uni.convert(s);
			if (from.equals(MalayalamFormat.MOZHI))
				return mozhi2uni.convert(s);
		}
		if (from.equals(MalayalamFormat.ISO15919_ASCII)) {
			if (to.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return uni2script.convert(ascii2uni.convert(s));
			if (to.equals(MalayalamFormat.MOZHI))
				return uni2mozhi.convert(ascii2uni.convert(s));
		}
		if (to.equals(MalayalamFormat.ISO15919_ASCII)) {
			if (from.equals(MalayalamFormat.MALAYALAM_SCRIPT))
				return uni2ascii.convert(script2uni.convert(s));
			if (from.equals(MalayalamFormat.MOZHI))
				return uni2ascii.convert(mozhi2uni.convert(s));
		}
		return s + "<unconverted>";
	}
	
	public String transcribe(String s) {
		return transcribe(s, defFrom);
	}

	public String transcribe(String s, MalayalamFormat format) {
		return uni2ipa.convert(convertBetween(s, format, MalayalamFormat.ISO15919_UNICODE));
	}
}
