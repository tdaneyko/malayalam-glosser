package de.ws1718.ismla.gloss.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("table")
public interface TableProviderService extends RemoteService {
	
	public static final String TRANSCR_TABLE = "/transcription-schemes.tsv";
	public static final String ABBR_TABLE = "/gloss-abbreviations.tsv";

	TableContents getTable(String filepath);

}
