package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.gloss.client.GlossService;
import de.ws1718.ismla.gloss.client.GlossedWord;

public class GlossServiceImpl extends RemoteServiceServlet implements GlossService {
	
	private static final String dictPath = "/mal-dict-all.tsv";

	@Override
	public List<GlossedWord> getGloss(String input) {
		try {
			MalayalamDictionary dict = new MalayalamDictionary(new BufferedReader(new InputStreamReader(
					getServletContext().getResourceAsStream(dictPath), "UTF-8")));
			MalayalamGlosser glosser = new MalayalamGlosser(dict);
			return glosser.gloss(input);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}
