package org.jbei.ice.client.entry.add.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.PlasmidInfo;
import org.jbei.ice.shared.dto.StrainInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
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

public class NewStrainWithPlasmidForm extends Composite implements IEntryFormSubmit {

    private TextBox creator;
    private TextBox pI;
    private TextBox creatorEmail;
    private TextBox fundingSource;
    private ListBox status;
    private ListBox bioSafety;

    // strain fields
    private TextBox strainNumber;
    private TextBox strainAlias;
    private TextBox strainLinks;
    private TextBox host;
    private TextBox strainMarkers;
    private TextBox genPhen;
    private TextBox strainKeywords;
    private TextArea strainSummary;
    private TextArea strainReferences;
    private TextArea strainIp;

    // plasmid fields
    private TextBox plasmidName;
    private TextBox plasmidAlias;
    private CheckBox circular;
    private TextBox backbone;
    private TextBox plasmidLinks;
    private TextBox plasmidMarkers;
    private TextBox origin;
    private TextBox promoters;
    private TextBox plasmidKeywords;
    private TextArea plasmidSummary;
    private TextArea plasmidReferences;
    private TextArea plasmidIp;

    // submit cancel buttons
    private Button submit;
    private Button cancel;

    private final StrainInfo strain;
    private final PlasmidInfo plasmid;

    private final FlexTable layout;

    public NewStrainWithPlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data,
            String creatorName, String creatorEmail) {
        this.layout = new FlexTable();
        initWidget(layout);
        initComponents();
        init();

        this.strain = new StrainInfo();
        this.plasmid = new PlasmidInfo();

        this.creator.setText(creatorName);
        this.creatorEmail.setText(creatorEmail);
    }

    protected void initComponents() {
        submit = new Button("Submit");
        submit.setStyleName("btn_submit_entry_form");
        cancel = new Button("Reset");
        cancel.setStyleName("btn_reset_entry_form");
    }

    protected void init() {
        layout.setWidth("100%");
        layout.setCellPadding(2);
        layout.setCellSpacing(0);

        int row = 0;
        layout.setWidget(row, 0, createGeneralWidget());

        layout.setWidget(++row, 0, createPlasmidGeneralWidget());
        layout.setWidget(++row, 0, createPlasmidNotesWidget());

        layout.setWidget(++row, 0, createStrainGeneralWidget());
        layout.setWidget(++row, 0, createStrainNotesWidget());

        layout.setWidget(++row, 0, createSubmitCancelButtons());
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        setLabel(true, "Strain Number", general, row, 0);
        strainNumber = createStandardTextBox("205px");
        Widget widget = createTextBoxWithHelp(strainNumber, "e.g. JBEI-0001");
        general.setWidget(row, 1, widget);

        // PI
        setLabel(true, "Principal Investigator", general, row, 2);
        pI = createStandardTextBox("205px");
        general.setWidget(row, 3, pI);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        creator = createStandardTextBox("205px");
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // funding source
        setLabel(false, "Funding Source", general, row, 2);
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 3, fundingSource);

        // creator's email
        row += 1;
        setLabel(false, "Creator's Email", general, row, 0);
        creatorEmail = createStandardTextBox("205px");
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // bio safety level
        setLabel(false, "Bio Safety Level", general, row, 2);
        bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        for (BioSafetyOptions option : BioSafetyOptions.values()) {
            bioSafety.addItem(option.getDisplayName(), option.getValue());
        }

        bioSafety.setStyleName("input_box");
        general.setWidget(row, 3, bioSafety);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        status = new ListBox();
        status.setVisibleItemCount(1);
        for (StatusType type : StatusType.values()) {
            status.addItem(type.toString(), type.name());
        }
        status.setStyleName("input_box");
        general.setWidget(row, 1, status);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    private Widget createPlasmidGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        general.setWidget(row, 0, new Label("Plasmid Information"));
        general.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        general.getFlexCellFormatter().setColSpan(row, 0, 4);

        row += 1;
        general.setWidget(row, 0, new Label(""));
        general.getFlexCellFormatter().setHeight(row, 0, "10px");
        general.getFlexCellFormatter().setColSpan(row, 0, 4);

        // name
        row += 1;
        setLabel(true, "Plasmid Name", general, row, 0);
        plasmidName = createStandardTextBox("205px");
        Widget widget = createTextBoxWithHelp(plasmidName, "e.g. pTSH117");
        general.setWidget(row, 1, widget);

        // alias
        setLabel(false, "Alias", general, row, 2);
        plasmidAlias = createStandardTextBox("205px");
        general.setWidget(row, 3, plasmidAlias);

        // backbone
        row += 1;
        setLabel(false, "Backbone", general, row, 0);
        backbone = createStandardTextBox("300px");
        general.setWidget(row, 1, backbone);

        // circular
        setLabel(false, "Circular", general, row, 2);
        circular = new CheckBox();
        circular.setValue(true);
        general.setWidget(row, 3, circular);

        // links
        row += 1;
        general.setWidget(row, 0, new Label("Links"));
        plasmidLinks = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(plasmidLinks, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        general.setWidget(row, 0, new Label("Selection Markers"));
        general.getCellFormatter().setWidth(8, 0, "170px");
        plasmidMarkers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(plasmidMarkers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // origin of replication
        row += 1;
        general.setWidget(row, 0, new Label("Origin of Replication"));
        origin = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(origin, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // promoters
        row += 1;
        general.setWidget(row, 0, new Label("Promoters"));
        promoters = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(promoters, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        general.setWidget(row, 0, new Label("Keywords"));
        plasmidKeywords = createStandardTextBox("640px");
        general.setWidget(row, 1, plasmidKeywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        general.setWidget(row, 0, new HTML("Summary <span class=\"required\">*</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        plasmidSummary = createTextArea("640px", "50px");
        general.setWidget(row, 1, plasmidSummary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        general.setWidget(row, 0, new Label("References"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        plasmidReferences = createTextArea("640px", "50px");
        general.setWidget(row, 1, plasmidReferences);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        general.setWidget(row, 0, new Label("Intellectual Property"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        plasmidIp = createTextArea("640px", "50px");
        general.setWidget(row, 1, plasmidIp);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    protected TextArea createTextArea(String width, String height) {
        final TextArea area = new TextArea();
        area.setStyleName("input_box");
        area.setWidth(width);
        area.setHeight(height);
        return area;
    }

    private Widget createPlasmidNotesWidget() {
        FlexTable notes = new FlexTable();
        notes.setCellPadding(0);
        notes.setCellSpacing(3);
        notes.setWidth("100%");

        notes.setWidget(0, 0, new Label("Plasmid Notes"));
        notes.getFlexCellFormatter().setStyleName(0, 0, "entry_add_sub_header");
        notes.getFlexCellFormatter().setColSpan(0, 0, 2);
        notes.setWidget(1, 0, new Label(""));
        notes.getFlexCellFormatter().setHeight(1, 0, "10px");
        notes.getFlexCellFormatter().setColSpan(1, 0, 2);

        notes.setWidget(2, 0, new Label("Markup Type"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        ListBox markupOptions = new ListBox();
        markupOptions.setVisibleItemCount(1);
        markupOptions.addItem("Text");
        markupOptions.addItem("Wiki");
        markupOptions.addItem("Confluence");
        markupOptions.setStyleName("entry_add_standard_input_box");
        notes.setWidget(2, 1, markupOptions);

        // input
        notes.setWidget(3, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        TextArea area = new TextArea();
        area.setStyleName("entry_add_notes_input");
        notes.setWidget(3, 1, area);

        return notes;
    }

    private Widget createStrainGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        general.setWidget(row, 0, new Label("Strain Information"));
        general.getFlexCellFormatter().setStyleName(row, 0, "entry_add_sub_header");
        general.getFlexCellFormatter().setColSpan(row, 0, 2);

        row += 1;
        general.setWidget(row, 0, new Label(""));
        general.getFlexCellFormatter().setHeight(row, 0, "10px");
        general.getFlexCellFormatter().setColSpan(row, 0, 2);

        // alias
        row += 1;
        general.setWidget(row, 0, new Label("Alias"));
        strainAlias = createStandardTextBox("205px");
        general.setWidget(row, 1, strainAlias);

        // host strain
        row += 1;
        general.setWidget(row, 0, new Label("Host Strain"));
        host = createStandardTextBox("300px");
        general.setWidget(row, 1, host);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // links
        row += 1;
        general.setWidget(row, 0, new Label("Links"));
        strainLinks = createStandardTextBox("300px");
        Widget widget = createTextBoxWithHelp(strainLinks, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        general.setWidget(row, 0, new Label("Selection Markers"));
        general.getCellFormatter().setWidth(row, 0, "170px");
        strainMarkers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(strainMarkers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // Genotype/Phenotype [TODO : diff]
        row += 1;
        general.setWidget(row, 0, new Label("Genotype/Phenotype"));
        genPhen = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(genPhen, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        general.setWidget(row, 0, new Label("Keywords"));
        strainKeywords = createStandardTextBox("640px");
        general.setWidget(row, 1, strainKeywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        general.setWidget(row, 0, new HTML("Summary <span class=\"required\">*</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        strainSummary = createTextArea("640px", "50px");
        general.setWidget(row, 1, strainSummary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        general.setWidget(row, 0, new Label("References"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        strainReferences = createTextArea("640px", "50px");
        general.setWidget(row, 1, strainReferences);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        general.setWidget(row, 0, new Label("Intellectual Property"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        strainIp = createTextArea("640px", "50px");
        general.setWidget(row, 1, strainIp);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    private Widget createStrainNotesWidget() {
        FlexTable notes = new FlexTable();
        notes.setCellPadding(0);
        notes.setCellSpacing(3);
        notes.setWidth("100%");

        notes.setWidget(0, 0, new Label("Strain Notes"));
        notes.getFlexCellFormatter().setStyleName(0, 0, "entry_add_sub_header");
        notes.getFlexCellFormatter().setColSpan(0, 0, 2);
        notes.setWidget(1, 0, new Label(""));
        notes.getFlexCellFormatter().setHeight(1, 0, "10px");
        notes.getFlexCellFormatter().setColSpan(1, 0, 2);

        notes.setWidget(2, 0, new Label("Markup Type"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        ListBox markupOptions = new ListBox();
        markupOptions.setVisibleItemCount(1);
        markupOptions.addItem("Text");
        markupOptions.addItem("Wiki");
        markupOptions.addItem("Confluence");
        markupOptions.setStyleName("entry_add_standard_input_box");
        notes.setWidget(2, 1, markupOptions);

        // input
        notes.setWidget(3, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        TextArea area = new TextArea();
        area.setStyleName("entry_add_notes_input");
        notes.setWidget(3, 1, area);

        return notes;
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

    protected Widget createTextBoxWithHelp(TextBox box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText
                + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
    }

    protected TextBox createStandardTextBox(String width) {
        final TextBox box = new TextBox();
        box.setStyleName("input_box");
        box.setWidth(width);
        return box;
    }

    protected Widget createSubmitCancelButtons() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(3);
        layout.setWidth("100%");

        layout.setWidget(0, 0, new HTML("&nbsp;"));
        layout.getCellFormatter().setWidth(0, 0, "160px");

        layout.setWidget(0, 1, submit);
        layout.getFlexCellFormatter().setWidth(0, 1, "100px");
        layout.setWidget(0, 2, cancel);

        return layout;
    }

    @Override
    public Button getSubmit() {
        return submit;
    }

    @Override
    public Button getCancel() {
        return cancel;
    }

    @Override
    public FocusWidget validateForm() {

        FocusWidget invalid = null;

        // strain number
        if (strainNumber.getText().isEmpty()) {
            strainNumber.setStyleName("entry_input_error");
            invalid = strainNumber;
        } else {
            strainNumber.setStyleName("input_box");
        }

        // principal Investigator
        if (pI.getText().isEmpty()) {
            pI.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = pI;
        } else {
            pI.setStyleName("input_box");
        }

        // creator
        if (creator.getText().isEmpty()) {
            creator.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = creator;
        } else {
            creator.setStyleName("input_box");
        }

        // plasmid name
        if (plasmidName.getText().isEmpty()) {
            plasmidName.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = plasmidName;
        } else {
            plasmidName.setStyleName("input_box");
        }

        // plasmid summary
        if (plasmidSummary.getText().isEmpty()) {
            plasmidSummary.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = plasmidSummary;
        } else {
            plasmidSummary.setStyleName("input_box");
        }

        // strain summary
        if (strainSummary.getText().isEmpty()) {
            strainSummary.setStyleName("entry_input_error");
            if (invalid == null)
                invalid = strainSummary;
        } else {
            strainSummary.setStyleName("input_box");
        }

        return invalid;
    }

    @Override
    public Set<EntryInfo> getEntries() {
        Set<EntryInfo> entries = new HashSet<EntryInfo>();
        entries.add(this.strain);
        entries.add(this.plasmid);
        return entries;
    }

    @Override
    public void setSampleLocation(SampleLocationWidget sampleLocation) {
        // no samples for strain with one plasmid
    }

    @Override
    public void populateEntries() {
        strain.setCreator(creator.getText());
        plasmid.setCreator(creator.getText());

        strain.setPrincipalInvestigator(pI.getText());
        plasmid.setPrincipalInvestigator(pI.getText());

        strain.setCreatorEmail(creatorEmail.getText());
        plasmid.setCreatorEmail(creatorEmail.getText());

        strain.setFundingSource(fundingSource.getText());
        plasmid.setFundingSource(fundingSource.getText());

        String statusText = status.getValue(status.getSelectedIndex());
        strain.setStatus(statusText);
        plasmid.setStatus(statusText);

        Integer bioSafetyLevel;
        try {
            bioSafetyLevel = Integer.valueOf(bioSafety.getValue(bioSafety.getSelectedIndex()));
        } catch (NumberFormatException nfe) {
            bioSafetyLevel = 1;
        }

        strain.setBioSafetyLevel(bioSafetyLevel);
        plasmid.setBioSafetyLevel(bioSafetyLevel);

        // strain fields
        strain.setName(strainNumber.getText());
        strain.setAlias(strainAlias.getText());
        strain.setLinks(strainLinks.getText());
        strain.setHost(host.getText());
        strain.setSelectionMarkers(strainMarkers.getText());
        strain.setGenotypePhenotype(genPhen.getText());
        strain.setKeywords(strainKeywords.getText());
        strain.setShortDescription(strainSummary.getText());
        strain.setReferences(strainReferences.getText());
        strain.setIntellectualProperty(strainIp.getText());

        // plasmid fields
        plasmid.setName(plasmidName.getText());
        plasmid.setAlias(plasmidAlias.getText());
        plasmid.setCircular(circular.isEnabled());
        plasmid.setBackbone(backbone.getText());
        plasmid.setLinks(plasmidLinks.getText());
        plasmid.setSelectionMarkers(plasmidMarkers.getText());
        plasmid.setOriginOfReplication(origin.getText());
        plasmid.setPromoters(promoters.getText());
        plasmid.setKeywords(plasmidKeywords.getText());
        plasmid.setShortDescription(plasmidSummary.getText());
        plasmid.setReferences(plasmidReferences.getText());
        plasmid.setIntellectualProperty(plasmidIp.getText());
    }
}
