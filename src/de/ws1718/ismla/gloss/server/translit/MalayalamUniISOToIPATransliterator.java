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
public class MalayalamUniISOToIPATransliterator extends Transliterator
{
	
 ArrayList<Transliterator> translits;
 
 boolean verbose = false;
 
 public MalayalamUniISOToIPATransliterator(boolean verbose, ServletContext servletContext)
 {
	 this.verbose = verbose;
  	translits = new ArrayList<Transliterator>();
  	
  	translits.add(new TerminalSymbolsAdder());
	translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream("/mal-prnc2xsampa"), false));
	translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream("/mal-assimilation-gemination"), false));
	translits.add(new ClassContextTransliterator(servletContext.getResourceAsStream("/mal-assimilation2"), false));
	translits.add(new SimpleTransliterator(servletContext.getResourceAsStream("/xsampa2ipa"), false));
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
