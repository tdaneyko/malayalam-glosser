package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.gloss.client.FileReaderService;

public class FileReaderServiceImpl extends RemoteServiceServlet implements FileReaderService {

	@Override
	public List<String[]> getLines(String filepath) {
		List<String[]> lines = new ArrayList<>();
		try (BufferedReader read = new BufferedReader(new InputStreamReader(getServletContext().getResourceAsStream(filepath), "UTF-8"))) {
			for (String line = read.readLine(); line != null; line = read.readLine()) {
				if (!line.isEmpty())
					lines.add(line.split("\t"));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

}
