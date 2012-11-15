package org.jbei.ice.client.collection.add.form;

import org.jbei.ice.client.common.widget.MultipleTextBox;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.Generation;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo.PlantType;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class ArabidopsisForm extends SingleEntryForm<ArabidopsisSeedInfo> {

    private ListBox generation;
    private ListBox plantType;
    private TextBox homozygosity;
    private SuggestBox markers;
    private TextBox ecoType;
    private TextBox parents;
    private DateBox harvestDate;
    private CheckBox sentToAbrc;

    public ArabidopsisForm(ArabidopsisSeedInfo seedInfo) {
        super(seedInfo);

        // fill out values if any exist
        for (PlantType type : PlantType.values()) {
            plantType.addItem(type.toString(), type.name());
        }

        for (Generation gen : Generation.values()) {
            generation.addItem(gen.toString(), gen.name());
        }

        for (int i = 0; i < this.generation.getItemCount(); i += 1) {
            if (this.generation.getValue(i).equalsIgnoreCase(seedInfo.getGeneration().name())) {
                this.generation.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < this.plantType.getItemCount(); i += 1) {
            if (this.plantType.getValue(i).equalsIgnoreCase(seedInfo.getPlantType().name())) {
                this.plantType.setSelectedIndex(i);
                break;
            }
        }

        homozygosity.setText(seedInfo.getHomozygosity());
        markers.setText(seedInfo.getSelectionMarkers());
        ecoType.setText(seedInfo.getEcotype());
        parents.setText(seedInfo.getParents());
        harvestDate.setValue(seedInfo.getHarvestDate());
        sentToAbrc.setValue(seedInfo.isSentToAbrc());
    }

    protected void initComponents() {
        super.initComponents();
        markers = createAutoCompleteForSelectionMarkers("300px");
        generation = new ListBox();
        generation.setVisibleItemCount(1);
        for (Generation gen : Generation.values()) {
            generation.addItem(gen.toString(), gen.name());
        }
        generation.setStyleName("pull_down");

        plantType = new ListBox();
        plantType.setVisibleItemCount(1);
        for (PlantType type : PlantType.values()) {
            plantType.addItem(type.toString(), type.name());
        }
        plantType.setStyleName("pull_down");

        harvestDate = new DateBox();
        harvestDate.setStyleName("input_box");

        DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
        harvestDate.setWidth("205px");
        harvestDate.setFormat(new DateBox.DefaultFormat(dateFormat));

        homozygosity = createStandardTextBox("300px");
        ecoType = createStandardTextBox("300px");
        parents = createStandardTextBox("300px");
        sentToAbrc = new CheckBox();
    }

    protected Widget createGeneralWidget() {
        int row = 0;
        FlexTable general = new FlexTable();
        general.setWidth("100%");
        general.setStyleName("no_wrap");
        general.setCellPadding(3);
        general.setCellSpacing(0);

        // name
        setLabel(true, "Name", general, row, 0);
        Widget widget = createTextBoxWithHelp(name, "e.g. Stock ID / Mutant Name");
        general.setWidget(row, 1, widget);

        // alias
        setLabel(false, "Alias", general, row, 2);
        general.setWidget(row, 3, alias);

        // creator
        row += 1;
        setLabel(true, "Creator", general, row, 0);
        widget = createTextBoxWithHelp(creator, "Who made this part?");
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
        general.setWidget(row, 3, fundingSource);

        // status
        row += 1;
        setLabel(false, "Status", general, row, 0);
        general.setWidget(row, 1, status);

        // bio safety level
        setLabel(false, "Bio Safety Level", general, row, 2);
        general.setWidget(row, 3, bioSafety);

        // generation
        row += 1;
        setLabel(false, "Generation", general, row, 0);
        general.setWidget(row, 1, generation);

        // sent to abrc
        setLabel(false, "Sent To ABRC", general, row, 2);
        general.setWidget(row, 3, sentToAbrc);

        // plant type
        row += 1;
        setLabel(false, "Plant Type", general, row, 0);
        general.setWidget(row, 1, plantType);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // harvest date
        row += 1;
        setLabel(false, "Harvest Date", general, row, 0);
        general.setWidget(row, 1, harvestDate);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // homozygosity
        row += 1;
        setLabel(false, "Homozygosity", general, row, 0);
        general.setWidget(row, 1, homozygosity);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // links
        row += 1;
        setLabel(false, "Links", general, row, 0);
        widget = createTextBoxWithHelp(links, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // selection markers
        row += 1;
        setLabel(false, "Selection Markers", general, row, 0);
        widget = createTextBoxWithHelp(markers, "Comma separated");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // eco type
        row += 1;
        setLabel(false, "Ecotype", general, row, 0);
        widget = createTextBoxWithHelp(ecoType, "If known");
        general.setWidget(row, 1, widget);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // parents
        row += 1;
        setLabel(false, "Parents", general, row, 0);
        general.setWidget(row, 1, parents);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // keywords
        row += 1;
        setLabel(false, "Keywords", general, row, 0);
        general.setWidget(row, 1, keywords);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // summary
        row += 1;
        setLabel(true, "Summary", general, row, 0);
        general.setWidget(row, 1, summary);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // references
        row += 1;
        setLabel(false, "References", general, row, 0);
        general.setWidget(row, 1, references);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        // intellectual property
        row += 1;
        setLabel(false, "Intellectual Property", general, row, 0);
        general.setWidget(row, 1, ip);
        general.getFlexCellFormatter().setColSpan(row, 1, 3);

        return general;
    }

    @Override
    public void populateEntries() {
        super.populateEntries();

        ArabidopsisSeedInfo seed = super.getEntryInfo();
        String selectionMarkers = ((MultipleTextBox) markers.getValueBox()).getWholeText();
        seed.setSelectionMarkers(selectionMarkers);

        Generation gen = Generation.valueOf(generation.getValue(generation.getSelectedIndex()));
        seed.setGeneration(gen);
        PlantType type = PlantType.valueOf(plantType.getValue(plantType.getSelectedIndex()));
        seed.setPlantType(type);
        seed.setHomozygosity(homozygosity.getText());
        seed.setEcotype(this.ecoType.getText());
        seed.setHarvestDate(this.harvestDate.getDatePicker().getValue());
        seed.setParents(parents.getText());
        seed.setSentToAbrc(sentToAbrc.getValue());
    }
}
