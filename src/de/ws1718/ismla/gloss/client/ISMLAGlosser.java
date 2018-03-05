package de.ws1718.ismla.gloss.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Legend;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.StringRadioGroup;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Text;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ISMLAGlosser implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Gloss service.
	 */
	private GlossServiceAsync glossService = GWT.create(GlossService.class);
	
	private List<GlossedWord> gloss = new ArrayList<>();
	private ListBox[] splits = new ListBox[0];
	private ListBox[] transl = new ListBox[0];
	
	private FieldSet glossPage = null;
	private FieldSet finGlossPage = null;
	
	private class GlossCallBack implements AsyncCallback<List<GlossedWord>> {

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Unable to obtain server response: " + caught.getMessage());
		}

		@Override
		public void onSuccess(List<GlossedWord> result) {
			gloss = result;
			reloadGloss();
		}
		
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		loadMainPage();
	}
	
	private void loadMainPage() {
		Form form = new Form();
		FieldSet fset = new FieldSet();
		FormGroup textGroup = new FormGroup();
		FormGroup buttonGroup = new FormGroup();
		
		Legend inputHeader = new Legend("Your input");
		inputHeader.addStyleName("col-lg-8");
		inputHeader.addStyleName("col-sm-12");
		
		final TextArea inputText = new TextArea();
		inputText.setVisibleLines(12);
		inputText.setPlaceholder("Enter your Malayalam text here");
		FlowPanel textPanel = new FlowPanel();
		textPanel.addStyleName("col-lg-6");
		textPanel.addStyleName("col-sm-12");
		textPanel.add(inputText);
		
		RadioButton bi1 = new RadioButton("Malayalam script");
		bi1.setText("Malayalam script");
		RadioButton bi2 = new RadioButton("Mozhi romanization");
		bi2.setText("Mozhi romanization");
		RadioButton bi3 = new RadioButton("ISO-15919 romanization");
		bi3.setText("ISO-15919 romanization");
		StringRadioGroup inFormat = new StringRadioGroup("Input format");
		inFormat.add(bi1);
		inFormat.add(bi2);
		inFormat.add(bi3);
		FormLabel labelIn = new FormLabel();
		labelIn.setFor("inFormat");
		labelIn.setText("Input script:");
		
		RadioButton bo1 = new RadioButton("Mozhi romanization");
		bo1.setText("Mozhi romanization");
		RadioButton bo2 = new RadioButton("ISO-15919 romanization");
		bo2.setText("ISO-15919 romanization");
		StringRadioGroup outFormat = new StringRadioGroup("Output format");
		outFormat.add(bo1);
		outFormat.add(bo2);
		FormLabel labelOut = new FormLabel();
		labelOut.setFor("outFormat");
		labelOut.setText("Gloss script:");
		
		FlowPanel inOutFormatPanel = new FlowPanel();
		inOutFormatPanel.addStyleName("col-lg-2");
		inOutFormatPanel.add(labelIn);
		inOutFormatPanel.add(inFormat);
		inOutFormatPanel.add(labelOut);
		inOutFormatPanel.add(outFormat);
		
		textGroup.add(textPanel);
		textGroup.add(inOutFormatPanel);
		
		class GlossSubmissionHandler implements ClickHandler {
			@Override
			public void onClick(ClickEvent event) {
				glossService.getGloss(inputText.getText(), new GlossCallBack());
			}
		}
		
		Button submit = new Button("Gloss!");
		submit.setType(ButtonType.PRIMARY);
		submit.addClickHandler(new GlossSubmissionHandler());
		buttonGroup.add(submit);
		buttonGroup.addStyleName("col-lg-8");
		buttonGroup.addStyleName("col-sm-12");

		form.addStyleName("col-lg-offset-2");
		
		fset.add(inputHeader);
		fset.add(textGroup);
		fset.add(buttonGroup);
		
		form.add(fset);
		
		RootPanel.get().add(form);
		
		GlossedWord maanga = new GlossedWord(
				"മാങ്ങ",
				"maːŋːa",
				new String[]{"māṅṅa"},
				new String[][]{new String[]{"mango","mango.ACC","mango.NOM"}});
		GlossedWord vaangikkunna = new GlossedWord(
				"വാങ്ങിക്കുന്ന",
				"vaːŋːikʲːun̪ːa",
				new String[]{"vāṅṅikk-unn-a"},
				new String[][]{new String[]{"buy-PRS-A"}});
		GlossedWord payyan = new GlossedWord(
				"പയ്യൻ",
				"pajːan",
				new String[]{"payyan"},
				new String[][]{new String[]{"boy","boy.ACC","boy.NOM"}});
		GlossedWord cantayil = new GlossedWord(
				"ചന്തയിൽ",
				"t͡ɕan̪d̪ajil",
				new String[]{"canta-y-il"},
				new String[][]{new String[]{"market-0-LOC"}});
		GlossedWord aaNu = new GlossedWord(
				"ആണ്",
				"aːɳɨ̆",
				new String[]{"āṇ˘"},
				new String[][]{new String[]{"be"}});
		GlossedWord dot = new GlossedWord(
				".",
				"ǀ",
				new String[]{"."},
				new String[][]{new String[]{"."}});
		
		gloss = Arrays.asList(new GlossedWord[]{maanga, vaangikkunna, payyan, cantayil, aaNu, dot});
		reloadGloss();
	}
	
	private FlowPanel createGlossPanel(boolean finished) {
		FieldSet fset = new FieldSet();
		fset.addStyleName("col-lg-offset-2");
		
		Legend glossHeader = new Legend((finished) ? "Finished Gloss" : "Gloss");
		glossHeader.addStyleName("col-lg-8");
		glossHeader.addStyleName("col-sm-12");
		fset.add(glossHeader);
		
		FlowPanel glossPanel = new FlowPanel();
		glossPanel.addStyleName("col-lg-8");
		glossPanel.addStyleName("col-sm-12");
		fset.add(glossPanel);

		if (!finished) {
			class GlossFinishingHandler implements ClickHandler {
				@Override
				public void onClick(ClickEvent event) {
					finishGloss();
				}
			}
			
			FlowPanel buttonPanel = new FlowPanel();
			Button submit = new Button("Finish!");
			submit.setType(ButtonType.PRIMARY);
			submit.addClickHandler(new GlossFinishingHandler());
			buttonPanel.add(submit);
			buttonPanel.addStyleName("col-lg-8");
			buttonPanel.addStyleName("col-sm-12");
			fset.add(buttonPanel);
		}

		resetGloss(fset, finished);
		
		return glossPanel;
	}
	
	private void resetGloss(FieldSet newGloss, boolean finished) {
		RootPanel root = RootPanel.get();
		if (!finished) {
			if (glossPage != null)
				root.remove(glossPage);
			glossPage = newGloss;
			if (glossPage != null)
				root.add(glossPage);
		}
		if (finished) {
			if (finGlossPage != null)
				root.remove(finGlossPage);
			finGlossPage = newGloss;
			if (finGlossPage != null)
				root.add(finGlossPage);
		}
	}
	
	private void reloadGloss() {
		resetGloss(null, true);
		FlowPanel glossPanel = createGlossPanel(false);
		this.splits = new ListBox[gloss.size()];
		this.transl = new ListBox[gloss.size()];
		for (int i = 0; i < gloss.size(); i++) {
			GlossedWord word = gloss.get(i);
			String[] splits = word.getSplits();
			glossPanel.add(getEditableGloss(word.getOrig(), word.getIpa(), splits, word.getGlosses(splits[0]), i));
		}
	}
	
	private void finishGloss() {
		FlowPanel glossPanel = createGlossPanel(true);
		
		for (int i = 0; i < gloss.size(); i++) {
			glossPanel.add(getFinishedGloss(gloss.get(i).getOrig(), gloss.get(i).getIpa(),
					splits[i].getSelectedValue(), transl[i].getSelectedValue()));
		}
	}
	
	private VerticalPanel getEditableGloss(String orig, String ipa, String[] splits, String[] glosses, int i) {
		ListBox splitBox = getListBox(splits);
		ListBox glossBox = getListBox(glosses);
		this.splits[i] = splitBox;
		this.transl[i] = glossBox;
		
		VerticalPanel g = new VerticalPanel();
		g.add(new Text(orig));
		g.add(new Text(ipa));
		g.add(splitBox);
		g.add(glossBox);
		g.setStyleName("glossPanel");
		return g;
	}
	
	private ListBox getListBox(String[] items) {
		ListBox box = new ListBox();
		for (String item : items)
			box.addItem(item);
		if (items.length == 1)
			box.setEnabled(false);
		return box;
	}
	
	private VerticalPanel getFinishedGloss(String orig, String ipa, String split, String gloss) {
		VerticalPanel g = new VerticalPanel();
		g.add(new Text(orig));
		g.add(new Text(ipa));
		g.add(new Text(split));
		g.add(new Text(gloss));
		g.setStyleName("glossPanel");
		return g;
	}
}
