package de.ws1718.ismla.gloss.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;

public class ReverseTrie implements Serializable {
	
	private TrieState start;
	
	public ReverseTrie(Map<String, Set<Gloss>> glosses) {
		start = new TrieState();
		for (String form : glosses.keySet()) {
			for (Gloss gloss : glosses.get(form)) {
				start.add(form, form.length()-1, gloss);
			}
		}
	}
	
	public boolean contains(String s) {
		return start.contains(s, s.length()-1);
	}
	
	public List<Gloss> get(String s) {
		return start.get(s, s.length()-1);
	}
	
	public int suffixSearch(String s) {
		return start.suffixSearch(s, s.length()-1);
	}
	
	private class TrieState implements Serializable {
		
		private TCharList chars;
		private List<TrieState> nextStates;
		private List<Gloss> splits;
		
		public TrieState() {
			this.chars = new TCharArrayList();
			this.nextStates = new ArrayList<>();
			this.splits = null;
		}
		
		public TrieState(String s, int i, Gloss g) {
			this();
			add(s, i, g);
		}
		
		public void add(String s, int i, Gloss g) {
			if (i < 0) {
				if (splits == null)
					splits = new ArrayList<>();
				splits.add(g);
			}
			else {
				char c = s.charAt(i);
				int j = chars.binarySearch(c);
				if (j < 0) {
					j = -j - 1;
					chars.insert(j, c);
					nextStates.add(j, new TrieState(s, --i, g));
				}
				else 
					nextStates.get(j).add(s, --i, g);
			}
		}
		
		public boolean contains(String s, int i) {
			if (i < 0)
				return splits != null;
			else {
				int j = chars.binarySearch(s.charAt(i));
				return j >= 0 && nextStates.get(j) != null && nextStates.get(j).contains(s, --i);
			}
		}
		
		public List<Gloss> get(String s, int i) {
			if (i < 0)
				return (splits == null) ? null : new ArrayList<>(splits);
			else {
				int j = chars.binarySearch(s.charAt(i));
				if (j < 0 || nextStates.get(j) == null)
					return null;
				return nextStates.get(j).get(s, --i);
			}
		}
		
		public int suffixSearch(String s, int i) {
			if (i < 0)
				return 0;
			else {
				int j = chars.binarySearch(s.charAt(i));
				if (j < 0 || nextStates.get(j) == null)
					return (splits == null) ? -1 : i+1;
				else {
					int k = nextStates.get(j).suffixSearch(s, i-1);
					if (k < 0 && splits != null)
						return i+1;
					return k;
				} 
			}
		}
	}
	
}
