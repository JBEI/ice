package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.EntryFields;

import java.util.ArrayList;
import java.util.List;

/**
 * Headers for each of the entry add types for bulk csv upload
 *
 * @author Hector Plahar
 */
class BulkCSVUploadHeaders {

    static List<EntryField> getHeadersForType(EntryType type) {
        if (type == null)
            return null;

        List<EntryField> list = EntryFields.getCommonFields();

        switch (type) {
            case SEED:
                EntryFields.addArabidopsisSeedHeaders(list);
                break;

            case STRAIN:
                EntryFields.addStrainHeaders(list);
                break;

            case PLASMID:
                EntryFields.addPlasmidHeaders(list);
                break;

            case PROTEIN:
                EntryFields.addProteinHeaders(list);
                break;

            default:
        }

        return list;
    }

    static List<EntryField> getFileHeaders() {
        List<EntryField> headers = new ArrayList<>(3);
        headers.add(EntryField.SEQ_TRACE_FILES);
        headers.add(EntryField.SEQ_FILENAME);
        headers.add(EntryField.ATT_FILENAME);
        return headers;
    }
}
