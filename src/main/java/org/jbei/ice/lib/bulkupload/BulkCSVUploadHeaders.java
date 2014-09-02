package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;

/**
 * Headers for each of the entry add types for bulk csv upload
 *
 * @author Hector Plahar
 */
public class BulkCSVUploadHeaders {

    public static List<EntryField> getHeadersForType(EntryType type) {
        if (type == null)
            return null;

        switch (type) {
            case ARABIDOPSIS:
                return getArabidopsisSeedHeaders();

            case STRAIN:
                return getStrainHeaders();

            case PLASMID:
                return getPlasmidHeaders();

            default:
            case PART:
                return getPartHeaders();
        }
    }

    protected static List<EntryField> getCommonFields() {
        List<EntryField> list = new ArrayList<>();
        list.add(EntryField.PI);
        list.add(EntryField.PI_EMAIL);
        list.add(EntryField.FUNDING_SOURCE);
        list.add(EntryField.IP);
        list.add(EntryField.BIOSAFETY_LEVEL);
        list.add(EntryField.NAME);
        list.add(EntryField.ALIAS);
        list.add(EntryField.KEYWORDS);
        list.add(EntryField.SUMMARY);
        list.add(EntryField.NOTES);
        list.add(EntryField.REFERENCES);
        list.add(EntryField.LINKS);
        list.add(EntryField.STATUS);
        list.add(EntryField.CREATOR);
        list.add(EntryField.CREATOR_EMAIL);
        return list;
    }

    protected static void adddFileHeaders(List<EntryField> headers) {
        headers.add(EntryField.SEQ_TRACE_FILES);
        headers.add(EntryField.SEQ_FILENAME);
        headers.add(EntryField.ATT_FILENAME);
    }

    public static List<EntryField> getPartHeaders() {
        List<EntryField> list = getCommonFields();
        adddFileHeaders(list);
        return list;
    }

    public static List<EntryField> getStrainHeaders() {
        List<EntryField> list = getCommonFields();
        list.add(EntryField.PARENTAL_STRAIN);
        list.add(EntryField.GENOTYPE_OR_PHENOTYPE);
        list.add(EntryField.PLASMIDS);
        list.add(EntryField.SELECTION_MARKERS);
        adddFileHeaders(list);
        return list;
    }

    public static List<EntryField> getPlasmidHeaders() {
        List<EntryField> list = getCommonFields();
        list.add(EntryField.CIRCULAR);
        list.add(EntryField.BACKBONE);
        list.add(EntryField.PROMOTERS);
        list.add(EntryField.REPLICATES_IN);
        list.add(EntryField.ORIGIN_OF_REPLICATION);
        list.add(EntryField.SELECTION_MARKERS);
        adddFileHeaders(list);
        return list;
    }

    public static List<EntryField> getArabidopsisSeedHeaders() {
        List<EntryField> list = getCommonFields();
        list.add(EntryField.HOMOZYGOSITY);
        list.add(EntryField.HARVEST_DATE);
        list.add(EntryField.ECOTYPE);
        list.add(EntryField.PARENTS);
        list.add(EntryField.GENERATION);
        list.add(EntryField.PLANT_TYPE);
        list.add(EntryField.GENERATION);
        list.add(EntryField.SENT_TO_ABRC);
        adddFileHeaders(list);
        return list;
    }
}
