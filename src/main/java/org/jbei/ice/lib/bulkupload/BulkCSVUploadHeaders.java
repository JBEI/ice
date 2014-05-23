package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;

/**
 * Headers for each of the entry add types for bulk csv upload
 *
 * @author Hector Plahar
 */
public class BulkCSVUploadHeaders {

    public static ArrayList<EntryField> getHeadersForType(EntryType type) {
        if (type == null)
            return null;

        switch (type) {
            case ARABIDOPSIS:
                return getArabidopsisSeedHeaders();

            case STRAIN:
                return getStrainHeaders();

            case PART:
                return getPartHeaders();

            case PLASMID:
                return getPlasmidHeaders();

            default:
                return null;
        }
    }

    public static boolean isRequired(EntryField field, EntryType addType) {
        if (addType == null || field == null)
            throw new IllegalArgumentException("Both field and addType are required");

        if (field == EntryField.PI ||
                field == EntryField.BIOSAFETY_LEVEL ||
                field == EntryField.NAME ||
                field == EntryField.SUMMARY ||
                field == EntryField.STATUS)
            return true;

        switch (addType) {
            case PLASMID:
                return field == EntryField.SELECTION_MARKERS ||
                        field == EntryField.CIRCULAR;

            case STRAIN:
                return field == EntryField.SELECTION_MARKERS;

            case ARABIDOPSIS:
                return field == EntryField.GENERATION ||
                        field == EntryField.PLANT_TYPE ||
                        field == EntryField.SENT_TO_ABRC;

            default:
                return false;
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
        headerFields.add(EntryField.SEQ_FILENAME);
        headerFields.add(EntryField.ATT_FILENAME);
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
}
