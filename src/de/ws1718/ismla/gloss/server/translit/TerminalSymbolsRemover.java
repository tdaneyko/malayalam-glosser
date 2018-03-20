package de.ws1718.ismla.gloss.server.translit;

/**
 * @author Johannes Dellert
 */
public class TerminalSymbolsRemover extends Transliterator
{
	@Override
	public String convert(String str) 
	{
		return str.replace("#","");
	}
}
