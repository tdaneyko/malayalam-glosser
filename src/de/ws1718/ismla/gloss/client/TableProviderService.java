package de.ws1718.ismla.gloss.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * This class loads the tables to be displayed on the help page at launch, so that they don't have
 * to be read over and over again.
 */
@RemoteServiceRelativePath("table")
public interface TableProviderService extends RemoteService {
	
	// The path to the table containing the transcription schemes
	public static final String TRANSCR_TABLE = "/transcription-schemes.tsv";
	// The path to the table containing the gloss abbreviations
	public static final String ABBR_TABLE = "/gloss-abbreviations.tsv";

	TableContents getTable(String filepath);

}
