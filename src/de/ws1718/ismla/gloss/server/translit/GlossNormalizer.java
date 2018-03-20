package de.ws1718.ismla.gloss.server.translit;

/**
 * @author Thora Daneyko
 */
public class GlossNormalizer extends Transliterator
{
	@Override
	public String convert(String str) 
	{
		return str.replace('|', '-').replace('&', '.');
	}
}
