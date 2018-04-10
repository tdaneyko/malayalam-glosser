package de.ws1718.ismla.gloss.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.ws1718.ismla.gloss.shared.PageSettings;

public interface GlossServiceAsync {
	
	void getGloss(String text, String inFormat, String outFormat, AsyncCallback<List<GlossedSentence>> callback);

	void getSettings(AsyncCallback<PageSettings> callback);

}
