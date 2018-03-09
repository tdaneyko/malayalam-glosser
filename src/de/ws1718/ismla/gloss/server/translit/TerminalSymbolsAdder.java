package de.ws1718.ismla.gloss.server.translit;

public class TerminalSymbolsAdder implements Transliterator
{
	@Override
	public String convert(String str) 
	{
		return "#" + str.replace(" ", "# #") + "#";
	}
}
