package de.ws1718.ismla.gloss.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.taskdefs.FixCRLF.AddAsisRemove;

import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;

/**
 * A trie that stores strings in reverse order.
 */
public class ReverseTrie implements Serializable {
	
	// The start state
	private TrieNode start;
	
	/**
	 * Initialize the trie to an empty start state
	 */
	public ReverseTrie() {
		start = new TrieNode();
	}
	
	/**
	 * Create a trie corresponding to a hash map dictionary
	 * @param glosses The dictionary data
	 */
	public ReverseTrie(Map<String, Set<MalayalamDictionaryEntry>> glosses) {
		this();
		addAll(glosses);
	}
	
	/**
	 * Add a string to the trie.
	 * @param word A word
	 * @param gloss A gloss of the word
	 */
	public void add(String word, MalayalamDictionaryEntry gloss) {
		start.add(word, word.length()-1, gloss);
	}
	
	/**
	 * Add all entries of a hash map ictionary to the trie.
	 * @param glosses The dictionary data
	 */
	public void addAll(Map<String, Set<MalayalamDictionaryEntry>> glosses) {
		for (String form : glosses.keySet()) {
			for (MalayalamDictionaryEntry gloss : glosses.get(form)) {
				start.add(form, form.length()-1, gloss);
			}
		}
	}
	
	/**
	 * @param s A word
	 * @return True if the word is stored in the trie, false if not
	 */
	public boolean contains(String s) {
		return start.contains(s, s.length()-1);
	}
	
	/**
	 * @param s A word
	 * @return Its glosses
	 */
	public List<MalayalamDictionaryEntry> get(String s) {
		return start.get(s, s.length()-1);
	}
	
	/**
	 * @param s A word
	 * @return The start index of the longest known suffix of the word
	 */
	public int suffixSearch(String s) {
		return start.suffixSearch(s, s.length()-1);
	}
	
	/**
	 * A node in the reverse trie.
	 */
	private class TrieNode implements Serializable {
		// The chars that lead to the next nodes
		private TCharList chars;
		// The next nodes that can be reached from this node
		private List<TrieNode> nextNodes;
		// The glosses for the string ending at this node
		private List<MalayalamDictionaryEntry> splits;
		
		public TrieNode() {
			this.chars = new TCharArrayList();
			this.nextNodes = new ArrayList<>();
			this.splits = null;
		}
		
		public TrieNode(String s, int i, MalayalamDictionaryEntry g) {
			this();
			add(s, i, g);
		}
		
		/**
		 * Add a string to this trie
		 * @param s The string to add
		 * @param i The current index in the string
		 * @param g A gloss for the string
		 */
		public void add(String s, int i, MalayalamDictionaryEntry g) {
			// If the string ends here, add gloss to list of glosses
			if (i < 0) {
				if (splits == null)
					splits = new ArrayList<>();
				splits.add(g);
			}
			// Else hand string to appropriate next node (and create that node if necessary)
			else {
				char c = s.charAt(i);
				int j = chars.binarySearch(c);
				if (j < 0) {
					j = -j - 1;
					chars.insert(j, c);
					nextNodes.add(j, new TrieNode(s, --i, g));
				}
				else 
					nextNodes.get(j).add(s, --i, g);
			}
		}
		
		/**
		 * @param s A string
		 * @param i The current index in that string
		 * @return True if the string is represented in this trie, false if not
		 */
		public boolean contains(String s, int i) {
			// If the string ends here, it is contained if there is a gloss for it
			if (i < 0)
				return splits != null;
			// Else hand string to appropriate next node (if there is any)
			else {
				int j = chars.binarySearch(s.charAt(i));
				return j >= 0 && nextNodes.get(j) != null && nextNodes.get(j).contains(s, --i);
			}
		}
		
		/**
		 * @param s A string
		 * @param i The current index in that string
		 * @return The glosses for that string
		 */
		public List<MalayalamDictionaryEntry> get(String s, int i) {
			// If the string ends here, return glosses saved in this node
			if (i < 0)
				return (splits == null) ? null : new ArrayList<>(splits);
			// Else hand string to appropriate next node (if there is any)
			else {
				int j = chars.binarySearch(s.charAt(i));
				if (j < 0 || nextNodes.get(j) == null)
					return null;
				return nextNodes.get(j).get(s, --i);
			}
		}
		
		/**
		 * @param s A string
		 * @param i The current index in that string
		 * @return The start index of the longest known suffix of the word
		 */
		public int suffixSearch(String s, int i) {
			// If the string ends here, check whether there are any glosses stored in this node
			if (i < 0)
				return (splits == null) ? -1 : 0;
			// Else hand string to appropriate next node (if there is any)
			else {
				int j = chars.binarySearch(s.charAt(i));
				// If there is no matching next node, check whether the current node has any glosses (i.e. contains this string)
				if (j < 0 || nextNodes.get(j) == null)
					return (splits == null) ? -1 : i+1;
				// Else check whether any of the following nodes contains a suffix
				else {
					int k = nextNodes.get(j).suffixSearch(s, i-1);
					if (k < 0 && splits != null)
						return i+1;
					return k;
				} 
			}
		}
	}
	
}
