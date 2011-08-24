package org.jbei.ice.client.view.form;

import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.EntryType;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;

public class NewArabidopsisForm extends NewEntryForm {

    public NewArabidopsisForm() {

        layout.setWidth("800px");
        layout.addStyleName("gray_border");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);
        layout.setHTML(0, 0, ("New " + EntryType.ARABIDOPSIS.getDisplay()));
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

        // generation
        layout.setHTML(7, 0, "&nbsp;Generation:");
        ListBox generation = new ListBox();
        generation.setVisibleItemCount(1);
        generation.addItem("M0");
        generation.addItem("M1");
        generation.addItem("M2");
        generation.addItem("T1");
        generation.addItem("T2");
        generation.addItem("T3");
        generation.addItem("T4");
        generation.addItem("T5");
        generation.setStyleName("inputbox");
        layout.setWidget(7, 1, generation);

        // Plant Type
        layout.setHTML(8, 0, "&nbsp;Plant Type:");
        ListBox plantType = new ListBox();
        generation.setVisibleItemCount(1);
        plantType.addItem("EMS");
        plantType.addItem("Over Expression");
        plantType.addItem("RNAi");
        plantType.addItem("Reporter");
        plantType.addItem("T-DNA");
        plantType.addItem("Other");
        plantType.setStyleName("inputbox");
        layout.setWidget(8, 1, plantType);

        // homozygosity
        layout.setHTML(9, 0, "&nbsp;Homozygosity:");
        TextBox homozygosity = createTextBox("300px");
        layout.setWidget(9, 1, homozygosity);

        // selection markers
        layout.setHTML(10, 0, "&nbsp;Selection Markers:");
        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        oracle.add("foo");
        oracle.add("bar");
        SuggestBox box = new SuggestBox(oracle);
        box.setWidth("300px");
        box.addStyleName("inputbox");
        layout.setWidget(10, 1, createWidgetWithHelpText(box, "Comma Separated", true));

        // ecotype
        layout.setHTML(11, 0, "&nbsp;Ecotype:");
        TextBox ecotype = createTextBox("300px");
        layout.setWidget(11, 1, createWidgetWithHelpText(ecotype, "if known", true));

        // parents
        layout.setHTML(12, 0, "&nbsp;Parents:");
        TextBox parents = createTextBox("300px");
        layout.setWidget(12, 1, parents);

        // harvest date
        layout.setHTML(13, 0, "&nbsp;Harvest Date:");
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
        DateBox dateBox = new DateBox();
        dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
        dateBox.addStyleName("inputbox");
        layout.setWidget(13, 1, dateBox);

        // keywords
        layout.setHTML(14, 0, "&nbsp;Keywords:");
        TextBox keywords = createTextBox("640px");
        layout.setWidget(14, 1, keywords);

        //summary
        layout.setHTML(15, 0, "&nbsp;Summary: <span class=\"required\">*</span>");
        TextArea summary = new TextArea();
        summary.addStyleName("inputbox");
        summary.addStyleName("form_textarea");
        layout.setWidget(15, 1, summary);

        // references
        layout.setHTML(16, 0, "&nbsp;References:");
        TextArea references = new TextArea();
        references.addStyleName("inputbox");
        references.addStyleName("form_textarea");
        layout.setWidget(16, 1, references);

        // bio safety level
        layout.setHTML(17, 0, "&nbsp;Bio Safety Level:");
        ListBox bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName());
        bioSafety.setStyleName("inputbox");
        layout.setWidget(17, 1, bioSafety);

        // intellectual property
        layout.setHTML(18, 0, "&nbsp;Intellectual Property:");
        TextArea intellectualProp = new TextArea();
        intellectualProp.addStyleName("inputbox");
        intellectualProp.addStyleName("form_textarea");
        layout.setWidget(18, 1, intellectualProp);

        // funding source
        layout.setHTML(19, 0, "&nbsp;Funding Source:");
        TextBox funding = createTextBox();
        layout.setWidget(19, 1, funding);

        // principal investigator
        layout.setHTML(20, 0, "&nbsp;Principal Investigator: <span class=\"required\">*</span>");
        TextBox principalInvestigator = createTextBox();
        layout.setWidget(20, 1, principalInvestigator);

        // parameters
        layout.setHTML(21, 0, "&nbsp;Parameters:");
        TextBox parameters = this.createTextBox("500px"); // TODO : max length of 255
        layout.setWidget(
            21,
            1,
            createWidgetWithHelpText(
                parameters,
                "Example: text_parameter=\"Some text.\",number_parameter=23.45,boolean_parameter=true",
                false));

        // sample name
        layout.setHTML(22, 0, "&nbsp;Sample Name:");
        TextBox sampleName = createTextBox();
        layout.setWidget(
            22,
            1,
            createWidgetWithHelpText(sampleName,
                "(Optional. Required if location is specified below)", true));

        // sample notes
        layout.setHTML(23, 0, "&nbsp;Sample Notes:");
        TextArea sampleNotes = new TextArea();
        sampleNotes.setStyleName("inputbox");
        layout.setWidget(23, 1, sampleNotes);

        // save
        layout.setHTML(24, 0, "&nbsp;");
        Button button = new Button("Save");
        layout.setWidget(24, 1, button);
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
}
