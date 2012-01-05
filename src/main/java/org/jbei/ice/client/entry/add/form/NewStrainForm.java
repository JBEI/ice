package org.jbei.ice.client.entry.add.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.StrainInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NewStrainForm extends NewSingleEntryForm<StrainInfo> {

    private TextBox name;
    private ListBox status;
    private TextBox alias;
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
    private TextBox pI;

    public NewStrainForm(HashMap<AutoCompleteField, ArrayList<String>> data, String creatorName,
            String creatorEmail) {
        super(data, creatorName, creatorEmail, new StrainInfo());
        initWidget(layout);
        init();
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

    // Note that this needs to be in the respective specialized classes
    private Widget createTextBoxWithHelp(TextBox box, String helpText) {
        String html = "<span id=\"box_id\"></span><span class=\"help_text\">" + helpText
                + "</span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.addAndReplaceElement(box, "box_id");
        return panel;
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        general.setWidget(row, 0, new HTML("Name <span class=\"required\">*</span>"));
        Widget widget = createTextBoxWithHelp(name, "e.g. JBEI-0001");
        general.setWidget(row, 1, widget);

        // alias
        general.setWidget(row, 2, new Label("Alias"));
        general.getFlexCellFormatter().setWidth(row, 2, "170px");
        general.setWidget(row, 3, alias);

        // creator
        row += 1;
        general.setWidget(row, 0, new HTML("Creator <span class=\"required\">*</span>"));
        widget = createTextBoxWithHelp(creator, "Who made this part?");
        general.setWidget(row, 1, widget);

        // PI
        general.setWidget(row, 2, new HTML(
                "Principal Investigator <span class=\"required\">*</span>"));
        general.setWidget(row, 3, pI);

        // creator's email
        row += 1;
        general.setWidget(row, 0, new Label("Creator's Email"));
        widget = createTextBoxWithHelp(creatorEmail, "If known");
        general.setWidget(row, 1, widget);

        // funding source
        general.setWidget(row, 2, new Label("Funding Source"));
        fundingSource = createStandardTextBox("205px");
        general.setWidget(row, 3, fundingSource);

        // status
        row += 1;
        general.setWidget(row, 0, new Label("Status"));
        status = new ListBox();
        status.setVisibleItemCount(1);
        status.addItem("Complete");
        status.addItem("In Progress");
        status.addItem("Planned");
        status.setStyleName("input_box");
        general.setWidget(row, 1, status);

        // bio safety level
        general.setWidget(row, 2, new Label("Bio Safety Level"));
        bioSafety = new ListBox();
        bioSafety.setVisibleItemCount(1);
        bioSafety.addItem(BioSafetyOptions.LEVEL_ONE.getDisplayName());
        bioSafety.addItem(BioSafetyOptions.LEVEL_TWO.getDisplayName());
        bioSafety.setStyleName("input_box");
        general.setWidget(row, 3, bioSafety);

        // host strain
        row += 1;
        general.setWidget(row, 0, new Label("Host Strain"));
        host = createStandardTextBox("300px");
        general.setWidget(row, 1, host);
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
        general.getCellFormatter().setWidth(row, 0, "170px");
        markers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // Genotype/Phenotype [TODO : diff]
        row += 1;
        general.setWidget(row, 0, new Label("Genotype/Phenotype"));
        genPhen = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(genPhen, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // plasmids [TODO : diff]
        row += 1;
        general.setWidget(row, 0, new Label("Plasmids"));
        plasmids = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(plasmids, "Comma separated");
        general.setWidget(row, 1, widget);
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

    @Override
    public void populateEntries() {
        super.populateEntries();
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
        strain.setBioSafetyLevel(bioSafety.getSelectedIndex());
        strain.setIntellectualProperty(ip.getText());
        strain.setPrincipalInvestigator(pI.getText());
    }

    @Override
    public Set<EntryInfo> getEntries() {
        Set<EntryInfo> entries = new HashSet<EntryInfo>();
        entries.add(getEntryInfo());
        return entries;
    }
}
