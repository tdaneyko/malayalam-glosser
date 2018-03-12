package de.ws1718.ismla.gloss.client;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Legend;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.NavbarCollapse;
import org.gwtbootstrap3.client.ui.NavbarCollapseButton;
import org.gwtbootstrap3.client.ui.NavbarHeader;
import org.gwtbootstrap3.client.ui.NavbarNav;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Pre;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.StringRadioGroup;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.ws1718.ismla.gloss.shared.MalayalamFormat;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ISMLAGlosser implements EntryPoint {
	
	private static final String MALAYALAM_SCRIPT = "Malayalam script";
	private static final String ISO15919_UNICODE = "ISO-15919 (Unicode)";
	private static final String ISO15919_ASCII = "ISO-15919 (ASCII)";
	private static final String MOZHI = "Mozhi romanization";
	
	private static final String LG_OFFSET = "col-lg-offset-2";
	private static final String LG_WIDTH = "col-lg-8";
	private static final String SM_WIDTH = "col-sm-12";
	private static final String[] PAGE_STYLES = new String[]{LG_OFFSET,LG_WIDTH,SM_WIDTH};

	private GlossServiceAsync glossService = GWT.create(GlossService.class);
	
	private FlowPanel currentPage;
	
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
		Navbar navbar = new Navbar();
		
		NavbarHeader navHeader = new NavbarHeader();
		NavbarBrand navTitle = new NavbarBrand();
		navTitle.setText("Malayalam Glosser");
		NavbarCollapseButton collapseButton = new NavbarCollapseButton();
		navHeader.add(navTitle);
		navHeader.add(collapseButton);
		navHeader.addStyleName(LG_OFFSET);
		navbar.add(navHeader);
		
		NavbarCollapse navCollapse = new NavbarCollapse();
		collapseButton.setDataTargetWidget(navCollapse);
		NavbarNav navEntries = new NavbarNav();
		final AnchorListItem mainPage = new AnchorListItem("Glosser");
		final AnchorListItem infoPage = new AnchorListItem("Info");
		final AnchorListItem sourcesPage = new AnchorListItem("Sources");
		navEntries.add(mainPage);
		navEntries.add(infoPage);
		navEntries.add(sourcesPage);
		navCollapse.add(navEntries);
		navbar.add(navCollapse);
		
		mainPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainPage.setActive(true);
				infoPage.setActive(false);
				sourcesPage.setActive(false);
				RootPanel.get().remove(currentPage);
				loadMainPage();
			}
		});
		infoPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainPage.setActive(false);
				infoPage.setActive(true);
				sourcesPage.setActive(false);
				RootPanel.get().remove(currentPage);
				loadInfoPage();
			}
		});
		sourcesPage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				mainPage.setActive(false);
				infoPage.setActive(false);
				sourcesPage.setActive(true);
				RootPanel.get().remove(currentPage);
				loadSourcesPage();
			}
		});
		
		RootPanel.get().add(navbar);

		mainPage.setActive(true);
		loadMainPage();
	}
	
	
	private void loadMainPage() {
		FlowPanel mainPanel = new FlowPanel();
		Form form = new Form();
		FieldSet fset = new FieldSet();
		FormGroup textGroup = new FormGroup();
		FormGroup buttonGroup = new FormGroup();
		
		Legend inputHeader = new Legend("Your input");
		inputHeader.addStyleName(LG_WIDTH);
		inputHeader.addStyleName(SM_WIDTH);
		
		final TextArea inputText = new TextArea();
		inputText.setVisibleLines(12);
		inputText.setPlaceholder("Enter your Malayalam text here");
		FlowPanel textPanel = new FlowPanel();
		textPanel.addStyleName("col-lg-6");
		textPanel.addStyleName(SM_WIDTH);
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
		buttonGroup.addStyleName(LG_WIDTH);
		buttonGroup.addStyleName(SM_WIDTH);

		form.addStyleName(LG_OFFSET);
		
		fset.add(inputHeader);
		fset.add(textGroup);
		fset.add(buttonGroup);
		
		form.add(fset);
		mainPanel.add(form);
		
		currentPage = mainPanel;
		RootPanel.get().add(mainPanel);
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
		fset.addStyleName(LG_OFFSET);
		
		Legend glossHeader = new Legend((finished) ? "Finished Gloss" : "Gloss");
		glossHeader.addStyleName(LG_WIDTH);
		glossHeader.addStyleName(SM_WIDTH);
		fset.add(glossHeader);
		
		List<FlowPanel> glossPanels = new ArrayList<>();
		for (GlossedSentence s : gloss) {
			FlowPanel panelPanel = new FlowPanel();
			panelPanel.addStyleName(LG_WIDTH);
			panelPanel.addStyleName(SM_WIDTH);
			
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
			buttonPanel.addStyleName(LG_WIDTH);
			buttonPanel.addStyleName(SM_WIDTH);
			buttonPanel.addStyleName("space-below-l");
			fset.add(buttonPanel);
		}

		resetGloss(fset, finished);
		
		return glossPanels;
	}
	
	private void resetGloss(FieldSet newGloss, boolean finished) {
		FlowPanel root = currentPage;
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
			String line1 = "";
			String line2 = "";
			String line3 = "";
			for (int i = 0; i < glosses.size(); i++) {
				String orig = glosses.get(i).getOrig();
				String ipa = glosses.get(i).getIpa();
				String split = splits[g][i].getSelectedValue();
				String trans = transl[g][i].getSelectedValue();
				line1 += orig + " ";
				line2 += split + " ";
				line3 += trans + " ";
				glossPanel.add(getFinishedGloss(orig, ipa, split, trans));
			}
			String glossCode = "\\begin{exe}\n" + "\\ex\n" + "\\glll\n"
					+ escapeLaTeXChars(line1) + "\\\\\n"
					+ escapeLaTeXChars(line2) + "\\\\\n"
					+ escapeLaTeXChars(line3) + "\\\\\n"
					+ "\\trans `Insert your translation here...'\n" + "\\end{exe}\n";
			Pre codePanel = new Pre();
			codePanel.setText(glossCode);
			codePanel.addStyleName("space-above-sm");
			glossPanel.add(codePanel);
		}
	}
	
	private String escapeLaTeXChars(String code) {
		StringBuilder esc = new StringBuilder();
		for (int i = 0; i < code.length(); i++) {
			char c = code.charAt(i);
			switch (c) {
				case '~': esc.append("\\textasciitilde{}"); break;
				case '^': esc.append("\\textasciicircum{}"); break;
				case '\\': esc.append("\\textbackslash{}"); break;
				case '<': esc.append("\\textless{}"); break;
				case '>': esc.append("\\textgreater{}"); break;
				case '|': esc.append("\\textbar{}"); break;
				case '&':
				case '%':
				case '$':
				case '#':
				case '_':
				case '{':
				case '}': esc.append('\\');
				default: esc.append(c);
			}
		}
		return esc.toString();
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
	
	
	private void loadInfoPage() {
		FlowPanel tablePanel = new FlowPanel();
		
		tablePanel.add(applyPageStyles(new Legend("Transcription schemes")));
		tablePanel.add(applyPageStyles(getTranscriptionTable()));
		tablePanel.add(applyPageStyles(new Legend("Glossing abbreviations")));
		tablePanel.add(applyPageStyles(getGlossAbbrTable()));

		currentPage = tablePanel;
		RootPanel.get().add(tablePanel);
	}
	
	private FlowPanel getTranscriptionTable() {
		FlowPanel tablePanel = new FlowPanel();
		CellTable<String[]> transcrTable = new CellTable<>();
		List<String[]> content = new ArrayList<>();
		content.add(new String[]{"Script","ISO-Uni","ISO-ASCII","Mozhi"});
		content.add(new String[]{"ആ","ā","aa","aa"});
		content.add(new String[]{"ങ","ṅa",";na","nga"});
		content.add(new String[]{"ശ","śa",";sa","Sa"});
		
		TextColumn<String[]> scriptCol = new TextColumn<String[]>() {
			@Override
			public String getValue(String[] line) {
				return line[0];
			}
		};
		transcrTable.addColumn(scriptCol);
		TextColumn<String[]> uniCol = new TextColumn<String[]>() {
			@Override
			public String getValue(String[] line) {
				return line[1];
			}
		};
		transcrTable.addColumn(uniCol);
		TextColumn<String[]> asciiCol = new TextColumn<String[]>() {
			@Override
			public String getValue(String[] line) {
				return line[2];
			}
		};
		transcrTable.addColumn(asciiCol);
		TextColumn<String[]> mozhiCol = new TextColumn<String[]>() {
			@Override
			public String getValue(String[] line) {
				return line[3];
			}
		};
		transcrTable.addColumn(mozhiCol);
		
		transcrTable.setRowCount(content.size());
		transcrTable.setRowData(0, content);
		tablePanel.add(transcrTable);
		
		return tablePanel;
	}
	
	private FlowPanel getGlossAbbrTable() {
		FlowPanel tablePanel = new FlowPanel();
		CellTable<String[]> abbrTable = new CellTable<>();
		List<String[]> content = new ArrayList<>();
		content.add(new String[]{"Abbr.","Grammatical phenomenon"});
		content.add(new String[]{"NEG","Negation"});
		content.add(new String[]{"PRS","Present tense"});
		content.add(new String[]{"PST","Past tense"});
		
		TextColumn<String[]> abbreviationCol = new TextColumn<String[]>() {
			@Override
			public String getValue(String[] line) {
				return line[0];
			}
		};
		abbrTable.addColumn(abbreviationCol);
		TextColumn<String[]> explanationCol = new TextColumn<String[]>() {
			@Override
			public String getValue(String[] line) {
				return line[1];
			}
		};
		abbrTable.addColumn(explanationCol);
		
		abbrTable.setRowCount(content.size());
		abbrTable.setRowData(0, content);
		tablePanel.add(abbrTable);
		
		return tablePanel;
	}
	
	private void loadSourcesPage() {
		FlowPanel sourcesPanel = new FlowPanel();

		currentPage = sourcesPanel;
		RootPanel.get().add(sourcesPanel);
	}
	
	private Widget applyPageStyles(Widget widget) {
		for (String style : PAGE_STYLES)
			widget.addStyleName(style);
		return widget;
	}
}
