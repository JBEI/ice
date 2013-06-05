package org.jbei.ice.client.collection.add.form;

import java.util.HashMap;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.client.entry.view.model.AutoCompleteSuggestOracle;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;

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
    private SuggestBox strainMarkers;
    private TextBox genPhen;
    private TextBox strainKeywords;
    private TextArea strainSummary;
    private TextArea strainReferences;
    private TextArea strainIp;
    private ListBox strainNotesMarkupOptions;
    private TextArea strainNotesArea;

    // plasmid fields
    private SuggestBox plasmidName;
    private TextBox plasmidAlias;
    private CheckBox circular;
    private TextBox backbone;
    private TextBox plasmidLinks;
    private SuggestBox plasmidMarkers;
    private SuggestBox origin;
    private SuggestBox promoters;
    private TextBox plasmidKeywords;
    private TextArea plasmidSummary;
    private TextArea plasmidReferences;
    private TextArea plasmidIp;
    private ListBox plasmidNotesMarkupOptions;
    private TextArea plasmidNotesArea;

    // submit cancel buttons
    private Button submit;
    private Button cancel;

    private final StrainInfo strain;
    private final PlasmidInfo plasmid;

    private final FlexTable layout;

    private HandlerRegistration cancelRegistration;
    private HandlerRegistration submitRegistration;

    public NewStrainWithPlasmidForm(StrainInfo strain) {
        this.layout = new FlexTable();
        initWidget(layout);
        initComponents();
        initLayout();

        this.strain = strain;
        this.plasmid = (PlasmidInfo) strain.getInfo();

        this.creator.setText(strain.getCreator());
        this.creatorEmail.setText(strain.getCreatorEmail());
    }

    protected void initComponents() {
        submit = new Button("Submit");
        submit.setStyleName("btn_submit_entry_form");
        cancel = new Button("Reset");
        cancel.setStyleName("btn_reset_entry_form");
    }

    protected void initLayout() {
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
        for (BioSafetyOption option : BioSafetyOption.values()) {
            bioSafety.addItem(option.getDisplayName(), option.getValue());
        }

        bioSafety.setStyleName("pull_down");
        general.setWidget(row, 3, bioSafety);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        status = new ListBox();
        status.setVisibleItemCount(1);
        for (StatusType type : StatusType.values()) {
            status.addItem(type.getDisplayName());
        }
        status.setStyleName("pull_down");
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
        plasmidName = createAutoCompleteForPlasmidNames("205px");
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
        setLabel(false, "Links", general, row, 0);
        plasmidLinks = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(plasmidLinks, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        setLabel(true, "Selection Markers", general, row, 0);
        plasmidMarkers = createAutoCompleteForSelectionMarkers("300px");
        widget = createTextBoxWithHelp(plasmidMarkers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // origin of replication
        row += 1;
        setLabel(false, "Origin of Replication", general, row, 0);
        origin = createAutoCompleteForOriginOfReplication("300px");
        widget = createTextBoxWithHelp(origin, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // promoters
        row += 1;
        setLabel(false, "Promoters", general, row, 0);
        promoters = createAutoCompleteForPromoters("300px");
        widget = createTextBoxWithHelp(promoters, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        setLabel(false, "Keywords", general, row, 0);
        plasmidKeywords = createStandardTextBox("640px");
        general.setWidget(row, 1, plasmidKeywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        setLabel(true, "Summary", general, row, 0);
        plasmidSummary = createTextArea("640px", "50px");
        general.setWidget(row, 1, plasmidSummary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        setLabel(false, "References", general, row, 0);
        plasmidReferences = createTextArea("640px", "50px");
        general.setWidget(row, 1, plasmidReferences);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        setLabel(false, "Intellectual Property", general, row, 0);
        plasmidIp = createTextArea("640px", "50px");
        general.setWidget(row, 1, plasmidIp);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    private SuggestBox createSuggestBox(AutoCompleteField field, String width) {
        AutoCompleteSuggestOracle oracle = new AutoCompleteSuggestOracle(field);
        SuggestBox box = new SuggestBox(oracle, new MultipleTextBox());
        box.setStyleName("input_box");
        box.setWidth(width);
        return box;
    }

    public SuggestBox createAutoCompleteForPromoters(String width) {
        return createSuggestBox(AutoCompleteField.PROMOTERS, width);
    }

    public SuggestBox createAutoCompleteForSelectionMarkers(String width) {
        return createSuggestBox(AutoCompleteField.SELECTION_MARKERS, width);
    }

    public SuggestBox createAutoCompleteForPlasmidNames(String width) {
        return createSuggestBox(AutoCompleteField.PLASMID_NAME, width);
    }

    public SuggestBox createAutoCompleteForOriginOfReplication(String width) {
        return this.createSuggestBox(AutoCompleteField.ORIGIN_OF_REPLICATION, width);
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

        notes.setWidget(2, 0, new HTML("<span class=\"font-85em\">Markup Type</span>"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        plasmidNotesMarkupOptions = new ListBox();
        plasmidNotesMarkupOptions.setVisibleItemCount(1);
        plasmidNotesMarkupOptions.addItem("Text");
        //        plasmidNotesMarkupOptions.addItem("Wiki");
        //        plasmidNotesMarkupOptions.addItem("Confluence");
        plasmidNotesMarkupOptions.setStyleName("pull_down");
        notes.setWidget(2, 1, plasmidNotesMarkupOptions);

        // input
        notes.setWidget(3, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        plasmidNotesArea = createTextArea("640px", "200px");
        notes.setWidget(3, 1, plasmidNotesArea);

        return notes;
    }

    protected void setLabel(boolean required, String label, FlexTable layout, int row, int col) {
        String html;
        if (required)
            html = "<span class=\"font-85em\" style=\"white-space:nowrap\">" + label
                    + "  <span class=\"required\">*</span></span>";
        else
            html = "<span class=\"font-85em\" style=\"white-space:nowrap\">" + label + "</span>";

        layout.setHTML(row, col, html);
        layout.getFlexCellFormatter().setVerticalAlignment(row, col, HasAlignment.ALIGN_TOP);
        layout.getFlexCellFormatter().setWidth(row, col, "170px");
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
        setLabel(false, "Alias", general, row, 0);
        strainAlias = createStandardTextBox("205px");
        general.setWidget(row, 1, strainAlias);

        // host strain
        row += 1;
        setLabel(false, "Parent Strain", general, row, 0);
        host = createStandardTextBox("300px");
        general.setWidget(row, 1, host);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // links
        row += 1;
        setLabel(false, "Links", general, row, 0);
        strainLinks = createStandardTextBox("300px");
        Widget widget = createTextBoxWithHelp(strainLinks, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        setLabel(true, "Selection Markers", general, row, 0);
        strainMarkers = createAutoCompleteForSelectionMarkers("300px");
        widget = createTextBoxWithHelp(strainMarkers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // Genotype/Phenotype
        row += 1;
        setLabel(false, "Genotype/Phenotype", general, row, 0);
        genPhen = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(genPhen, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        setLabel(false, "Keywords", general, row, 0);
        strainKeywords = createStandardTextBox("640px");
        general.setWidget(row, 1, strainKeywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        setLabel(true, "Summary", general, row, 0);
        strainSummary = createTextArea("640px", "50px");
        general.setWidget(row, 1, strainSummary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        setLabel(false, "References", general, row, 0);
        strainReferences = createTextArea("640px", "50px");
        general.setWidget(row, 1, strainReferences);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        setLabel(false, "Intellectual Property", general, row, 0);
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

        notes.setWidget(2, 0, new HTML("<span class=\"font-85em\">Markup Type</span>"));
        notes.getFlexCellFormatter().setStyleName(2, 0, "entry_add_sub_label");
        strainNotesMarkupOptions = new ListBox();
        strainNotesMarkupOptions.setVisibleItemCount(1);
        strainNotesMarkupOptions.addItem("Text");
        //        strainNotesMarkupOptions.addItem("Wiki");
        //        strainNotesMarkupOptions.addItem("Confluence");
        strainNotesMarkupOptions.setStyleName("pull_down");
        notes.setWidget(2, 1, strainNotesMarkupOptions);

        // input
        notes.setWidget(3, 0, new Label(""));
        notes.getFlexCellFormatter().setWidth(3, 0, "170px");

        strainNotesArea = createTextArea("640px", "200px");
        notes.setWidget(3, 1, strainNotesArea);

        return notes;
    }

    protected Widget createTextBoxWithHelp(Widget box, String helpText) {
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
    public void addSubmitHandler(ClickHandler handler) {
        if (submitRegistration != null)
            submitRegistration.removeHandler();

        submitRegistration = submit.addClickHandler(handler);
    }

    @Override
    public void addCancelHandler(ClickHandler handler) {
        if (cancelRegistration != null)
            cancelRegistration.removeHandler();
        cancelRegistration = this.cancel.addClickHandler(handler);
    }

    @Override
    public FocusWidget validateForm() {

        FocusWidget invalid = null;

        // strain number
        if (strainNumber.getText().isEmpty()) {
            strainNumber.setStyleName("input_box_error");
            invalid = strainNumber;
        } else {
            strainNumber.setStyleName("input_box");
        }

        // principal Investigator
        if (pI.getText().isEmpty()) {
            pI.setStyleName("input_box_error");
            if (invalid == null)
                invalid = pI;
        } else {
            pI.setStyleName("input_box");
        }

        // creator
        if (creator.getText().isEmpty()) {
            creator.setStyleName("input_box_error");
            if (invalid == null)
                invalid = creator;
        } else {
            creator.setStyleName("input_box");
        }

        // plasmid name
        if (plasmidName.getText().isEmpty()) {
            plasmidName.setStyleName("input_box_error");
            if (invalid == null)
                invalid = plasmidName.getValueBox();
        } else {
            plasmidName.setStyleName("input_box");
        }

        // plasmid summary
        if (plasmidSummary.getText().isEmpty()) {
            plasmidSummary.setStyleName("input_box_error");
            if (invalid == null)
                invalid = plasmidSummary;
        } else {
            plasmidSummary.setStyleName("input_box");
        }

        // strain summary
        if (strainSummary.getText().isEmpty()) {
            strainSummary.setStyleName("input_box_error");
            if (invalid == null)
                invalid = strainSummary;
        } else {
            strainSummary.setStyleName("input_box");
        }

        // strain markers
        if (strainMarkers.getText().isEmpty()) {
            strainMarkers.setStyleName("input_box_error");
            if (invalid == null)
                invalid = strainMarkers.getValueBox();
        } else {
            strainMarkers.setStyleName("input_box");
        }

        // plasmid markers
        if (plasmidMarkers.getText().isEmpty()) {
            plasmidMarkers.setStyleName("input_box_error");
            if (invalid == null)
                invalid = plasmidMarkers.getValueBox();
        } else {
            plasmidMarkers.setStyleName("input_box");
        }

        return invalid;
    }

    @Override
    public void populateEntries() {

        strain.setOwner(ClientController.account.getFullName());
        strain.setOwnerEmail(ClientController.account.getEmail());
        plasmid.setOwner(ClientController.account.getFullName());
        plasmid.setOwnerEmail(ClientController.account.getEmail());

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
        String strainSelectionMarkers = ((MultipleTextBox) strainMarkers.getValueBox()).getWholeText();
        strain.setSelectionMarkers(strainSelectionMarkers);
        strain.setGenotypePhenotype(genPhen.getText());
        strain.setKeywords(strainKeywords.getText());
        strain.setShortDescription(strainSummary.getText());
        strain.setReferences(strainReferences.getText());
        strain.setIntellectualProperty(strainIp.getText());
        strain.setLongDescription(this.strainNotesArea.getText());
        String longDescType = strainNotesMarkupOptions.getItemText(strainNotesMarkupOptions.getSelectedIndex());
        strain.setLongDescriptionType(longDescType);

        // plasmid fields
        plasmid.setName(plasmidName.getText());
        plasmid.setAlias(plasmidAlias.getText());
        plasmid.setCircular(circular.isEnabled());
        plasmid.setBackbone(backbone.getText());
        plasmid.setLinks(plasmidLinks.getText());
        String plasmidSelectionMarkers = ((MultipleTextBox) plasmidMarkers.getValueBox()).getWholeText();
        plasmid.setSelectionMarkers(plasmidSelectionMarkers);
        plasmid.setOriginOfReplication(origin.getText());
        plasmid.setPromoters(promoters.getText());
        plasmid.setKeywords(plasmidKeywords.getText());
        plasmid.setShortDescription(plasmidSummary.getText());
        plasmid.setReferences(plasmidReferences.getText());
        plasmid.setIntellectualProperty(plasmidIp.getText());
        plasmid.setLongDescription(this.plasmidNotesArea.getText());
        longDescType = plasmidNotesMarkupOptions.getItemText(plasmidNotesMarkupOptions.getSelectedIndex());
        plasmid.setLongDescriptionType(longDescType);
        strain.setInfo(plasmid);
    }

    @Override
    public void setPreferences(HashMap<PreferenceKey, String> preferences) {
        if (preferences.containsKey(PreferenceKey.FUNDING_SOURCE)) {
            fundingSource.setText(preferences.get(PreferenceKey.FUNDING_SOURCE));
        }

        if (preferences.containsKey(PreferenceKey.PRINCIPAL_INVESTIGATOR))
            pI.setText(preferences.get(PreferenceKey.PRINCIPAL_INVESTIGATOR));
    }

    @Override
    public EntryInfo getEntry() {
        return strain;
    }

    @Override
    public String getHeaderDisplay() {
        return EntryAddType.STRAIN_WITH_PLASMID.getDisplay();
    }
}
