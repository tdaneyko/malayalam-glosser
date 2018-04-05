package de.ws1718.ismla.gloss.server.translit;

public class NotATransliterator extends Transliterator {
	
	public NotATransliterator() {}

	@Override
	public String convert(String str) {
		return "";
	}

}
