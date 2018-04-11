package de.ws1718.ismla.gloss.server.translit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Johannes Dellert
 */
public class ClassContextTransliterator extends Transliterator
{
	List<ReplacementRule> rules;
	HashMap<String, Set<String>> classes;
	boolean verbose = false;

	public ClassContextTransliterator(InputStream inputStream, boolean verbose)
	{
		rules = new ArrayList<ReplacementRule>();
		classes = new HashMap<String, Set<String>>();
		this.verbose = verbose;

		try (Scanner in = new Scanner(new InputStreamReader(inputStream, "UTF-8"))) {
			String line;
			int lineNumber = 1;
			while (in.hasNext())
			{
				line = in.nextLine();

				//ignore comment lines and empty lines
				if (line.startsWith("//") || "".equals(line)) continue;

				//recognize definition lines (starting with #def)
				if (line.startsWith("#def"))
				{
					String[] tokens = line.split("\t");
					if (tokens.length == 3)
					{
						String symbol = tokens[1];
						Set<String> classSet = parseSet(tokens[2]);
						classes.put(symbol, classSet);
					} 
					else
					{
						System.err.println("WARNING: invalid definition on line " + lineNumber + " of mapping file " + inputStream + ":");
						System.err.println("  " + line);
					}
				}
				else
				{
					String[] tokens = line.split("\t");
					if (line.endsWith("\t"))
					{
						rules.add(new ReplacementRule(parsePattern(tokens[0]), parseSequence("")));
					}
					else if (tokens.length == 2)
					{
						//System.out.println("mapping: " + tokens[0] + " -> " + tokens[1]);
						rules.add(new ReplacementRule(parsePattern(tokens[0]), parseSequence(tokens[1])));
					}
					else
					{
						//System.err.println("WARNING: line " + lineNumber + " of mapping file " + inputStream + " could not be interpreted!");
						//System.err.println(Arrays.toString(tokens));
					}
				}
				lineNumber++;
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private Set<String> parseSet(String setString)
	{
		Set<String> set = new TreeSet<String>();
		if (setString.startsWith("[") && setString.endsWith("]"))
		{
			String[] tokens = setString.substring(1,setString.length() - 1).split(" ");
			for (String token : tokens)
			{
				set.add(token);
			}
		}
		else
		{
			System.err.println("WARNING: Invalid set definition: " + setString);
		}
		return set;
	}


	private List<Set<String>> parsePattern(String expression)
	{
		List<Set<String>> sets = new ArrayList<Set<String>>();
		while (expression.length() > 0)
		{
			Set<String> classForSymbol = new HashSet<String>();
			if (expression.startsWith("["))
			{
				int closePos = expression.indexOf(']');
				if (closePos == -1)
				{
					System.err.println("ERROR: No closing bracket found in subexpression " + expression + ", returning incomplete pattern");
					return sets;
				}
				String symbol = expression.substring(1,closePos);
				classForSymbol = classes.get(symbol);
				if (classForSymbol == null)
				{
					if (symbol.indexOf(' ') != -1)
					{
						classForSymbol = parseSet("[" + symbol + "]");
					}
					else
					{
						System.err.println("WARNING: Unknown set definition: " + symbol + ", interpreting it as a literal");
						classForSymbol = new HashSet<String>();
						classForSymbol.add(symbol);
					}
				}
				expression = expression.substring(closePos + 1);
			}
			else
			{
				int nextSymbolPos = expression.indexOf('[');
				if (nextSymbolPos == -1)
				{
					classForSymbol.add(expression);
					expression = "";
				}
				else
				{
					classForSymbol.add(expression.substring(0,nextSymbolPos));
					expression = expression.substring(nextSymbolPos);
				}
			}
			sets.add(classForSymbol);
		}
		return sets;
	}

	private List<String> parseSequence(String expression)
	{
		List<String> elements = new ArrayList<String>();
		while (expression.length() > 0)
		{
			if (expression.startsWith("["))
			{
				int closePos = expression.indexOf(']');
				if (closePos == -1)
				{
					System.err.println("ERROR: No closing bracket found in subexpression " + expression + ", returning incomplete sequence");
					return elements;
				}
				String symbol = expression.substring(1,closePos);
				if (symbol.equals(".") || symbol.equals(""))
				{
					elements.add(symbol);
				}
				else
				{
					System.err.println("WARNING: Invalid symbol [" + symbol + "] on right-hand side, ignored it while parsing sequence");
				}
				expression = expression.substring(closePos + 1);
			}
			else
			{
				int nextSymbolPos = expression.indexOf('[');
				if (nextSymbolPos == -1)
				{
					elements.add(expression);
					expression = "";
				}
				else
				{
					elements.add(expression.substring(0,nextSymbolPos));
					expression = expression.substring(nextSymbolPos);
				}
			}
		}
		return elements;
	}

	public String convert(String str)
	{
		if (verbose) System.out.print("ClassContextTransliterator.convert(" + str + ") = " );
		StringBuilder result = new StringBuilder();
		int startPos = 0;
		while (startPos < str.length())
		{
			int oldStartPos = startPos;
			//TODO: implement indexing structure based on the next symbol
			for (ReplacementRule rule : rules)
			{
				if (verbose) System.err.println("Testing rule: " + rule);
				boolean ruleHasApplied = false;
				int endPos = str.length();
				while (endPos > startPos)
				{
					List<String> match = matchPattern(str.substring(startPos, endPos), rule.lhs);
					if (match != null)
					{
						result.append(produceReplacement(rule.rhs, match));
						startPos = endPos;
						ruleHasApplied = true;
						break;
					}
					else
					{
						endPos--;
					}
				}
				if (ruleHasApplied) break;
			}
			if (startPos == oldStartPos)
			{
				//no pattern has matched, proceed by exactly one character
				result.append(str.charAt(startPos));
				startPos++;
			}
		}
		if (verbose) System.out.println(result.toString());
		return result.toString();
	}

	private List<String> matchPattern(String input, List<Set<String>> pattern)
	{
		List<String> matches = new ArrayList<String>();
		int chunkIndex = 0;
		int startPos = 0;
		int endPos = -1;
		while (chunkIndex < pattern.size())
		{
			endPos = input.length();
			int oldStartPos = startPos;
			while (endPos > startPos)
			{
				String prefix = input.substring(startPos,endPos);
				if (pattern.get(chunkIndex).contains(prefix))
				{
					if (pattern.get(chunkIndex).size() > 1)
					{
						matches.add(prefix);
					}
					startPos = endPos;
					chunkIndex++;
				}
				else
				{
					endPos--;
				}
			}
			//if no progress, the next chunk was not matched!
			if (startPos == oldStartPos) return null;
		}
		//TODO: more efficient treatment of incomplete matches!
		if (startPos != input.length()) return null;
		return matches;
	}

	private String produceReplacement(List<String> rhs, List<String> match)
	{
		String result = "";
		int matchIndex = 0;
		for (String rhsElement : rhs)
		{
			if (".".equals(rhsElement))
			{
				if (match.size() <= matchIndex)
				{
					System.err.println("WARNING: RHS mismatch in replacement rule!");
					System.err.println("         " + match + " " + rhs);
				}
				else
				{
					result += match.get(matchIndex);
				}
				matchIndex++;
			}
			else if ("".equals(rhsElement))
			{
				matchIndex++;
			}
			else
			{
				result += rhsElement;
			}
		}
		return result;
	}

	private class ReplacementRule
	{
		List<Set<String>> lhs;
		List<String> rhs;

		public ReplacementRule(List<Set<String>> lhs, List<String> rhs)
		{
			this.lhs = lhs;
			this.rhs = rhs;
		}

		public String toString()
		{
			return lhs.toString() + " -> " + rhs.toString();
		}
	}
}
