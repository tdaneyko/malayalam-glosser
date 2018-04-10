package de.ws1718.ismla.gloss.shared;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.Legend;

import com.google.gwt.user.client.ui.Widget;

public class ParagraphHeader implements Serializable, TextPageContents {
	
	private String header;
	
	public ParagraphHeader() {
		this("");
	}
	
	public ParagraphHeader(String header) {
		this.header = header;
	}

	@Override
	public Widget getWidget() {
		return new Legend(header);
	}

}
