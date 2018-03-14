package de.ws1718.ismla.gloss.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TableProviderServiceAsync {

	void getTable(String filepath, AsyncCallback<TableContents> callback);

}
