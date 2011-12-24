package org.jbei.ice.client.entry.add.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jbei.ice.shared.ArabidopsisSeedInfo;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewArabidopsisForm extends NewSingleEntryForm<ArabidopsisSeedInfo> {

    private TextBox name;
    private TextBox alias;
    private TextBox pI;
    private TextBox fundingSource;
    private ListBox status;
    private ListBox bioSafety;
    private ListBox generation;
    private ListBox plantType;
    private TextBox homozygosity;
    private TextBox links;
    private TextBox markers;
    private TextBox ecoType;
    private TextBox parents;
    private TextBox harvestDate;
    private TextBox keywords;
    private TextArea summary;
    private TextArea references;
    private TextArea ip;

    public NewArabidopsisForm(HashMap<AutoCompleteField, ArrayList<String>> data,
            String creatorName, String creatorEmail) {
        super(data, creatorName, creatorEmail, new ArabidopsisSeedInfo());
        initWidget(layout);
        init();
    }

    // Note that this needs to be in the respective specialized classes
    private Widget createTextBoxWithHelp(TextBox box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText
                + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
    }

    protected void init() {
        layout.setWidth("100%");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);

        layout.setWidget(0, 0, createGeneralWidget());
        layout.setWidget(1, 0, createParametersWidget());
        layout.setWidget(2, 0, createSampleWidget());
        layout.setWidget(3, 0, createNotesWidget());
        layout.setWidget(4, 0, createSubmitCancelButtons());
    }

    private void setLabel(boolean required, String label, FlexTable layout, int row, int col) {
        Widget labelWidget;
        if (required)
            labelWidget = new HTML(label + " <span class=\"required\">*</span>");
        else
            labelWidget = new Label(label);

        layout.setWidget(row, col, labelWidget);
        layout.getFlexCellFormatter().setWidth(row, col, "170px");
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        setLabel(true, "Name", general, row, 0);
        name = createStandardTextBox("205px");
        Widget widget = createTextBoxWithHelp(name, "e.g. Stock ID / Mutant Name");
        general.setWidget(row, 1, widget);

        // alias
        setLabel(false, "Alias", general, row, 2);
        alias = createStandardTextBox("205px");
        general.setWidget(row, 3, alias);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        setLabel(true, "Principal Investigator", general, row, 2);
        pI = createStandardTextBox("205px");
        general.setWidget(row, 3, pI);

        // creator's email
        row += 1;
        setLabel(false, "Creator's Email", general, row, 0);
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        setLabel(false, "Funding Source", general, row, 2);
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 3, fundingSource);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        status = new ListBox();
        status.setVisibleItemCount(1);
        status.addItem("Complete");
        status.addItem("In Progress");
        status.addItem("Planned");
        status.setStyleName("input_box");
        general.setWidget(row, 1, status);

        // bio safety level
        setLabel(false, "Bio Safety Level", general, row, 2);
        bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName());
        bioSafety.setStyleName("input_box");
        general.setWidget(row, 3, bioSafety);

        // generation
        row += 1;
        setLabel(false, "Generation", general, row, 0);
        generation = new ListBox();
        generation.setVisibleItemCount(1);
        generation.addItem("M0");
        generation.addItem("M1");
        generation.setStyleName("input_box");
        general.setWidget(row, 1, generation);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // plant type
        row += 1;
        setLabel(false, "Plant Type", general, row, 0);
        plantType = new ListBox();
        plantType.setVisibleItemCount(1);
        plantType.addItem("EMS");
        plantType.addItem("Over Expression");
        plantType.setStyleName("input_box");
        general.setWidget(row, 1, plantType);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // harvest date
        row += 1;
        general.setWidget(row, 0, new Label("Harvest Date"));
        harvestDate = createStandardTextBox("205px");
        general.setWidget(row, 1, harvestDate);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // homozygosity
        row += 1;
        general.setWidget(row, 0, new Label("Homozygosity"));
        homozygosity = createStandardTextBox("300px");
        general.setWidget(row, 1, homozygosity);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // links
        row += 1;
        general.setWidget(row, 0, new Label("Links"));
        links = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(links, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        general.setWidget(row, 0, new Label("Selection Markers"));
        general.getCellFormatter().setWidth(8, 0, "170px");
        markers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // eco type
        row += 1;
        general.setWidget(row, 0, new Label("Ecotype"));
        general.getCellFormatter().setWidth(8, 0, "170px");
        ecoType = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(ecoType, "If known");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // parents
        row += 1;
        general.setWidget(row, 0, new Label("Parents"));
        parents = createStandardTextBox("300px");
        general.setWidget(row, 1, parents);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        general.setWidget(row, 0, new Label("Keywords"));
        keywords = createStandardTextBox("640px");
        general.setWidget(row, 1, keywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        general.setWidget(row, 0, new HTML("Summary <span class=\"required\">*</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        summary = createTextArea("640px", "50px");
        general.setWidget(row, 1, summary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        general.setWidget(row, 0, new Label("References"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        references = createTextArea("640px", "50px");
        general.setWidget(row, 1, references);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        general.setWidget(row, 0, new Label("Intellectual Property"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        ip = createTextArea("640px", "50px");
        general.setWidget(row, 1, ip);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    protected Widget createSampleWidget() {
        int row = 0;
        FlexTable sample = new FlexTable();
        sample.setCellPadding(0);
        sample.setCellSpacing(3);
        sample.setWidth("100%");

        sample.setWidget(row, 0, new Label("Samples"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        sample.getFlexCellFormatter().setColSpan(row, 0, 2);

        row += 1;
        sample.setWidget(row, 0, new Label(""));
        sample.getFlexCellFormatter().setHeight(row, 0, "10px");
        sample.getFlexCellFormatter().setColSpan(row, 0, 2);

        // TODO : rest of samples here
        // name
        row += 1;
        sample.setWidget(row, 0, new Label("Name"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_label");
        TextBox sampleName = new TextBox();
        sampleName.setStyleName("entry_add_standard_input_box");
        sample.setWidget(row, 1, sampleName);

        // notes
        row += 1;
        sample.setWidget(row, 0, new Label("Notes"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_label");
        sample.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        TextArea sampleNotes = new TextArea();
        sampleNotes.setStyleName("entry_add_sample_notes_input");
        sample.setWidget(row, 1, sampleNotes);

        // location
        row += 1;
        sample.setWidget(row, 0, new Label("Location"));
        sample.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_label");
        ListBox locationOptions = new ListBox();
        locationOptions.setVisibleItemCount(1);
        locationOptions.addItem("Plasmid Storage (Default)");
        locationOptions.setStyleName("entry_add_standard_input_box");
        sample.setWidget(row, 1, locationOptions);

        // shelf, box etc
        row += 1;
        sample.setWidget(row, 0, new HTML("&nbsp;"));
        sample.getFlexCellFormatter().setWidth(row, 0, "170px");
        final String shelfTxt = "Shelf";
        final TextBox shelf = new TextBox();
        shelf.setText(shelfTxt);
        shelf.setStyleName("entry_add_standard_input_box");
        shelf.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                if (shelfTxt.equals(shelf.getText().trim()))
                    shelf.setText("");
            }
        });

        shelf.addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if ("".equals(shelf.getText().trim()))
                    shelf.setText(shelfTxt);
            }
        });
        sample.setWidget(row, 1, shelf);

        // second 
        row += 1;
        sample.setWidget(row, 0, new HTML("&nbsp;"));
        sample.getFlexCellFormatter().setWidth(row, 0, "170px");
        final String boxTxt = "Box";
        final TextBox box = new TextBox();
        box.setText(boxTxt);
        box.setStyleName("entry_add_standard_input_box");
        sample.setWidget(row, 1, box);

        // third
        row += 1;
        sample.setWidget(row, 0, new HTML("&nbsp;"));
        sample.getFlexCellFormatter().setWidth(row, 0, "170px");
        final String tubeTxt = "Tube";
        final TextBox tube = new TextBox();
        tube.setText(tubeTxt);
        tube.setStyleName("entry_add_standard_input_box");
        sample.setWidget(row, 1, tube);

        return sample;
    }

    @Override
    public FocusWidget validateForm() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<EntryInfo> getEntries() {
        // TODO Auto-generated method stub
        return null;
    }
}
