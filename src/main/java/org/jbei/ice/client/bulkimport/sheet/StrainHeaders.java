package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.user.client.ui.FlexTable;

public class StrainHeaders extends SheetHeader {

    public StrainHeaders(int col, int row, FlexTable headerTable) {
        super(col, row, headerTable);
    }

    // TODO : use proper class to enable sub-classing from SheetHeader parent
    public enum Header implements IHeader {
        PI("Principal Investigator", true), FUNDING_SOURCE("Funding Source", false), IP(
                "Intellectual Property", false), BIOSAFETY("BioSafety Level", true), NAME("Name",
                true), ALIAS("Alias", false), KEYWORDS("Keywords", false), SUMMARY("Summary", true), NOTES(
                "Notes", false), REFERENCES("References", false), LINKS("Links", false), STATUS(
                "Status", true), SEQ_FILENAME("Sequence Filename", false), ATT_FILENAME(
                "Attachments Filename", false), SELECTION_MARKERS("Selection Markers", false), PARENTAL_STRAIN(
                "Parental Strain", false), GEN_PHEN("Genotype or Phenotype", false), PLASMIDS(
                "Plasmids", false);

        private String label;
        private boolean required;

        Header(String label, boolean required) {
            this.label = label;
            this.required = required;
        }

        public boolean isRequired() {
            return this.required;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }
}
