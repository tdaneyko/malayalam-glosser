package de.ws1718.ismla.gloss.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a table with a column header.
 */
public class TableContents implements Serializable {
	
	private String[] header;
	private List<String[]> rows;
	
	public TableContents() {
		this.header = new String[0];
		this.rows = new ArrayList<>();
	}
	
	public TableContents(String[] header, List<String[]> rows) {
		this.header = header;
		this.rows = rows;
	}
	
	public String[] getHeader() {
		return header;
	}
	
	public List<String[]> getRows() {
		return rows;
	}
	
	public int cols() {
		return header.length;
	}
	
	public int rows() {
		return rows.size();
	}
}
