package de.ws1718.ismla.gloss.shared;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;

import com.google.gwt.user.client.ui.Widget;

public class Paragraph implements Serializable, TextPageContents {
	
	private String text;
	
	public Paragraph() {
		this("");
	}
	
	public Paragraph(String text) {
		StringBuilder html = new StringBuilder();
		int i = 0;
		while (i < text.length()) {
			int j = text.indexOf('#', i);
			if (j < 0)
				break;
			else {
				html.append(text, i, j);
				if (text.startsWith("#url", j)) {
					j = j+4;
					int k = text.indexOf('#', j+1);
					if (k >= 0) {
						int l = text.indexOf('#', k+1);
						if (l >= 0) {
							html.append(link(text.substring(k+1, l), text.substring(j+1, k)));
							i = l+1;
							continue;
						}
					}
				}
				html.append('#');
				i = j+1;
			}
		}
		if (i < text.length())
			html.append(text, i, text.length());
		this.text = html.toString();
	}
	
	/**
	 * @param url A URL
	 * @param label The text that should be linked with this URL
	 * @return The HTML code for that link
	 */
	private String link(String url, String label) {
		return "<a  target=\"_blank\" rel=\"noopener noreferrer\" href=\"" + url + "\">" + label + "</a>";
	}

	@Override
	public Widget getWidget() {
		return new HTMLPanel("p", text);
	}

}
