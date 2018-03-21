package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.gloss.client.TableProviderService;
import de.ws1718.ismla.gloss.client.TableContents;
import de.ws1718.ismla.gloss.shared.StringUtils;

/**
 * This class loads the tables to be displayed on the help page at launch, so that they don't have
 * to be read over and over again.
 */
public class TableProviderServiceImpl extends RemoteServiceServlet implements TableProviderService {
	// The table containing the transcription schemes
	TableContents transcr;
	// The table containing the gloss abbreviations
	TableContents abbr;
	
	@Override
	public void init() throws ServletException {
		transcr = readTable(TRANSCR_TABLE);
		abbr = readTable(ABBR_TABLE);
	}
	
	/**
	 * Parse a tsv file into a TableContents object.
	 * @param path The path to the tsv file
	 * @return The parsed table
	 */
	private TableContents readTable(String path) {
		boolean firstLine = true;
		List<String[]> rows = new ArrayList<>();
		String[] header = null;
		try (BufferedReader read = new BufferedReader(new InputStreamReader(getServletContext().getResourceAsStream(path), "UTF-8"))) {
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
		return new TableContents(header, rows);
	}

	/**
	 * @param filepath The path to a tsv file
	 * @return The previously loaded contents of that tsv file
	 */
	@Override
	public TableContents getTable(String filepath) {
		if (filepath.equals(TRANSCR_TABLE))
			return transcr;
		if (filepath.equals(ABBR_TABLE))
			return abbr;
		return new TableContents();
	}

}
