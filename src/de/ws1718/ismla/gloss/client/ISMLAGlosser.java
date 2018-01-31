package de.ws1718.ismla.gloss.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.apache.commons.collections.functors.ForClosure;
import org.apache.commons.lang3.text.StrBuilder;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Legend;
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
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
	
	private void goToMainPage() {
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
		
		RadioButton b1 = new RadioButton("Malayalam script");
		b1.setText("Malayalam script");
		RadioButton b2 = new RadioButton("Mozhi romanization");
		b2.setText("Mozhi romanization");
		RadioButton b3 = new RadioButton("ISO-15919 romanization");
		b3.setText("ISO-15919 romanization");
		StringRadioGroup inFormat = new StringRadioGroup("Input format");
		inFormat.add(b1);
		inFormat.add(b2);
		inFormat.add(b3);
		FormLabel label = new FormLabel();
		label.setFor("inFormat");
		label.setText("Input script:");
		FlowPanel inFormatPanel = new FlowPanel();
		inFormatPanel.addStyleName("col-lg-2");
		inFormatPanel.add(label);
		inFormatPanel.add(inFormat);
		
		textGroup.add(textPanel);
		textGroup.add(inFormatPanel);
		
		Button submit = new Button("Gloss!");
		submit.setType(ButtonType.PRIMARY);
		buttonGroup.add(submit);
		buttonGroup.addStyleName("col-lg-8");
		buttonGroup.addStyleName("col-sm-12");

		form.addStyleName("col-lg-offset-2");
		
		fset.add(inputHeader);
		fset.add(textGroup);
		fset.add(buttonGroup);
		
		form.add(fset);
		
		RootPanel.get().add(form);
		
		addGloss();
	}
	
	private void addGloss() {
		Legend glossHeader = new Legend("Gloss");
		glossHeader.addStyleName("col-lg-8");
		glossHeader.addStyleName("col-sm-12");
		
		FieldSet fset = new FieldSet();
		
		VerticalPanel maanga = new VerticalPanel();
		maanga.add(new Text("മാങ്ങ"));
		maanga.add(new Text("māṅṅa"));
		maanga.add(new Text("maːŋːa"));
		maanga.add(new Text("mango"));
		maanga.setStyleName("glossPanel");
		
		VerticalPanel vaangikkunna = new VerticalPanel();
		vaangikkunna.add(new Text("വാങ്ങിക്കുന്ന"));
		vaangikkunna.add(new Text("vāṅṅikk-unn-a"));
		vaangikkunna.add(new Text("vaːŋːikʲːun̪ːa"));
		vaangikkunna.add(new Text("buy-PRES-A"));
		vaangikkunna.setStyleName("glossPanel");
		
		VerticalPanel payyan = new VerticalPanel();
		payyan.add(new Text("പയ്യൻ"));
		payyan.add(new Text("payyan"));
		payyan.add(new Text("pajːan"));
		payyan.add(new Text("boy"));
		payyan.setStyleName("glossPanel");
		
		VerticalPanel cantiyil = new VerticalPanel();
		cantiyil.add(new Text("ചന്തിയിൽ"));
		cantiyil.add(new Text("canti-y-il"));
		cantiyil.add(new Text("t͡ɕan̪d̪ijil"));
		cantiyil.add(new Text("market-0-LOC"));
		cantiyil.setStyleName("glossPanel");
		
		VerticalPanel aaNu = new VerticalPanel();
		aaNu.add(new Text("ആണ്"));
		aaNu.add(new Text("āṇ˘"));
		aaNu.add(new Text("aːɳɨ"));
		aaNu.add(new Text("be"));
		aaNu.setStyleName("glossPanel");
		
		VerticalPanel dot = new VerticalPanel();
		dot.add(new Text("."));
		dot.add(new Text("."));
		dot.add(new Text("ǀ"));
		dot.add(new Text("."));
		dot.setStyleName("glossPanel");
		
		FlowPanel glossPanel = new FlowPanel();
		glossPanel.add(maanga);
		glossPanel.add(vaangikkunna);
		glossPanel.add(payyan);
		glossPanel.add(cantiyil);
		glossPanel.add(aaNu);
		glossPanel.add(dot);
		glossPanel.addStyleName("col-lg-8");
		glossPanel.addStyleName("col-sm-12");
		
		fset.add(glossHeader);
		fset.add(glossPanel);
		fset.addStyleName("col-lg-offset-2");
		
		RootPanel.get().add(fset);
		
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		goToMainPage();
//		final Button sendButton = new Button("Send");
//		final TextBox nameField = new TextBox();
//		nameField.setText("GWT User");
//		final Label errorLabel = new Label();
//
//		// We can add style names to widgets
//		sendButton.addStyleName("sendButton");
//
//		// Add the nameField and sendButton to the RootPanel
//		// Use RootPanel.get() to get the entire body element
//		RootPanel.get("nameFieldContainer").add(nameField);
//		RootPanel.get("sendButtonContainer").add(sendButton);
//		RootPanel.get("errorLabelContainer").add(errorLabel);
//
//		// Focus the cursor on the name field when the app loads
//		nameField.setFocus(true);
//		nameField.selectAll();
//
//		// Create the popup dialog box
//		final DialogBox dialogBox = new DialogBox();
//		dialogBox.setText("Remote Procedure Call");
//		dialogBox.setAnimationEnabled(true);
//		final Button closeButton = new Button("Close");
//		// We can set the id of a widget by accessing its Element
//		closeButton.getElement().setId("closeButton");
//		final Label textToServerLabel = new Label();
//		final HTML serverResponseLabel = new HTML();
//		VerticalPanel dialogVPanel = new VerticalPanel();
//		dialogVPanel.addStyleName("dialogVPanel");
//		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
//		dialogVPanel.add(textToServerLabel);
//		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
//		dialogVPanel.add(serverResponseLabel);
//		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
//		dialogVPanel.add(closeButton);
//		dialogBox.setWidget(dialogVPanel);
//
//		// Add a handler to close the DialogBox
//		closeButton.addClickHandler(new ClickHandler() {
//			public void onClick(ClickEvent event) {
//				dialogBox.hide();
//				sendButton.setEnabled(true);
//				sendButton.setFocus(true);
//			}
//		});
//
//		// Create a handler for the sendButton and nameField
//		class MyHandler implements ClickHandler, KeyUpHandler {
//			/**
//			 * Fired when the user clicks on the sendButton.
//			 */
//			public void onClick(ClickEvent event) {
//				sendNameToServer();
//			}
//
//			/**
//			 * Fired when the user types in the nameField.
//			 */
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//					sendNameToServer();
//				}
//			}
//
//			/**
//			 * Send the name from the nameField to the server and wait for a response.
//			 */
//			private void sendNameToServer() {
//				// First, we validate the input.
//				errorLabel.setText("");
//				String textToServer = nameField.getText();
//				if (!FieldVerifier.isValidName(textToServer)) {
//					errorLabel.setText("Please enter at least four characters");
//					return;
//				}
//
//				// Then, we send the input to the server.
//				sendButton.setEnabled(false);
//				textToServerLabel.setText(textToServer);
//				serverResponseLabel.setText("");
//				greetingService.greetServer(textToServer, new AsyncCallback<String>() {
//					public void onFailure(Throwable caught) {
//						// Show the RPC error message to the user
//						dialogBox.setText("Remote Procedure Call - Failure");
//						serverResponseLabel.addStyleName("serverResponseLabelError");
//						serverResponseLabel.setHTML(SERVER_ERROR);
//						dialogBox.center();
//						closeButton.setFocus(true);
//					}
//
//					public void onSuccess(String result) {
//						dialogBox.setText("Remote Procedure Call");
//						serverResponseLabel.removeStyleName("serverResponseLabelError");
//						serverResponseLabel.setHTML(result);
//						dialogBox.center();
//						closeButton.setFocus(true);
//					}
//				});
//			}
//		}
//
//		// Add a handler to send the name to the server
//		MyHandler handler = new MyHandler();
//		sendButton.addClickHandler(handler);
//		nameField.addKeyUpHandler(handler);
	}
}
