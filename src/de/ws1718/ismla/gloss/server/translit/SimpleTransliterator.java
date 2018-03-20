package de.ws1718.ismla.gloss.server.translit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Johannes Dellert
 */
public class SimpleTransliterator extends Transliterator
{
	HashMap<String, String> mapping;
	boolean verbose = false;

	public SimpleTransliterator(InputStream inputStream, boolean verbose)
	{
		mapping = new HashMap<String, String>();
		this.verbose = verbose;

		try (Scanner in = new Scanner(new InputStreamReader(inputStream, "UTF-8"))) {
			String line;
			int lineNumber = 1;
			while (in.hasNext())
			{
				line = in.nextLine();
				//ignore comment lines
				if (line.startsWith("//")) continue;

				String[] tokens = line.split("\t");
				if (tokens.length == 2)
				{
					//System.out.println("mapping: " + tokens[0] + " -> " + tokens[1]);
					mapping.put(tokens[0], tokens[1]);
				}
				else if (tokens.length == 1)
				{
					if (line.endsWith("\t"))
					{
						mapping.put(tokens[0], "");
					}
				}
				else
				{
					//System.err.println("WARNING: line " + lineNumber + " of mapping file " + inputStream + " could not be interpreted!");
				}
				lineNumber++;
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String convert(String str)
	{
		if (verbose) System.out.print("SimpleTransliterator.convert(" + str + ") = " );
		StringBuilder result = new StringBuilder();
		int startPos = 0;
		while (startPos < str.length())
		{
			int endPos = str.length();
			String replacement = null;
			while (endPos > startPos)
			{
				String prefix = str.substring(startPos,endPos);
				replacement = mapping.get(prefix);
				if (replacement != null)
				{
					break;
				}
				endPos--;
			}
			if (replacement == null)
			{
				result.append(str.charAt(startPos));
				startPos++;
			}
			else
			{
				result.append(replacement);
				startPos = endPos;
			}
		}
		if (verbose) System.out.println(result.toString());
		return result.toString();
	}
}
