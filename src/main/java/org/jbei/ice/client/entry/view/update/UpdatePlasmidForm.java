package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.PlasmidInfo;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
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

public class UpdatePlasmidForm extends UpdateEntryForm<PlasmidInfo> {

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

    public UpdatePlasmidForm(HashMap<AutoCompleteField, ArrayList<String>> data, PlasmidInfo info) {
        super(data, info);

        // fill out plasmid fields
        this.fundingSource.setText(info.getFundingSource());
        for (int i = 0; i < this.status.getItemCount(); i += 1) {
            if (status.getValue(i).equals(info.getStatus())) {
                status.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < this.bioSafety.getItemCount(); i += 1) {
            if (bioSafety.getValue(i).equalsIgnoreCase(info.getBioSafetyLevel().toString())) {
                this.bioSafety.setSelectedIndex(i);
                break;
            }
        }
        circular.setValue(info.getCircular());
        backbone.setText(info.getBackbone());
        links.setText(info.getLinks());
        markers.setText(info.getSelectionMarkers());
        origin.setText(info.getOriginOfReplication());
        promoters.setText(info.getPromoters());
        keywords.setText(info.getKeywords());
        ip.setText(info.getIntellectualProperty());
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

    @Override
    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(2);
        general.setCellSpacing(0);

        // name
        addField(general, "Name", row, 0, name, "e.g. pTSH117", true);

        // alias
        row += 1;
        addField(general, "Alias", row, 0, alias, null, false);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        Widget widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        row += 1;
        setLabel(true, "Principal Investigator", general, row, 0);
        general.setWidget(row, 1, principalInvestigator);

        // creator's email
        row += 1;
        setLabel(false, "Creator's Email", general, row, 0);
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        row += 1;
        setLabel(false, "Funding Source", general, row, 0);
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 1, fundingSource);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        status = new ListBox();
        status.setVisibleItemCount(1);
        for (StatusType type : StatusType.values()) {
            status.addItem(type.getDisplayName());
        }
        status.setStyleName("input_box");
        general.setWidget(row, 1, status);

        // bio safety level
        row += 1;
        setLabel(false, "Bio Safety Level", general, row, 0);
        bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        for (BioSafetyOptions options : BioSafetyOptions.values()) {
            bioSafety.addItem(options.getDisplayName(), options.getValue());
        }
        bioSafety.setStyleName("input_box");
        general.setWidget(row, 1, bioSafety);

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

    protected String getStatus() {
        int selectedIndex = this.status.getSelectedIndex();
        return this.status.getValue(selectedIndex);
    }

    protected String getBioSafetyLevel() {
        int selected = this.bioSafety.getSelectedIndex();
        return this.bioSafety.getValue(selected);
    }

    @Override
    public void populateEntry() {
        super.populateEntry();

        // plasmid specific fields
        PlasmidInfo info = super.getEntryInfo();
        info.setSelectionMarkers(markers.getText());
        info.setLinks(this.links.getText());
        info.setName(this.name.getText());
        info.setStatus(getStatus());

        info.setReferences(this.references.getText());
        info.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel()));
        info.setIntellectualProperty(this.ip.getText());
        info.setFundingSource(this.fundingSource.getText());
        info.setPrincipalInvestigator(this.principalInvestigator.getText());
        info.setKeywords(this.keywords.getText());

        // below are the fields peculiar to this specialization
        info.setBackbone(this.backbone.getText());
        info.setOriginOfReplication(this.origin.getText());
        info.setPromoters(this.promoters.getText());
        info.setCircular(this.circular.getValue());
    }
}
