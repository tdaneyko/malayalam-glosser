package de.ws1718.ismla.gloss.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.ws1718.ismla.gloss.shared.MalayalamFormat;

public interface GlossServiceAsync {
	
	void getGloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat, AsyncCallback<List<GlossedSentence>> callback);

}
