package de.ws1718.ismla.gloss.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;

/**
 * A class representing a table with a column header.
 */
public class TableContents implements Serializable, TextPageContents {
	
	private String[] header;
	private List<String[]> rows;
	private boolean equalWidth;
	
	public TableContents() {
		this.equalWidth = false;
		this.header = new String[0];
		this.rows = new ArrayList<>();
	}
	
	public TableContents(String[] header, List<String[]> rows) {
		this(header, rows, false);
	}
	
	public TableContents(String[] header, List<String[]> rows, boolean equalWidth) {
		this.header = header;
		this.rows = rows;
		this.equalWidth = equalWidth;
	}
	
	public String[] getHeader() {
		return header;
	}
	
	public List<String[]> getRows() {
		return rows;
	}
	
	public boolean equalWidth() {
		return equalWidth;
	}
	
	public int cols() {
		return header.length;
	}
	
	public int rows() {
		return rows.size();
	}
	
	public Widget getWidget() {
		return createTable(rows, header, equalWidth);
	}
	
	/**
	 * Parse a tsv file into a TableContents object.
	 * @param path The path to the tsv file
	 * @return The parsed table
	 */
	@GwtIncompatible
	public static TableContents readTable(String path, boolean equalWidth, ServletContext servletContext) {
		boolean firstLine = true;
		List<String[]> rows = new ArrayList<>();
		String[] header = null;
		if (path.charAt(0) != '/') path = '/' + path;
		try (BufferedReader read = new BufferedReader(new InputStreamReader(servletContext.getResourceAsStream(path), "UTF-8"))) {
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				if (!line.isEmpty()) {
					String[] fields = StringUtils.split(line, '\t');
					if (firstLine) {
						header = fields;
						firstLine = false;
					}
					else
						rows.add(fields);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return new TableContents(header, rows, equalWidth);
	}
	
	/**
	 * @param content The rows of a table
	 * @param colHeaders The header of the table columns
	 * @param equalSize True if all columns in the table should be of equal width,
	 * 					false if the width should be dependent on the content
	 * @return A CellTabe widget with the specified contents and settings
	 */
	public static FlowPanel createTable(List<String[]> content, String[] colHeaders, boolean equalSize) {
		FlowPanel tablePanel = new FlowPanel();
		CellTable<String[]> table = new CellTable<>();
		if (equalSize)
			table.setWidth("100%", true);
		double pc = 100 / colHeaders.length;
		
		for (int i = 0; i < colHeaders.length; i++) {
			final int j = i;
			TextColumn<String[]> col = new TextColumn<String[]>() {
				@Override
				public String getValue(String[] line) {
					return line[j];
				}
			};
			table.addColumn(col, colHeaders[i]);
			if (equalSize)
				table.setColumnWidth(col, pc, Unit.PCT);
		}
		
		table.setVisibleRange(0, content.size());
		table.setRowData(0, content);
		tablePanel.add(table);
		
		return tablePanel;
	}
}
