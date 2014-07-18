package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Arrays;
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

        List<EntryField> headers = new ArrayList<>(getPartHeaders());

        switch (type) {
            case ARABIDOPSIS:
                headers.addAll(getArabidopsisSeedHeaders());
                return headers;

            case STRAIN:
                headers.addAll(getStrainHeaders());
                return headers;

            case PLASMID:
                headers.addAll(getPlasmidHeaders());
                return headers;

            default:
                return headers;
        }
    }

    public static List<EntryField> getPartHeaders() {
        return Arrays.asList(EntryField.PI,
                             EntryField.PI_EMAIL,
                             EntryField.FUNDING_SOURCE,
                             EntryField.IP,
                             EntryField.BIOSAFETY_LEVEL,
                             EntryField.NAME,
                             EntryField.ALIAS,
                             EntryField.KEYWORDS,
                             EntryField.SUMMARY,
                             EntryField.NOTES,
                             EntryField.REFERENCES,
                             EntryField.LINKS,
                             EntryField.STATUS,
                             EntryField.SEQ_FILENAME,
                             EntryField.ATT_FILENAME);
    }

    public static List<EntryField> getStrainHeaders() {
        return Arrays.asList(EntryField.PARENTAL_STRAIN,
                             EntryField.GENOTYPE_OR_PHENOTYPE,
                             EntryField.PLASMIDS,
                             EntryField.SELECTION_MARKERS);
    }

    public static List<EntryField> getPlasmidHeaders() {
        return Arrays.asList(EntryField.CIRCULAR,
                             EntryField.BACKBONE,
                             EntryField.PROMOTERS,
                             EntryField.REPLICATES_IN,
                             EntryField.ORIGIN_OF_REPLICATION,
                             EntryField.SELECTION_MARKERS);
    }

    public static List<EntryField> getArabidopsisSeedHeaders() {
        return Arrays.asList(EntryField.HOMOZYGOSITY,
                             EntryField.HARVEST_DATE,
                             EntryField.ECOTYPE,
                             EntryField.PARENTS,
                             EntryField.GENERATION,
                             EntryField.PLANT_TYPE,
                             EntryField.GENERATION,
                             EntryField.SENT_TO_ABRC);
    }
}
