package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.user.client.ui.FlexTable;

public class StrainHeaders extends SheetHeader {

    public StrainHeaders(int col, int row, FlexTable headerTable) {
        super(col, row, headerTable);
    }

    enum Header implements IHeader {
        PI("Principal Investigator"), FUNDING_SOURCE("Funding Source"), IP("Intellectual Property"), BIOSAFETY(
                "BioSafety Level"), NAME("Name"), ALIAS("Alias"), KEYWORDS("Keywords"), SUMMARY(
                "Summary"), NOTES("Notes"), REFERENCES("References"), LINKS("Links"), STATUS(
                "Status"), SEQ_FILENAME("Sequence Filename"), ATT_FILENAME("Attachments Filename"), SELECTION_MARKERS(
                "Selection Markers"), PARENTAL_STRAIN("Parental Strain"), GEN_PHEN(
                "Genotype/Phenotype"), PLASMIDS("Plasmids");

        private String label;

        Header(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

}
