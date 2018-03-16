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

public class TableProviderServiceImpl extends RemoteServiceServlet implements TableProviderService {
	
	TableContents transcr;
	TableContents abbr;
	
	@Override
	public void init() throws ServletException {
		transcr = readTable(TRANSCR_TABLE);
		abbr = readTable(ABBR_TABLE);
	}
	
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

	@Override
	public TableContents getTable(String filepath) {
		if (filepath.equals(TRANSCR_TABLE))
			return transcr;
		if (filepath.equals(ABBR_TABLE))
			return abbr;
		return new TableContents();
	}

}
