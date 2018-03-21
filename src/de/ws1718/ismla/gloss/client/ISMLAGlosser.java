package de.ws1718.ismla.gloss.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.InlineRadio;
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
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.ws1718.ismla.gloss.server.TableProviderServiceImpl;
import de.ws1718.ismla.gloss.shared.MalayalamFormat;

/**
 * The user interface of the Malayalam Glosser.
 */
public class ISMLAGlosser implements EntryPoint {
	// The names of the supported scripts
	private static final String MALAYALAM_SCRIPT = "Malayalam script";
	private static final String ISO15919_UNICODE = "ISO-15919 (Unicode)";
	private static final String ISO15919_ASCII = "ISO-15919 (ASCII)";
	private static final String MOZHI = "Mozhi romanization";
	
	// A couple of recurring style settings
	private static final String LG_OFFSET = "col-lg-offset-2";
	private static final String LG_WIDTH = "col-lg-8";
	private static final String SM_WIDTH = "col-sm-12";
	private static final String[] PAGE_STYLES = new String[]{LG_OFFSET,LG_WIDTH,SM_WIDTH};
	
	// Representations of the supported LaTeX packages for glossing
	private static enum GlossPackage {GB4E, EXPEX}
	// The package currently selected by the user
	private GlossPackage selectedGlossPackage = GlossPackage.GB4E;

	// The interface to the actual Glosser on the server
	private GlossServiceAsync glossService = GWT.create(GlossService.class);
	// The service providing the table contents for the help page
	private TableProviderServiceAsync readService = GWT.create(TableProviderService.class);
	
	// The page currently opened by the user
	private FlowPanel currentPage;
	// The static help page
	private FlowPanel infoPage = loadInfoPage();
	// The static about page
	private FlowPanel sourcePage = loadSourcesPage();
	
	// The input script currently selected by the user
	private MalayalamFormat currentInFormat = MalayalamFormat.MALAYALAM_SCRIPT;
	// The gloss script currently selected by the user
	private MalayalamFormat currentOutFormat = MalayalamFormat.ISO15919_UNICODE;

	// The submit button
	private Button submit = new Button("Gloss");
	
	// The current gloss
	private List<GlossedSentence> gloss = new ArrayList<>();
	// The list boxes for the editable morpheme splits of the current gloss
	private ListBox[][] splits = new ListBox[0][];
	// The list boxes for the editable glosses of the current gloss
	private ListBox[][] transl = new ListBox[0][];
	
	// The editable gloss section, or null if not visible
	private FieldSet glossPage = null;
	// The finished gloss section, or null if not visible
	private FieldSet finGlossPage = null;


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// CREATE THE NAVBAR
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
		final AnchorListItem goToMain = new AnchorListItem("Glosser");
		final AnchorListItem goToInfo = new AnchorListItem("Help");
		final AnchorListItem goToSources = new AnchorListItem("About");
		navEntries.add(goToMain);
		navEntries.add(goToInfo);
		navEntries.add(goToSources);
		navCollapse.add(navEntries);
		navbar.add(navCollapse);
		
		// Add handlers for the Navbar entries
		goToMain.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				goToMain.setActive(true);
				goToInfo.setActive(false);
				goToSources.setActive(false);
				RootPanel.get().remove(currentPage);
				currentPage = loadMainPage();
				RootPanel.get().add(currentPage);
			}
		});
		goToInfo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				goToMain.setActive(false);
				goToInfo.setActive(true);
				goToSources.setActive(false);
				RootPanel.get().remove(currentPage);
				currentPage = infoPage;
				RootPanel.get().add(infoPage);
			}
		});
		goToSources.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				goToMain.setActive(false);
				goToInfo.setActive(false);
				goToSources.setActive(true);
				RootPanel.get().remove(currentPage);
				currentPage = sourcePage;
				RootPanel.get().add(sourcePage);
			}
		});
		
		RootPanel.get().add(navbar);

		// Load main page
		goToMain.setActive(true);
		currentPage = loadMainPage();
		RootPanel.get().add(currentPage);
	}
	
	/**
	 * Load the main page with the glosser UI.
	 * @return The panel containing the main page
	 */
	private FlowPanel loadMainPage() {
		FlowPanel mainPanel = new FlowPanel();
		Form form = new Form();
		FieldSet fset = new FieldSet();
		FormGroup textGroup = new FormGroup();
		
		Legend inputHeader = new Legend("Your input");
		applyPageStyles(inputHeader);
		
		// Create text area for user to type input in
		final TextArea inputText = new TextArea();
		inputText.setVisibleLines(14);
		inputText.setPlaceholder("Enter your Malayalam text here");
		inputText.addStyleName("space-below-sm");
		FlowPanel textPanel = new FlowPanel();
		textPanel.addStyleName(LG_OFFSET);
		textPanel.addStyleName("col-lg-6");
		textPanel.addStyleName(SM_WIDTH);
		textPanel.addStyleName("space-below-lg");
		textPanel.add(inputText);
		
		// Create input format selection buttons
		ClickHandler inFormatHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RadioButton button = (RadioButton) event.getSource();
				currentInFormat = getFormat(button.getText());
			}
		};
		RadioButton biScript = new RadioButton("inFormat");
		biScript.setText(MALAYALAM_SCRIPT);
		biScript.setValue(true);
		biScript.addClickHandler(inFormatHandler);
		RadioButton biMozhi = new RadioButton("inFormat");
		biMozhi.setText(MOZHI);
		biMozhi.addClickHandler(inFormatHandler);
		RadioButton biUni = new RadioButton("inFormat");
		biUni.setText(ISO15919_UNICODE);
		biUni.addClickHandler(inFormatHandler);
		RadioButton biAscii = new RadioButton("inFormat");
		biAscii.setText(ISO15919_ASCII);
		biAscii.addClickHandler(inFormatHandler);
		FormLabel labelIn = new FormLabel();
		labelIn.setFor("inFormat");
		labelIn.setText("Input script:");
		
		// Create gloss format selection buttons
		ClickHandler outFormatHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RadioButton button = (RadioButton) event.getSource();
				currentOutFormat = getFormat(button.getText());
			}
		};
		RadioButton boUni = new RadioButton("outFormat");
		boUni.setText(ISO15919_UNICODE);
		boUni.setValue(true);
		boUni.addClickHandler(outFormatHandler);
		RadioButton boAscii = new RadioButton("outFormat");
		boAscii.setText(ISO15919_ASCII);
		boAscii.addClickHandler(outFormatHandler);
		RadioButton boMozhi = new RadioButton("outFormat");
		boMozhi.setText(MOZHI);
		boMozhi.addClickHandler(outFormatHandler);
		FormLabel labelOut = new FormLabel();
		labelOut.setFor("outFormat");
		labelOut.setText("Gloss script:");
		labelOut.addStyleName("space-above-sm");
		
		// Build input and gloss format selection panel
		FlowPanel inOutFormatPanel = new FlowPanel();
		inOutFormatPanel.addStyleName("col-lg-2");
		inOutFormatPanel.add(labelIn);
		inOutFormatPanel.add(biScript);
		inOutFormatPanel.add(biUni);
		inOutFormatPanel.add(biAscii);
		inOutFormatPanel.add(biMozhi);
		inOutFormatPanel.add(labelOut);
		inOutFormatPanel.add(boUni);
		inOutFormatPanel.add(boAscii);
		inOutFormatPanel.add(boMozhi);
		
		textGroup.add(textPanel);
		textGroup.add(inOutFormatPanel);
		
		// Create submit button
		submit = new Button("Gloss");
		submit.setType(ButtonType.PRIMARY);
		submit.setDataLoadingText("Glossing...");
		submit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit.state().loading();
				glossService.getGloss(inputText.getText(), currentInFormat, currentOutFormat, new AsyncCallback<List<GlossedSentence>>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Unable to obtain server response: " + caught.getMessage());
					}

					@Override
					public void onSuccess(List<GlossedSentence> result) {
						gloss = result;
						reloadGloss();
						submit.state().reset();
					}
				});
			}
		});
		textPanel.add(submit);
		
		// Add everything together
		fset.add(inputHeader);
		fset.add(textGroup);
		
		form.add(fset);
		mainPanel.add(form);
		
		return mainPanel;
	}
	
	/**
	 * @param text The name of a Malayalam script
	 * @return The corresponding MalayalamFormat object
	 */
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
	
	/**
	 * Create the panel containing the glossed text
	 * @param finished Make editable if false
	 * @return A list of FlowPanels to fill with the actual gloss content
	 */
	private List<FlowPanel> createGlossPanel(boolean finished) {
		FieldSet fset = new FieldSet();
		
		// Create header
		Legend glossHeader = new Legend((finished) ? "Finished Gloss" : "Gloss");
		applyPageStyles(glossHeader);
		fset.add(glossHeader);
		
		// Prepare a panel for each glossed sentence
		List<FlowPanel> glossPanels = new ArrayList<>();
		for (GlossedSentence s : gloss) {
			FlowPanel panelPanel = new FlowPanel();
			applyPageStyles(panelPanel);
			
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

		// Create "Finish" button if gloss is editable
		if (!finished) {
			FlowPanel buttonPanel = new FlowPanel();
			applyPageStyles(buttonPanel);
			buttonPanel.addStyleName("space-below-lg");
			
			// Create button
			Button submit = new Button("Finish");
			submit.setType(ButtonType.PRIMARY);
			submit.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					finishGloss();
				}
			});
			buttonPanel.add(submit);
			
			// Create gloss package selection
			InlineRadio gb4e = new InlineRadio("glossFormat");
			gb4e.setText("gb4e");
			gb4e.addStyleName("space-left-sm");
			gb4e.setValue(true);
			gb4e.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) { selectedGlossPackage = GlossPackage.GB4E; }
			});
			InlineRadio expex = new InlineRadio("glossFormat");
			expex.setText("expex");
			expex.addStyleName("space-left-sm");
			expex.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) { selectedGlossPackage = GlossPackage.EXPEX; }
			});
			selectedGlossPackage = GlossPackage.GB4E;
			FormLabel glossFormatLabel = new FormLabel();
			glossFormatLabel.setFor("glossFormat");
			glossFormatLabel.setText("LaTeX code for:");
			glossFormatLabel.addStyleName("space-left-sm");
			buttonPanel.add(glossFormatLabel);
			buttonPanel.add(gb4e);
			buttonPanel.add(expex);
			
			fset.add(buttonPanel);
		}

		// Remove any previous glosses and display the new one
		resetGloss(fset, finished);
		
		return glossPanels;
	}
	
	/**
	 * Remove previous gloss(es) to display a new one
	 * @param newGloss The new gloss section
	 * @param finished True if newGloss contains finished glosses, false if they are editable
	 */
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
	
	/**
	 * Reload the displayed glosses to fit the new submitted user input.
	 */
	private void reloadGloss() {
		// Remove any finished glosses from previous run
		resetGloss(null, true);
		// Get panels for new editable glosses
		List<FlowPanel> glossPanels = createGlossPanel(false);
		// Fill panels with gloss data
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
	
	/**
	 * Update the finished gloss according to the user selections in the editable gloss part.
	 */
	private void finishGloss() {
		// Get panels for new finished glosses
		List<FlowPanel> glossPanels = createGlossPanel(true);

		// Fill panels with gloss data and create LaTeX codes
		for (int g = 0; g < gloss.size(); g++) {
			String sentence = gloss.get(g).getSentence();
			List<GlossedWord> glosses = gloss.get(g).getGlosses();
			FlowPanel glossPanel = glossPanels.get(g);
			// Lines for LaTeX glosses (original word, split word, gloss)
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
			// LaTeX code panel
			Pre codePanel = new Pre();
			codePanel.setText(getGlossCode(sentence, line1, line2, line3));
			codePanel.addStyleName("space-above-sm");
			glossPanel.add(codePanel);
		}
	}
	
	/**
	 * Create the LaTeX code for a glossed sentence.
	 * @param sentence Original sentence
	 * @param line1 Space separated original tokens
	 * @param line2 Space separated morpheme splits of the tokens
	 * @param line3 Space separated glosses of the tokens
	 * @return LaTeX code for gb4e or expex depending on the user's selection
	 */
	private String getGlossCode(String sentence, String line1, String line2, String line3) {
		if (selectedGlossPackage.equals(GlossPackage.GB4E))
			return "\\begin{exe}\n" + "\\ex\n" + sentence + "\n\\glll\n"
			+ escapeLaTeXChars(line1) + "\\\\\n"
			+ escapeLaTeXChars(line2) + "\\\\\n"
			+ escapeLaTeXChars(line3) + "\\\\\n"
			+ "\\trans `Insert your translation here...'\n" + "\\end{exe}\n";
		if (selectedGlossPackage.equals(GlossPackage.EXPEX))
			return "\\ex\\begingl\n" + "\\glpreamble " + sentence + " //\n"
			+ "\\gla " + escapeLaTeXChars(line1) + "//\n"
			+ "\\glb " + escapeLaTeXChars(line2) + "//\n"
			+ "\\glc " + escapeLaTeXChars(line3) + "//\n"
			+ "\\glft `Insert your translation here...' //\n" + "\\endgl\\xe\n";
		return "";
	}
	
	/**
	 * @param code String to inserted into LaTeX code
	 * @return The same string with LaTeX special characters escaped correctly
	 */
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
	
	/**
	 * @param orig A token in its original script
	 * @param ipa The IPA transcription of the token
	 * @param splts The possible morpheme splits of the token
	 * @param glsses The possible glosses for the currently selected morpheme split
	 * @param i The index of the sentence this token belongs to
	 * @param j The position of the token in the sentence
	 * @return The vertical panel containing the editable gloss for this token
	 */
	private VerticalPanel getEditableGloss(String orig, String ipa, String[] splts, String[] glsses, final int i, final int j) {
		ListBox splitBox = fillListBox(new ListBox(), splts);
		splitBox.addChangeHandler(new ChangeHandler() {
			GlossedWord word = gloss.get(i).getGlosses().get(j);
			@Override
			public void onChange(ChangeEvent event) {
				transl[i][j].clear();
				fillListBox(transl[i][j], word.getGlosses(splits[i][j].getSelectedIndex()));
			}
		});
		ListBox glossBox = fillListBox(new ListBox(), glsses);
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
	
	/**
	 * Fill a list box with an array of strings
	 * @param box A ListBox
	 * @param items The string array
	 * @return The filled list box (same as the one passed to the method)
	 */
	private ListBox fillListBox(ListBox box, String[] items) {
		for (String item : items)
			box.addItem(item);
		if (items.length > 1)
			box.setEnabled(true);
		else
			box.setEnabled(false);
		return box;
	}
	
	/**
	 * @param orig A token in its original script
	 * @param ipa The IPA transcription of the token
	 * @param split The currently selected morpheme split of the token
	 * @param gloss The currently selected gloss for the token
	 * @return The vertical panel containing the finished gloss for this token
	 */
	private VerticalPanel getFinishedGloss(String orig, String ipa, String split, String gloss) {
		VerticalPanel g = new VerticalPanel();
		g.add(new Text(orig));
		g.add(new Text(ipa));
		g.add(new Text(split));
		g.add(new Text(gloss));
		g.setStyleName("glossPanel");
		return g;
	}
	
	
	/**
	 * @return The panel containing the help page
	 */
	private FlowPanel loadInfoPage() {
		final FlowPanel infoPanel = new FlowPanel();

		infoPanel.add(applyPageStyles(new Legend("How to use the Glosser")));
		String gb4eLink = link("https://ctan.org/pkg/gb4e", "gb4e");
		String expexLink = link("https://ctan.org/pkg/expex", "expex");
		infoPanel.add(applyPageStyles(new HTMLPanel("p",
				"The Malayalam Glosser is a tool to break any Malayalam text apart into its individual morphemes. "
						+ "It is a tokenizer in the sense that it will split contracted expressions such as വീട്ടിലാണ് into "
						+ "their individual tokens (വീട്ടിൽ and ആണ്), but first and foremost it is a morphology analyzer which "
						+ "can split inflected words such as വീട്ടിൽ into their individual morphemes (വീട് and the locative ending "
						+ "-ഇൽ) and annotate them accordingly. The finished glosses are also converted to LaTeX code (using the "
						+ gb4eLink + " or " + expexLink + " package) so that you can easily insert them into you LaTeX document.")));
		infoPanel.add(applyPageStyles(new HTMLPanel("p",
				"To use the Glosser, simply enter your Malayalam text in one of the four supported input scripts (see "
						+ "below). Make sure to select the input script that you used, and choose a script to display the "
						+ "morpheme-split Malayalam words in. The Glosser will create a gloss for each sentence, displaying "
						+ "information in four rows: The indidvidual tokens (e.g. വീട്ടിൽ) in the input script, their phonetic "
						+ "transcription (e.g. ʋiːʈːil), their gloss in the selected gloss script (e.g. vīṭṭ-il) and the "
						+ "annotation of that gloss (e.g. house-LOC). In the case that there are multiple possible glosses "
						+ "or multiple possible annotations of a gloss, you will be able to select your preferred one via a "
						+ "dropdown list. When you are done editing your gloss, click \"Finish\" to get you final glosses and "
						+ "gb4e codes.")));

		infoPanel.add(applyPageStyles(new Legend("Supported scripts")));
		String isoLink = link("https://en.wikipedia.org/w/index.php?title=ISO_15919&oldid=825271114", "this");
		String mozhiLink = link("https://sites.google.com/site/cibu/mozhi/mozhi2", "this");
		infoPanel.add(applyPageStyles(new HTMLPanel("p",
				"The Malayalam Glosser supports four ways to write Malayalam: Malayalam script, the ISO-15919 (National Library "
						+ "at Kolkata) romanization both with Unicode and ASCII characters (following " + isoLink + " page), and "
						+ "the popular Mozhi romanization (following " + mozhiLink + " page). The chart below "
						+ "shows which Malayalam character maps to which Latin character in the different scripts. When typing "
						+ "your text into the Glosser in one of the romanizations, please make sure to always explicitly write "
						+ "out a word-final candrakkala, otherwise the words may not be found in the underlying dictionary.")));
		
		readService.getTable(TableProviderService.TRANSCR_TABLE, new AsyncCallback<TableContents>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Unable to obtain server response: " + caught.getMessage());
			}

			@Override
			public void onSuccess(TableContents transcrTable) {
				infoPanel.add(applyPageStyles(getTable(transcrTable.getRows(), transcrTable.getHeader(), true)));
				
				infoPanel.add(applyPageStyles(new Legend("Glossing abbreviations")));
				infoPanel.add(applyPageStyles(new HTMLPanel("p",
						"The following is a list of all the abbreviations used in the generated glosses and the names of the "
								+ "grammatical phenomena they refer to.")));
				
				readService.getTable(TableProviderService.ABBR_TABLE, new AsyncCallback<TableContents>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Unable to obtain server response: " + caught.getMessage());
					}

					@Override
					public void onSuccess(TableContents abbrTable) {						
						infoPanel.add(applyPageStyles(getTable(abbrTable.getRows(), abbrTable.getHeader(), false)));
					}
				});
			}
		});

		return infoPanel;
	}
	
	/**
	 * @param content The rows of a table
	 * @param colHeaders The header of the table columns
	 * @param equalSize True if all columns in the table should be of equal width,
	 * 					false if the width should be dependent on the content
	 * @return A CellTabe widget with the specified contents and settings
	 */
	private FlowPanel getTable(List<String[]> content, String[] colHeaders, boolean equalSize) {
		FlowPanel tablePanel = new FlowPanel();
		CellTable<String[]> table = new CellTable<>();
		if (equalSize)
			table.setWidth("100%", true);
		double pc = 100 / colHeaders.length;
		
		for (int i = 0; i < colHeaders.length; i++) {
			final int j = i;
			TextColumn<String[]> col = new TextColumn<String[]>() {
				@Override
				public String getValue(String[] line) {
					return line[j];
				}
			};
			table.addColumn(col, colHeaders[i]);
			if (equalSize)
				table.setColumnWidth(col, pc, Unit.PCT);
		}
		
		table.setVisibleRange(0, content.size());
		table.setRowData(0, content);
		tablePanel.add(table);
		
		return tablePanel;
	}
	
	/**
	 * @return The panel containing the about page
	 */
	private FlowPanel loadSourcesPage() {
		FlowPanel sourcesPanel = new FlowPanel();
		
		sourcesPanel.add(applyPageStyles(new Legend("About")));
		sourcesPanel.add(applyPageStyles(new HTMLPanel("p",
				"The Malayalam Glosser was created by Thora Daneyko for the course \"Industrial-Strength Multilingual "
						+ "Language Analysis\" taught by Johannes Dellert and Björn Rudzewitz at the University of "
						+ "Tübingen in the Winter Semester 2017/18.")));
		String gwtBootstrap = link("https://github.com/gwtbootstrap3/gwtbootstrap3", "GWT-Bootstrap library");
		String nel = link("http://northeuralex.org/", "NorthEuraLex");
		sourcesPanel.add(applyPageStyles(new HTMLPanel("p",
				"This page was coded in Java using the " + gwtBootstrap + ". For the phonetic transcription and "
				+ "transliteration between the different scripts, I used the transliterator system that was designed "
				+ "for the " + nel + " database.")));
		sourcesPanel.add(applyPageStyles(new HTMLPanel("p",
				"The Malayalam morphology analysis is mainly based on Rodney F. Moag's \"Malayalam: A University Course "
						+ "and Reference Grammar\" (1994), covering chapters 1 to 13 so far. Further information about "
						+ "Malayalam grammar was drawn from Ronald E. Asher and T. C. Kumari's \"Malayalam\" (1997) grammar.")));
//		String olam = link("https://olam.in/", "olam.in");
//		sourcesPanel.add(applyPageStyles(new HTMLPanel("p",
//				"The underlying dictionary data is composed of an " + olam + " dictionary dump (nouns, verbs, adjectives, "
//				+ "adverbs, adpositions and conjunctions) and manually added entries based on Moag's text book (pronouns "
//				+ "and grammatical particles).")));
		sourcesPanel.add(applyPageStyles(new HTMLPanel("p",
				"The underlying dictionary data is based on lessons 1 to 11 of the Moag textbook.")));

		return sourcesPanel;
	}
	
	/**
	 * Apply some common page styles to a widget
	 * @param widget A widget
	 * @return The same widget
	 */
	private Widget applyPageStyles(Widget widget) {
		for (String style : PAGE_STYLES)
			widget.addStyleName(style);
		return widget;
	}
	
	/**
	 * @param url A URL
	 * @param label The text that should be linked with this URL
	 * @return The HTML code for that link
	 */
	private String link(String url, String label) {
		return "<a  target=\"_blank\" rel=\"noopener noreferrer\" href=\"" + url + "\">" + label + "</a>";
	}
}
