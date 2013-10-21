package org.jbei.ice.server.servlet.helper;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

/**
 * Headers for each of the entry add types for bulk csv upload
 *
 * @author Hector Plahar
 */
public class BulkCSVUploadHeaders {

    public static ArrayList<EntryField> getHeadersForType(EntryAddType type) {
        if (type == null)
            return null;

        switch (type) {
            case ARABIDOPSIS:
                return getArabidopsisSeedHeaders();

            case STRAIN:
                return getStrainHeaders();

            case STRAIN_WITH_PLASMID:
                return getStrainWithPlasmidHeaders();

            case PART:
                return getPartHeaders();

            case PLASMID:
                return getPlasmidHeaders();

            default:
                return null;
        }
    }

    public static ArrayList<EntryField> getPartHeaders() {
        ArrayList<EntryField> headerFields = new ArrayList<>();
        headerFields.add(EntryField.PI);
        headerFields.add(EntryField.FUNDING_SOURCE);
        headerFields.add(EntryField.IP);
        headerFields.add(EntryField.BIOSAFETY_LEVEL);
        headerFields.add(EntryField.NAME);
        headerFields.add(EntryField.ALIAS);
        headerFields.add(EntryField.KEYWORDS);
        headerFields.add(EntryField.SUMMARY);
        headerFields.add(EntryField.NOTES);
        headerFields.add(EntryField.REFERENCES);
        headerFields.add(EntryField.LINKS);
        headerFields.add(EntryField.STATUS);
        return headerFields;
    }

    public static ArrayList<EntryField> getStrainHeaders() {
        ArrayList<EntryField> headerFields = new ArrayList<>();
        headerFields.addAll(getPartHeaders());
        headerFields.add(EntryField.PARENTAL_STRAIN);
        headerFields.add(EntryField.GENOTYPE_OR_PHENOTYPE);
        headerFields.add(EntryField.PLASMIDS);
        headerFields.add(EntryField.SELECTION_MARKERS);
        return headerFields;
    }

    public static ArrayList<EntryField> getPlasmidHeaders() {
        ArrayList<EntryField> headerFields = new ArrayList<>();
        headerFields.addAll(getPartHeaders());
        headerFields.add(EntryField.CIRCULAR);
        headerFields.add(EntryField.BACKBONE);
        headerFields.add(EntryField.PROMOTERS);
        headerFields.add(EntryField.REPLICATES_IN);
        headerFields.add(EntryField.ORIGIN_OF_REPLICATION);
        headerFields.add(EntryField.SELECTION_MARKERS);
        return headerFields;
    }

    public static ArrayList<EntryField> getArabidopsisSeedHeaders() {
        ArrayList<EntryField> headerFields = new ArrayList<>();
        headerFields.addAll(getPartHeaders());
        headerFields.add(EntryField.HOMOZYGOSITY);
        headerFields.add(EntryField.HARVEST_DATE);
        headerFields.add(EntryField.ECOTYPE);
        headerFields.add(EntryField.PARENTS);
        headerFields.add(EntryField.GENERATION);
        headerFields.add(EntryField.PLANT_TYPE);
        headerFields.add(EntryField.GENERATION);
        headerFields.add(EntryField.SENT_TO_ABRC);
        return headerFields;
    }

    public static ArrayList<EntryField> getStrainWithPlasmidHeaders() {
        ArrayList<EntryField> headerFields = new ArrayList<>();
        headerFields.add(EntryField.PI);
        headerFields.add(EntryField.FUNDING_SOURCE);
        headerFields.add(EntryField.IP);
        headerFields.add(EntryField.BIOSAFETY_LEVEL);
        headerFields.add(EntryField.STATUS);

        //strain information
        headerFields.add(EntryField.STRAIN_NAME);
        headerFields.add(EntryField.STRAIN_ALIAS);
        headerFields.add(EntryField.STRAIN_LINKS);
        headerFields.add(EntryField.STRAIN_SELECTION_MARKERS);
        headerFields.add(EntryField.STRAIN_PARENTAL_STRAIN);
        headerFields.add(EntryField.STRAIN_GEN_PHEN);
        headerFields.add(EntryField.STRAIN_KEYWORDS);
        headerFields.add(EntryField.STRAIN_SUMMARY);
        headerFields.add(EntryField.STRAIN_NOTES);
        headerFields.add(EntryField.STRAIN_REFERENCES);

        // plasmid information
        headerFields.add(EntryField.PLASMID_NAME);
        headerFields.add(EntryField.PLASMID_ALIAS);
        headerFields.add(EntryField.PLASMID_LINKS);
        headerFields.add(EntryField.PLASMID_SELECTION_MARKERS);
        headerFields.add(EntryField.CIRCULAR);
        headerFields.add(EntryField.PLASMID_BACKBONE);
        headerFields.add(EntryField.PLASMID_PROMOTERS);
        headerFields.add(EntryField.REPLICATES_IN);
        headerFields.add(EntryField.PLASMID_ORIGIN_OF_REPLICATION);
        headerFields.add(EntryField.PLASMID_KEYWORDS);
        headerFields.add(EntryField.PLASMID_SUMMARY);
        headerFields.add(EntryField.PLASMID_NOTES);
        headerFields.add(EntryField.PLASMID_REFERENCES);

        headerFields.add(EntryField.PARENTAL_STRAIN);
        headerFields.add(EntryField.GENOTYPE_OR_PHENOTYPE);
        headerFields.add(EntryField.PLASMIDS);
        headerFields.add(EntryField.SELECTION_MARKERS);
        return headerFields;
    }
}
