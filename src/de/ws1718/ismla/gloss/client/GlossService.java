package de.ws1718.ismla.gloss.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.ws1718.ismla.gloss.shared.MalayalamFormat;

/**
 * This class receives the user input and hands it to the actual glosser.
 */
@RemoteServiceRelativePath("gloss")
public interface GlossService extends RemoteService {

	List<GlossedSentence> getGloss(String text, MalayalamFormat inFormat, MalayalamFormat outFormat);

}
