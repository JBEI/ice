package org.jbei.ice.client.entry.view.update;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.BioSafetyOptions;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class UpdateArabidopsisForm extends UpdateEntryForm<ArabidopsisSeedInfo> {

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
    private DateBox harvestDate;
    private TextBox keywords;
    private TextArea references;
    private TextArea ip;

    public UpdateArabidopsisForm(HashMap<AutoCompleteField, ArrayList<String>> data,
            ArabidopsisSeedInfo info) {
        super(data, info);

        // fill out fields
        this.fundingSource.setText(info.getFundingSource());

        // status
        for (int i = 0; i < this.status.getItemCount(); i += 1) {
            if (status.getValue(i).equals(info.getStatus())) {
                status.setSelectedIndex(i);
                break;
            }
        }

        // biosafety
        for (int i = 0; i < this.bioSafety.getItemCount(); i += 1) {
            if (bioSafety.getValue(i).equalsIgnoreCase(info.getBioSafetyLevel().toString())) {
                this.bioSafety.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < this.generation.getItemCount(); i += 1) {
            if (this.generation.getValue(i).equalsIgnoreCase(info.getGeneration().name())) {
                this.generation.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < this.plantType.getItemCount(); i += 1) {
            if (this.plantType.getValue(i).equalsIgnoreCase(info.getPlantType().name())) {
                this.plantType.setSelectedIndex(i);
                break;
            }
        }

        homozygosity.setText(info.getHomozygosity());
        links.setText(info.getLinks());
        markers.setText(info.getSelectionMarkers());
        ecoType.setText(info.getEcotype());
        parents.setText(info.getParents());
        harvestDate.setValue(info.getHarvestDate());
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
        setLabel(true, "Name", general, row, 0);
        Widget widget = createTextBoxWithHelp(name, "e.g. Stock ID / Mutant Name");
        general.setWidget(row, 1, widget);

        // alias
        row += 1;
        setLabel(false, "Alias", general, row, 0);
        general.setWidget(row, 1, alias);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        widget = createTextBoxWithHelp(creator, "Who made this part?");
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

        // generation
        row += 1;
        setLabel(false, "Generation", general, row, 0);
        generation = new ListBox();
        generation.setVisibleItemCount(1);
        for (Generation gen : Generation.values()) {
            generation.addItem(gen.toString(), gen.name());
        }
        generation.setStyleName("input_box");
        general.setWidget(row, 1, generation);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // plant type
        row += 1;
        setLabel(false, "Plant Type", general, row, 0);
        plantType = new ListBox();
        plantType.setVisibleItemCount(1);
        for (PlantType type : PlantType.values()) {
            plantType.addItem(type.toString(), type.name());
        }
        plantType.setStyleName("input_box");
        general.setWidget(row, 1, plantType);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // harvest date
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Harvest Date</span>"));
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
        harvestDate = new DateBox();
        harvestDate.setStyleName("input_box");
        harvestDate.setWidth("205px");
        harvestDate.setFormat(new DateBox.DefaultFormat(dateFormat));
        general.setWidget(row, 1, harvestDate);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // homozygosity
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Homozygosity</span>"));
        homozygosity = createStandardTextBox("300px");
        general.setWidget(row, 1, homozygosity);
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
        markers = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // eco type
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Ecotype</span>"));
        general.getCellFormatter().setWidth(8, 0, "170px");
        ecoType = createStandardTextBox("300px");
        widget = createTextBoxWithHelp(ecoType, "If known");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // parents
        row += 1;
        general.setWidget(row, 0, new HTML("<span class=\"font-80em\">Parents</span>"));
        parents = createStandardTextBox("300px");
        general.setWidget(row, 1, parents);
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

        // plasmid specific fields
        ArabidopsisSeedInfo seed = super.getEntryInfo();
        seed.setSelectionMarkers(markers.getText());
        seed.setLinks(this.links.getText());
        seed.setName(this.name.getText());
        seed.setStatus(status.getValue(status.getSelectedIndex()));

        seed.setReferences(this.references.getText());
        int bioSafetySelectedIndex = bioSafety.getSelectedIndex();
        int value = Integer.parseInt(bioSafety.getValue(bioSafetySelectedIndex));
        seed.setBioSafetyLevel(value);
        seed.setIntellectualProperty(this.ip.getText());
        seed.setFundingSource(this.fundingSource.getText());
        seed.setPrincipalInvestigator(this.principalInvestigator.getText());
        seed.setKeywords(this.keywords.getText());

        // below are the fields peculiar to this specialization
        Generation gen = Generation.valueOf(generation.getValue(generation.getSelectedIndex()));
        seed.setGeneration(gen);
        PlantType type = PlantType.valueOf(plantType.getValue(plantType.getSelectedIndex()));
        seed.setPlantType(type);
        seed.setHomozygosity(homozygosity.getText());
        seed.setEcotype(this.ecoType.getText());
        seed.setHarvestDate(this.harvestDate.getDatePicker().getValue());
        seed.setParents(parents.getText());
    }
}
