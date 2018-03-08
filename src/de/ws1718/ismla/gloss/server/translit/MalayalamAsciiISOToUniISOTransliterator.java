package de.ws1718.ismla.gloss.server.translit;

import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.ServletContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class MalayalamAsciiISOToUniISOTransliterator implements Transliterator
{
	
 ArrayList<Transliterator> translits;
 
 boolean verbose = false;
 
 public MalayalamAsciiISOToUniISOTransliterator(boolean verbose, ServletContext servletContext)
 {
	 this.verbose = verbose;
  	translits = new ArrayList<Transliterator>();

	translits.add(new SimpleTransliterator(servletContext.getResourceAsStream("/lowercase"), false));
	translits.add(new SimpleTransliterator(servletContext.getResourceAsStream("/mal-asciiISO2unicodeISO"), false));
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
