package org.jbei.ice.client.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.PlasmidInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewPlasmidForm extends NewEntryForm {

    private final PlasmidInfo plasmid;

    interface PlasmidUiBinder extends UiBinder<Widget, NewPlasmidForm> {
    };

    private static PlasmidUiBinder uiBinder = GWT.create(PlasmidUiBinder.class);

    @UiField
    SuggestBox selectionMarkerBox;

    @UiField
    Button submitButton;

    public NewPlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data, Button saveButton) {

        super(data);
        plasmid = new PlasmidInfo();
        initWidget(uiBinder.createAndBindUi(this));
        init(saveButton);
    }

    /*
    protected void init() {

        layout.setWidth("800px");
        layout.addStyleName("gray_border");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);
        layout.setHTML(0, 0, ("New " + EntryType.PLASMID.getDisplay()));
        layout.getCellFormatter().setStyleName(0, 0, "collections_header");
        layout.getFlexCellFormatter().setColSpan(0, 0, 2);

        // name
        layout.setHTML(1, 0, "&nbsp;Name: <span class=\"required\">*</span>");
        TextBox name = createTextBox();
        layout.setWidget(1, 1, createWidgetWithHelpText(name, "e.g. pTSH117", true));

        // creator
        layout.setHTML(2, 0, "&nbsp;Creator: <span class=\"required\">*</span>");
        TextBox creator = createTextBox();
        layout.setWidget(2, 1, createWidgetWithHelpText(creator, "Who made this part?", true));

        // creator email
        layout.setHTML(3, 0, "&nbsp;Creator's Email:");
        TextBox creatorEmail = createTextBox();
        layout.setWidget(3, 1, createWidgetWithHelpText(creatorEmail, "If known", true));

        // status
        layout.setHTML(4, 0, "&nbsp;Status:");
        ListBox status = new ListBox();
        status.setVisibleItemCount(1);
        status.addItem("Complete");
        status.addItem("In Progress");
        status.addItem("Planned");
        status.setStyleName("inputbox");
        layout.setWidget(4, 1, status);

        // alias
        layout.setHTML(5, 0, "&nbsp;Alias:");
        TextBox alias = createTextBox();
        layout.setWidget(5, 1, alias);

        // links
        layout.setHTML(6, 0, "&nbsp;Links:");
        TextBox links = createTextBox("300px");
        layout.setWidget(6, 1, createWidgetWithHelpText(links, "Comma Separated", true));

        // circular
        layout.setHTML(7, 0, "&nbsp;Circular:");
        CheckBox circular = new CheckBox();
        circular.setValue(Boolean.TRUE);
        layout.setWidget(7, 1, circular);

        // backbone
        layout.setHTML(8, 0, "&nbsp;Backbone:");
        TextBox backbone = createTextBox("300px");
        layout.setWidget(8, 1, backbone);

        // selection markers
        layout.setHTML(9, 0, "&nbsp;Selection Markers:");
        Widget markerBox = createAutoCompleteForSelectionMarkers("300px");
        layout.setWidget(9, 1, createWidgetWithHelpText(markerBox, "Comma Separated", true));

        // origin of replication
        layout.setHTML(10, 0, "&nbsp;Origin of Replication:");
        Widget originBox = createAutoCompleteForOriginOfReplication("300px");
        layout.setWidget(10, 1, createWidgetWithHelpText(originBox, "Comma Separated", true));

        // promoters
        layout.setHTML(11, 0, "&nbsp;Promoters:");
        Widget promotersBox = createAutoCompleteForPromoters("300px");
        layout.setWidget(11, 1, createWidgetWithHelpText(promotersBox, "Comma Separated", true));

        // keywords
        layout.setHTML(12, 0, "&nbsp;Keywords:");
        TextBox keywords = createTextBox("640px");
        layout.setWidget(12, 1, keywords);

        //summary
        layout.setHTML(13, 0, "&nbsp;Summary: <span class=\"required\">*</span>");
        TextArea summary = new TextArea();
        summary.addStyleName("inputbox");
        summary.addStyleName("form_textarea");
        layout.setWidget(13, 1, summary);

        // references
        layout.setHTML(14, 0, "&nbsp;References:");
        TextArea references = new TextArea();
        references.addStyleName("inputbox");
        references.addStyleName("form_textarea");
        layout.setWidget(14, 1, references);

        // bio safety level
        layout.setHTML(15, 0, "&nbsp;Bio Safety Level:");
        ListBox bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName());
        status.setStyleName("inputbox");
        layout.setWidget(15, 1, bioSafety);

        // intellectual property
        layout.setHTML(16, 0, "&nbsp;Intellectual Property:");
        TextArea intellectualProp = new TextArea();
        intellectualProp.addStyleName("inputbox");
        intellectualProp.addStyleName("form_textarea");
        layout.setWidget(16, 1, intellectualProp);

        // funding source
        layout.setHTML(17, 0, "&nbsp;Funding Source:");
        TextBox funding = createTextBox();
        layout.setWidget(17, 1, funding);

        // principal investigator
        layout.setHTML(18, 0, "&nbsp;Principal Investigator: <span class=\"required\">*</span>");
        TextBox principalInvestigator = createTextBox();
        layout.setWidget(18, 1, principalInvestigator);

        // parameters
        layout.setHTML(19, 0, "&nbsp;Parameters:");
        TextBox parameters = this.createTextBox("500px"); // TODO : max length of 255
        layout.setWidget(
            19,
            1,
            createWidgetWithHelpText(
                parameters,
                "Example: text_parameter=\"Some text.\",number_parameter=23.45,boolean_parameter=true",
                false));

        // sample name
        layout.setHTML(20, 0, "&nbsp;Sample Name:");
        TextBox sampleName = createTextBox();
        layout.setWidget(
            20,
            1,
            createWidgetWithHelpText(sampleName,
                "(Optional. Required if location is specified below)", true));

        // sample notes
        layout.setHTML(21, 0, "&nbsp;Sample Notes:");
        TextArea sampleNotes = new TextArea();
        sampleNotes.setStyleName("inputbox");
        layout.setWidget(21, 1, sampleNotes);

        // save
        layout.setHTML(22, 0, "&nbsp;");
        Button button = new Button("Save");
        layout.setWidget(22, 1, button);
    }

    */
    protected TextBox createTextBox() {
        return this.createTextBox(null);
    }

    protected TextBox createTextBox(String width) {

        TextBox textBox = new TextBox();
        textBox.setStyleName("inputbox");
        textBox.setMaxLength(127);
        if (width != null)
            textBox.setWidth(width);
        return textBox;
    }

    @Override
    public EntryInfo getEntry() {
        return plasmid;
    }

    @Override
    protected void init(Button button) {
        selectionMarkerBox = createAutoCompleteForSelectionMarkers("300px");
    }

    @UiHandler("submitButton")
    void handleSubmit(ClickEvent event) {
        Window.alert("Clicked: " + selectionMarkerBox.getText());
    }

    //    @UiHandler("selectionMarkerBox")
    //    void handleText
}
