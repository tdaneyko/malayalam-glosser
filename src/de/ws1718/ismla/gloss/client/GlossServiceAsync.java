package de.ws1718.ismla.gloss.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GlossServiceAsync {
	
	void getGloss(String input, AsyncCallback<List<GlossedWord>> callback);

}
