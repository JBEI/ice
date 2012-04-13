package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.StrainInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class UpdateStrainForm extends UpdateEntryForm<StrainInfo> {

    private ListBox status;
    private TextBox links;
    private TextBox host;
    private TextBox markers;
    private TextBox genPhen;
    private TextBox plasmids;
    private TextBox keywords;
    private TextArea references;
    private ListBox bioSafety;
    private TextArea ip;
    private TextBox fundingSource;

    public UpdateStrainForm(HashMap<AutoCompleteField, ArrayList<String>> data, StrainInfo info) {
        super(data, info);

        // fill out strain fields
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

        host.setText(info.getHost());
        genPhen.setText(info.getGenotypePhenotype());
        plasmids.setText(info.getPlasmids());
        links.setText(info.getLinks());
        markers.setText(info.getSelectionMarkers());
        keywords.setText(info.getKeywords());
        references.setText(info.getReferences());
        ip.setText(info.getIntellectualProperty());
    }

    @Override
    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        general.setWidget(row, 0, new HTML(
                "<span class=\"font-80em\">Name</span> <span class=\"required\">*</span>"));
        Widget widget = createTextBoxWithHelp(name, "e.g. JBEI-0001");
        general.setWidget(row, 1, widget);

        // alias
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Alias</span>"));
        general.getFlexCellFormatter().setWidth(row, 0, "170px");
        general.setWidget(row, 1, alias);

        // creator
        row += 1;
        general.setWidget(row, 0, new HTML(
                "<span class=\"font-80em\">Creator</span> <span class=\"required\">*</span>"));
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        row += 1;
        general.setWidget(
            row,
            0,
            new HTML(
                    "<span class=\"font-80em\">Principal Investigator</span> <span class=\"required\">*</span>"));
        general.setWidget(row, 1, principalInvestigator);

        // creator's email
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Creator's Email</span>"));
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Funding Source</span>"));
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 1, fundingSource);

        // status
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Status</span>"));
        status = new ListBox();
        status.setVisibleItemCount(1);
        status.addItem("Complete");
        status.addItem("In Progress");
        status.addItem("Planned");
        status.setStyleName("input_box");
        general.setWidget(row, 1, status);

        // bio safety level
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Bio Safety Level</span>"));
        bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName());
        bioSafety.setStyleName("input_box");
        general.setWidget(row, 1, bioSafety);

        // host strain
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Host Strain</span>"));
        host = createStandardTextBox("300px");
        general.setWidget(row, 1, host);
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
        general.getCellFormatter().setWidth(row, 0, "170px");
        markers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // Genotype/Phenotype [TODO : diff]
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Genotype/Phenotype</span>"));
        genPhen = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(genPhen, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // plasmids [TODO : diff]
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Plasmids</span>"));
        plasmids = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(plasmids, "Comma separated");
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
        general.setWidget(row, 0, new HTML(
                "<span class=\"font-80em\">Summary</span> <span class=\"required\">*</span>"));
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

    @Override
    public void populateEntry() {
        super.populateEntry();
        StrainInfo strain = getEntryInfo();
        strain.setName(name.getText());
        strain.setCreator(creator.getText());
        strain.setCreatorEmail(creatorEmail.getText());
        strain.setStatus(status.getValue(status.getSelectedIndex()));
        strain.setAlias(alias.getText());
        strain.setHost(host.getText());
        strain.setGenotypePhenotype(genPhen.getText());
        strain.setPlasmids(plasmids.getText());
        strain.setKeywords(keywords.getText());
        strain.setReferences(references.getText());
        int bioSafetySelectedIndex = bioSafety.getSelectedIndex();
        int value = Integer.parseInt(bioSafety.getValue(bioSafetySelectedIndex));
        strain.setBioSafetyLevel(value);
        strain.setIntellectualProperty(ip.getText());
        strain.setPrincipalInvestigator(principalInvestigator.getText());
    }
}
