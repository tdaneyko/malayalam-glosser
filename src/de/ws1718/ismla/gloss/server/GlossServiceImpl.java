package de.ws1718.ismla.gloss.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.gloss.client.GlossService;
import de.ws1718.ismla.gloss.shared.GlossedSentence;
import de.ws1718.ismla.gloss.shared.PageSettings;

/**
 * This class receives the user input and hands it to the actual glosser.
 */
public class GlossServiceImpl extends RemoteServiceServlet implements GlossService {
	
	// The glosser settings
	GlosserSettings settings;
	// The glosser/tokenizer
	LanguageGlosser glosser;
	
	@Override
	public void init() throws ServletException {
		try {
			ServletContext servletContext = getServletContext();
			settings = new GlosserSettings(Config.DEFAULT_LANG, servletContext);
			glosser = settings.getGlosser(servletContext);
		}
		catch (GlosserSettingsException e) {
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
	public List<GlossedSentence> getGloss(String text, String inFormat, String outFormat) {
		return glosser.gloss(text, inFormat, outFormat);
	}

	@Override
	public PageSettings getSettings() {
		return settings.getPageSettings();
	}
	
}
