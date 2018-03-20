package de.ws1718.ismla.gloss.server.translit;

import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.ServletContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Johannes Dellert
 * 
 * I removed the main method and added the ServletContext to the constructor.
 */
public class MalayalamScriptToUniISOTransliterator extends Transliterator
{
	
 ArrayList<Transliterator> translits;
 
 boolean verbose = false;
 
 public MalayalamScriptToUniISOTransliterator(boolean verbose, ServletContext servletContext)
 {
	 this.verbose = verbose;
  	translits = new ArrayList<Transliterator>();

  	translits.add(new TerminalSymbolsAdder());
	translits.add(new SimpleTransliterator(servletContext.getResourceAsStream("/lowercase"), false));
	translits.add(new SimpleTransliterator(servletContext.getResourceAsStream("/mal-preprocessing"), false));
	translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream("/mal-orth2prnc-cons"), false));
	translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream("/mal-orth2prnc-vow"), false));
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
