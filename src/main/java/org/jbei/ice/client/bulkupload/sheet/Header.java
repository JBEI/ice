package org.jbei.ice.client.bulkupload.sheet;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkupload.sheet.cell.BioSafetySheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.DateInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.FileInputCell;
import org.jbei.ice.client.bulkupload.sheet.cell.GenerationSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.InputSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.MultiSuggestSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.PlantTypeSheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.client.bulkupload.sheet.cell.StatusSheetCell;
import org.jbei.ice.shared.AutoCompleteField;

public enum Header {
    PI("Principal Investigator", true, "", new InputSheetCell()),
    FUNDING_SOURCE("Funding Source", false, "", new InputSheetCell()),
    IP("Intellectual Property", false, "", new InputSheetCell()),
    BIOSAFETY("BioSafety Level", true, "", new BioSafetySheetCell()),
    NAME("Name", true, "e.g. JBEI-0001", new InputSheetCell()),
    ALIAS("Alias", false, "", new InputSheetCell()),
    KEYWORDS("Keywords", false, "", new InputSheetCell()),
    SUMMARY("Summary", true, "", new InputSheetCell()),
    NOTES("Notes", false, "", new InputSheetCell()),
    REFERENCES("References", false, "", new InputSheetCell()),
    LINKS("Links", false, "", new InputSheetCell()),
    STATUS("Status", true, "", new StatusSheetCell()),
    SEQ_FILENAME("Sequence File", false, "", new FileInputCell()),
    ATT_FILENAME("Attachment File", false, "", new FileInputCell()),
    SELECTION_MARKERS("Selection Markers", false, "", new MultiSuggestSheetCell(AppController.autoCompleteData.get(
            AutoCompleteField.SELECTION_MARKERS))),
    PARENTAL_STRAIN("Parental Strain", false, "", new InputSheetCell()),
    GEN_PHEN("Genotype or Phenotype", false, "", new InputSheetCell()),
    PLASMIDS("Plasmids", false, "", new MultiSuggestSheetCell(AppController.autoCompleteData.get(
            AutoCompleteField.PLASMID_NAME))),
    CIRCULAR("Circular", false, "", new InputSheetCell()),
    BACKBONE("Backbone", false, "", new InputSheetCell()),
    PROMOTERS("Promoters", false, "", new MultiSuggestSheetCell(AppController.autoCompleteData.get(
            AutoCompleteField.PROMOTERS))),
    ORIGIN_OF_REPLICATION("Origin of Replication", false, "", new MultiSuggestSheetCell(
            AppController.autoCompleteData.get(AutoCompleteField.ORIGIN_OF_REPLICATION))),
    HOMOZYGOSITY("Homozygosity", false, "", new InputSheetCell()),
    ECOTYPE("Ecotype", false, "", new InputSheetCell()),
    HARVEST_DATE("Harvest Date", false, "", new DateInputCell()),
    GENERATION("Generation", true, "", new GenerationSheetCell()),
    PLANT_TYPE("Plant Type", true, "", new PlantTypeSheetCell()),
    PARENTS("Parents", false, "", new InputSheetCell()),
    PLASMID_NAME("Plasmid Name", true, "e.g. pTSH117", new InputSheetCell()),
    PLASMID_ALIAS("Plasmid Alias", false, "", new InputSheetCell()),
    PLASMID_KEYWORDS("Keywords", false, "", new InputSheetCell()),
    PLASMID_SUMMARY("Summary", true, "", new InputSheetCell()),
    PLASMID_NOTES("Notes", false, "", new InputSheetCell()),
    PLASMID_REFERENCES("References", false, "", new InputSheetCell()),
    PLASMID_LINKS("Links", false, "", new InputSheetCell()),
    PLASMID_STATUS("Status", true, "", new StatusSheetCell()),
    PLASMID_BACKBONE("Backbone", false, "", new InputSheetCell()),
    PLASMID_PROMOTERS("Promoters", false, "", new MultiSuggestSheetCell(
            AppController.autoCompleteData.get(AutoCompleteField.PROMOTERS))),
    PLASMID_ORIGIN_OF_REPLICATION("Origin of Replication", false, "", new MultiSuggestSheetCell(
            AppController.autoCompleteData.get(AutoCompleteField.ORIGIN_OF_REPLICATION))),
    PLASMID_SEQ_FILENAME("Sequence File", false, "", new FileInputCell()),
    PLASMID_ATT_FILENAME("Attachment File", false, "", new FileInputCell()),
    PLASMID_SELECTION_MARKERS("Selection Markers", false, "", new MultiSuggestSheetCell(
            AppController.autoCompleteData.get(AutoCompleteField.SELECTION_MARKERS))),
    STRAIN_NAME("Name", true, "", new InputSheetCell()),
    STRAIN_ALIAS("Alias", false, "", new InputSheetCell()),
    STRAIN_KEYWORDS("Keywords", false, "", new InputSheetCell()),
    STRAIN_SUMMARY("Summary", true, "", new InputSheetCell()),
    STRAIN_NOTES("Notes", false, "", new InputSheetCell()),
    STRAIN_REFERENCES("References", false, "", new InputSheetCell()),
    STRAIN_LINKS("Links", false, "", new InputSheetCell()),
    STRAIN_STATUS("Status", true, "", new StatusSheetCell()),
    STRAIN_SELECTION_MARKERS("Selection Markers", false, "", new MultiSuggestSheetCell(
            AppController.autoCompleteData.get(AutoCompleteField.SELECTION_MARKERS))),
    STRAIN_PARENTAL_STRAIN("Parental Strain", false, "", new InputSheetCell()),
    STRAIN_GEN_PHEN("Genotype or Phenotype", false, "", new InputSheetCell()),
    STRAIN_PLASMIDS("Plasmids", false, "", new MultiSuggestSheetCell(
            AppController.autoCompleteData.get(AutoCompleteField.PLASMID_NAME))),
    STRAIN_SEQ_FILENAME("Sequence File", false, "", new FileInputCell()),
    STRAIN_ATT_FILENAME("Attachment File", false, "", new FileInputCell());

    private String label;
    private String description;
    private transient SheetCell cell;

    Header(String label, boolean required, String description, SheetCell sheetCell) {
        this.label = label;
        this.description = description;
        this.cell = sheetCell;
        this.cell.setRequired(required);
    }

    @Override
    public String toString() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    public SheetCell getCell() {
        return this.cell;
    }
}