package de.ws1718.ismla.gloss.server.translit;

/**
 * @author Johannes Dellert
 */
public class TerminalSymbolsAdder extends Transliterator
{
	@Override
	public String convert(String str) 
	{
		return "#" + str.replace(" ", "# #") + "#";
	}
}
