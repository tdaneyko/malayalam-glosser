package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.gloss.client.GlossService;
import de.ws1718.ismla.gloss.client.GlossedSentence;
import de.ws1718.ismla.gloss.client.GlossedWord;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

public class GlossServiceImpl extends RemoteServiceServlet implements GlossService {
	
	private static final String dictPath = "/mal-dict-all.tsv";

	@Override
	public List<GlossedSentence> getGloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat) {
		try {
			ServletContext servletContext = getServletContext();
			MalayalamDictionary dict = new MalayalamDictionary(new BufferedReader(new InputStreamReader(
					servletContext.getResourceAsStream(dictPath), "UTF-8")));
			MalayalamGlosser glosser = new MalayalamGlosser(dict, new MalayalamTranscriptor(servletContext));
			return glosser.gloss(text, inFormat, outFormat);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}
