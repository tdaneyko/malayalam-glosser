package de.ws1718.ismla.gloss.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.ws1718.ismla.gloss.shared.MalayalamFormat;
import javafx.scene.layout.FlowPane;

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
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
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
	
	private static final String MALAYALAM_SCRIPT = "Malayalam script";
	private static final String ISO15919_UNICODE = "ISO-15919 (Unicode)";
	private static final String ISO15919_ASCII = "ISO-15919 (ASCII)";
	private static final String MOZHI = "Mozhi romanization";

	/**
	 * Create a remote service proxy to talk to the server-side Gloss service.
	 */
	private GlossServiceAsync glossService = GWT.create(GlossService.class);
	
	private MalayalamFormat currentInFormat = MalayalamFormat.MALAYALAM_SCRIPT;
	private MalayalamFormat currentOutFormat = MalayalamFormat.ISO15919_UNICODE;
	
	private List<GlossedSentence> gloss = new ArrayList<>();
	private ListBox[][] splits = new ListBox[0][];
	private ListBox[][] transl = new ListBox[0][];
	
	private FieldSet glossPage = null;
	private FieldSet finGlossPage = null;
	
	private class GlossCallBack implements AsyncCallback<List<GlossedSentence>> {

		@Override
		public void onFailure(Throwable caught) {
			Window.alert("Unable to obtain server response: " + caught.getMessage());
		}

		@Override
		public void onSuccess(List<GlossedSentence> result) {
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
		
		ClickHandler inFormatHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RadioButton button = (RadioButton) event.getSource();
				currentInFormat = getFormat(button.getText());
			}
		};
		RadioButton biScript = new RadioButton(MALAYALAM_SCRIPT);
		biScript.setText(MALAYALAM_SCRIPT);
		biScript.setValue(true);
		biScript.addClickHandler(inFormatHandler);
		RadioButton biMozhi = new RadioButton(MOZHI);
		biMozhi.setText(MOZHI);
		biMozhi.addClickHandler(inFormatHandler);
		RadioButton biUni = new RadioButton(ISO15919_UNICODE);
		biUni.setText(ISO15919_UNICODE);
		biUni.addClickHandler(inFormatHandler);
		RadioButton biAscii = new RadioButton(ISO15919_ASCII);
		biAscii.setText(ISO15919_ASCII);
		biAscii.addClickHandler(inFormatHandler);
		StringRadioGroup inFormat = new StringRadioGroup("Input format");
		inFormat.add(biScript);
		inFormat.add(biUni);
		inFormat.add(biAscii);
		inFormat.add(biMozhi);
		inFormat.addStyleName("space-below-sm");
		FormLabel labelIn = new FormLabel();
		labelIn.setFor("inFormat");
		labelIn.setText("Input script:");
		
		ClickHandler outFormatHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RadioButton button = (RadioButton) event.getSource();
				currentOutFormat = getFormat(button.getText());
			}
		};
		RadioButton boUni = new RadioButton(ISO15919_UNICODE);
		boUni.setText(ISO15919_UNICODE);
		boUni.setValue(true);
		boUni.addClickHandler(outFormatHandler);
		RadioButton boMozhi = new RadioButton(MOZHI);
		boMozhi.setText(MOZHI);
		boMozhi.addClickHandler(outFormatHandler);
		StringRadioGroup outFormat = new StringRadioGroup("Output format");
		outFormat.add(boUni);
		outFormat.add(boMozhi);
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
		textPanel.addStyleName("space-below-sm");
		
		class GlossSubmissionHandler implements ClickHandler {
			@Override
			public void onClick(ClickEvent event) {
				glossService.getGloss(inputText.getText(), currentInFormat, currentOutFormat, new GlossCallBack());
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
		
		// മാങ്ങ വാങ്ങിക്കുന്ന പയ്യൻ ചന്തയിൽ ആണ്.
		// māṅṅa vāṅṅikkunna payyan cantayil āṇ˘.
		// maa;n;na vaa;n;nikkunna payyan cantayil aa.n' .
		// maangnga vaangngikkunna payyan canthayil aaN~.
		// മാങ്ങ വാങ്ങിക്കുന്ന പയ്യൻ ചന്തയിലാണ്.
		// പൂച്ച നല്ലയാണ്.
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
		
		gloss = Arrays.asList(
				new GlossedSentence[]{new GlossedSentence("മാങ്ങ വാങ്ങിക്കുന്ന പയ്യൻ ചന്തയിലാണ്.",
						Arrays.asList(new GlossedWord[]{maanga, vaangikkunna, payyan, cantayil, aaNu, dot}))});
		reloadGloss();
	}
	
	private MalayalamFormat getFormat(String text) {
		if (text.equals(MALAYALAM_SCRIPT))
			return MalayalamFormat.MALAYALAM_SCRIPT;
		else if (text.equals(ISO15919_UNICODE))
			return MalayalamFormat.ISO15919_UNICODE;
		else if (text.equals(ISO15919_ASCII))
			return MalayalamFormat.ISO15919_ASCII;
		else if (text.equals(MOZHI))
			return MalayalamFormat.MOZHI;
		return MalayalamFormat.UNKNOWN;
	}
	
	private List<FlowPanel> createGlossPanel(boolean finished) {
		FieldSet fset = new FieldSet();
		fset.addStyleName("col-lg-offset-2");
		
		Legend glossHeader = new Legend((finished) ? "Finished Gloss" : "Gloss");
		glossHeader.addStyleName("col-lg-8");
		glossHeader.addStyleName("col-sm-12");
		fset.add(glossHeader);
		
		List<FlowPanel> glossPanels = new ArrayList<>();
		for (GlossedSentence s : gloss) {
			FlowPanel panelPanel = new FlowPanel();
			panelPanel.addStyleName("col-lg-8");
			panelPanel.addStyleName("col-sm-12");
			
			Panel sentPanel = new Panel();
			PanelHeader origSent = new PanelHeader();
			PanelBody glossedSent = new PanelBody();
			
			origSent.setText("Original sentence: " + s.getSentence());
			
			FlowPanel glossPanel = new FlowPanel();
			glossPanels.add(glossPanel);
			glossedSent.add(glossPanel);
			
			sentPanel.add(origSent);
			sentPanel.add(glossedSent);
			panelPanel.add(sentPanel);
			fset.add(panelPanel);
		}

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
			buttonPanel.addStyleName("space-below-l");
			fset.add(buttonPanel);
		}

		resetGloss(fset, finished);
		
		return glossPanels;
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
		List<FlowPanel> glossPanels = createGlossPanel(false);
		this.splits = new ListBox[gloss.size()][];
		this.transl = new ListBox[gloss.size()][];
		for (int g = 0; g < gloss.size(); g++) {
			List<GlossedWord> glosses = gloss.get(g).getGlosses();
			FlowPanel glossPanel = glossPanels.get(g);
			this.splits[g] = new ListBox[glosses.size()];
			this.transl[g] = new ListBox[glosses.size()];
			for (int i = 0; i < glosses.size(); i++) {
				GlossedWord word = glosses.get(i);
				String[] splits = word.getSplits();
				glossPanel.add(getEditableGloss(word.getOrig(), word.getIpa(), splits, word.getGlosses(splits[0]), g, i));
			}
		}
	}
	
	private void finishGloss() {
		List<FlowPanel> glossPanels = createGlossPanel(true);

		for (int g = 0; g < gloss.size(); g++) {
			List<GlossedWord> glosses = gloss.get(g).getGlosses();
			FlowPanel glossPanel = glossPanels.get(g);
			for (int i = 0; i < glosses.size(); i++) {
				glossPanel.add(getFinishedGloss(glosses.get(i).getOrig(), glosses.get(i).getIpa(),
						splits[g][i].getSelectedValue(), transl[g][i].getSelectedValue()));
			}
		}
	}
	
	private VerticalPanel getEditableGloss(String orig, String ipa, String[] splits, String[] glosses, int i, int j) {
		ListBox splitBox = getListBox(splits);
		ListBox glossBox = getListBox(glosses);
		this.splits[i][j] = splitBox;
		this.transl[i][j] = glossBox;
		
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
