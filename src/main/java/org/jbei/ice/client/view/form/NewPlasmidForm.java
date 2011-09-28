package org.jbei.ice.client.view.form;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.EntryType;
import org.jbei.ice.shared.PlasmidInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class NewPlasmidForm extends NewEntryForm {

    private final PlasmidInfo plasmid;

    public NewPlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data, Button saveButton) {

        super(data);
        plasmid = new PlasmidInfo();
        initWidget(layout);
        init(saveButton);
    }

    @Override
    protected void init(Button saveButton) {

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
        bioSafety.setStyleName("inputbox");
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

        // sample location
        layout.setHTML(22, 0, "&nbsp;Sample Location");
        Widget location = createListBoxWithHelpText("(Must specify Sample Name above)",
            "Plasmid Storage (Default)");
        layout.setWidget(22, 1, location);

        // sample location places
        layout.setHTML(23, 0, "&nbsp;");
        layout.setWidget(23, 1, createSampleLocationOptions());

        // notes
        layout.setHTML(24, 0, "&nbsp;Notes");
        layout.setWidget(24, 1, createNotes());

        // save
        layout.setHTML(25, 0, "&nbsp;");
        saveButton = new Button("Save");
        layout.setWidget(25, 1, saveButton);
    }

    protected Widget createNotes() {
        VerticalPanel panel = new VerticalPanel();
        HorizontalPanel horiPanel = new HorizontalPanel();
        horiPanel.add(new HTML("Markup Type:"));
        horiPanel.add(this.createListBox());
        panel.add(horiPanel);

        TextArea notesArea = new TextArea();
        notesArea.addStyleName("inputbox");
        notesArea.setWidth("640px");
        notesArea.setHeight("480px");
        panel.add(notesArea);
        return panel;
    }

    protected Widget createListBox() {
        ListBox box = new ListBox();
        box.setStyleName("inputbox");
        box.setVisibleItemCount(1);
        box.addItem("Text");
        box.addItem("Wiki");
        box.addItem("Confluence");
        return box;
    }

    protected Widget createSampleLocationOptions() {
        FlexTable table = new FlexTable();
        table.setHTML(0, 0, "Shelf");
        table.setWidget(0, 1, createTextBox());
        table.setHTML(1, 0, "Box");
        table.setWidget(1, 1, createTextBox());
        table.setHTML(2, 0, "Tube");
        table.setWidget(2, 1, createTextBox());
        return table;
    }

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

}
