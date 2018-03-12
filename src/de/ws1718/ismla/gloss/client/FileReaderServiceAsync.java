package de.ws1718.ismla.gloss.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FileReaderServiceAsync {

	void getLines(String filepath, AsyncCallback<List<String[]>> callback);

}
