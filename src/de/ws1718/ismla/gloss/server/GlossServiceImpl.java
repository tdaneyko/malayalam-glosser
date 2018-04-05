package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.gloss.client.GlossService;
import de.ws1718.ismla.gloss.client.GlossedSentence;
import de.ws1718.ismla.gloss.client.GlossedWord;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

/**
 * This class receives the user input and hands it to the actual glosser.
 */
public class GlossServiceImpl extends RemoteServiceServlet implements GlossService {
	
	// The path to the fully inflected dictionary file
	private static final String dictPath = "/mal-dict-all.tsv";
	
	// The glosser/tokenizer
	LanguageGlosser glosser;
	
	@Override
	public void init() throws ServletException {
		try {
			ServletContext servletContext = getServletContext();
			MalayalamDictionary dict = new MalayalamDictionary(new BufferedReader(new InputStreamReader(
					servletContext.getResourceAsStream(dictPath), "UTF-8")));
			glosser = new MalayalamGlosser(dict, new MalayalamTranscriptor(servletContext));
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param text A user-input Malayalam text
	 * @param inFormat The script used in the text
	 * @param outFormat The script to be used in glosses
	 * @return The glossed text
	 */
	@Override
	public List<GlossedSentence> getGloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat) {
		return glosser.gloss(text, inFormat, outFormat);
	}
	
}
