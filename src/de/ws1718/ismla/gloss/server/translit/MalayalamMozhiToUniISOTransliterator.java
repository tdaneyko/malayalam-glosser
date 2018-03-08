package de.ws1718.ismla.gloss.server.translit;

import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.ServletContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class MalayalamMozhiToUniISOTransliterator implements Transliterator
{
	
 ArrayList<Transliterator> translits;
 
 boolean verbose = false;
 
 public MalayalamMozhiToUniISOTransliterator(boolean verbose, ServletContext servletContext)
 {
	 this.verbose = verbose;
  	translits = new ArrayList<Transliterator>();

  	translits.add(new TerminalSymbolsAdder());
	translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream("/mal-mozhi2unicodeISO-preprocessing"), false));
	translits.add(new SimpleTransliterator(servletContext.getResourceAsStream("/mal-mozhi2unicodeISO"), false));
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
