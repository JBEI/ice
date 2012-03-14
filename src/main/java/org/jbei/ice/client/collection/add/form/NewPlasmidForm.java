package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.PlasmidInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form for creating a new plasmid
 * 
 * @author Hector Plahar
 */

public class NewPlasmidForm extends NewSingleEntryForm<PlasmidInfo> {

    private TextBox fundingSource;
    private ListBox status;
    private ListBox bioSafety;
    private CheckBox circular;
    private TextBox backbone;
    private TextBox links;
    private SuggestBox markers;
    private TextBox origin;
    private TextBox promoters;
    private TextBox keywords;
    private TextArea references;
    private TextArea ip;

    public NewPlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data, String creatorName,
            String creatorEmail) {
        super(data, creatorName, creatorEmail, new PlasmidInfo());
        initWidget(layout);
        initComponent();
    }

    protected void initComponent() {
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
            labelWidget = new HTML("<span class=\"font-80em\">" + label
                    + "</span> <span class=\"required\">*</span>");
        else
            labelWidget = new HTML("<span class=\"font-80em\">" + label + "</span>");

        layout.setWidget(row, col, labelWidget);
        layout.getFlexCellFormatter().setWidth(row, col, "170px");
    }

    private void addField(FlexTable table, String label, int row, int col, TextBox box,
            String help, boolean required) {
        setLabel(required, label, table, row, col);
        if (help != null) {
            Widget widget = createTextBoxWithHelp(box, help);
            table.setWidget(row, col + 1, widget);
        } else
            table.setWidget(row, col + 1, box);
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(2);
        general.setCellSpacing(0);

        // name
        addField(general, "Name", row, 0, name, "e.g. pTSH117", true);

        // alias
        addField(general, "Alias", row, 2, alias, null, false);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        Widget widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        setLabel(true, "Principal Investigator", general, row, 2);
        general.setWidget(row, 3, principalInvestigator);

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
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName(),
            BioSafetyOptions.LEVEL_ONE.getValue());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName(),
            BioSafetyOptions.LEVEL_TWO.getValue());
        bioSafety.setStyleName("input_box");
        general.setWidget(row, 3, bioSafety);

        // circular
        row += 1;
        setLabel(false, "Circular", general, row, 0);
        circular = new CheckBox();
        circular.setValue(true);
        general.setWidget(row, 1, circular);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // backbone
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Backbone</span>"));
        backbone = createStandardTextBox("300px");
        general.setWidget(row, 1, backbone);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // links
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Links</span>"));
        links = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(links, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Selection Markers</span>"));
        general.getCellFormatter().setWidth(8, 0, "170px");
        markers = createAutoCompleteForSelectionMarkers("300px");
        //        markers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // origin of replication
        row += 1;
        general.setWidget(row, 0,
            new HTML("<span class=\"font-80em\">Origin of Replication</span>"));
        origin = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(origin, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // promoters
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Promoters</span>"));
        promoters = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(promoters, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Keywords</span>"));
        keywords = createStandardTextBox("640px");
        general.setWidget(row, 1, keywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Summary</span> "
                + "<span class=\"required\">*</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        general.setWidget(row, 1, summary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">References</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        references = createTextArea("640px", "50px");
        general.setWidget(row, 1, references);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        general.setWidget(row, 0,
            new HTML("<span class=\"font-80em\">Intellectual Property</span>"));
        general.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
        ip = createTextArea("640px", "50px");
        general.setWidget(row, 1, ip);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    protected Widget createTextBoxWithHelp(Widget box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText
                + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
    }

    protected String getAlias() {
        return this.alias.getText();
    }

    protected String getCreator() {
        return this.creator.getText();
    }

    protected String getCreatorEmail() {
        return this.creatorEmail.getText();
    }

    protected String getStatus() {
        int selectedIndex = this.status.getSelectedIndex();
        return this.status.getValue(selectedIndex);
    }

    protected String getSummary() {
        return this.summary.getText();
    }

    protected String getBioSafetyLevel() {
        int selected = this.bioSafety.getSelectedIndex();
        return this.bioSafety.getValue(selected);
    }

    protected String getFundingSource() {
        return this.fundingSource.getText();
    }

    protected String getPrincipalInvestigator() {
        return this.principalInvestigator.getText();
    }

    protected String getKeywords() {
        return this.keywords.getText();
    }

    protected String getReferences() {
        return this.references.getText();
    }

    protected String getLinks() {
        return this.links.getText();
    }

    protected String getName() {
        return this.name.getText();
    }

    protected String getMarkers() {
        return this.markers.getText();
    }

    @Override
    public void populateEntries() {
        super.populateEntries();

        // plasmid specific fields
        PlasmidInfo info = super.getEntryInfo();
        info.setSelectionMarkers(markers.getText());
        info.setLinks(getLinks());
        info.setName(getName());
        info.setSelectionMarkers(getMarkers());

        info.setAlias(getAlias());
        info.setCreator(getCreator());
        info.setCreatorEmail(getCreatorEmail());
        info.setStatus(getStatus());
        info.setKeywords(getKeywords());
        info.setShortDescription(getSummary());
        info.setReferences(getReferences());
        info.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel()));
        info.setIntellectualProperty(this.ip.getText());
        info.setFundingSource(getFundingSource());
        info.setPrincipalInvestigator(getPrincipalInvestigator());

        // below are the fields peculiar to this specialization
        info.setBackbone(getBackbone());
        info.setOriginOfReplication(getOriginOfReplication());
        info.setPromoters(getPromoters());
        info.setCircular(getCircular());
    }

    private String getBackbone() {
        String text = this.backbone.getText();
        if (text == null)
            return "";
        return text;
    }

    private String getOriginOfReplication() {
        return this.origin.getText();
    }

    private String getPromoters() {
        return this.promoters.getText();
    }

    private boolean getCircular() {
        return this.circular.getValue();
    }

    @Override
    public Button getSubmit() {
        return submit;
    }

    @Override
    public Button getCancel() {
        return cancel;
    }
}
