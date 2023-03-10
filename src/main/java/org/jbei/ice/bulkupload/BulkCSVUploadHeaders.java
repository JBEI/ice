package org.jbei.ice.bulkupload;

import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.dto.entry.EntryType;

import java.util.ArrayList;
import java.util.List;

/**
 * Headers for each of the entry add types for bulk csv upload
 *
 * @author Hector Plahar
 */
class BulkCSVUploadHeaders {

    static List<EntryFieldLabel> getHeadersForType(EntryType type) {
        if (type == null)
            return null;

        return switch (type) {
            default -> EntryFieldLabel.getPartLabels();
            case PLASMID -> EntryFieldLabel.getPlasmidLabels();
            case STRAIN -> EntryFieldLabel.getStrainLabels();
            case SEED -> EntryFieldLabel.getSeedLabels();
            case PROTEIN -> EntryFieldLabel.getProteinFields();
        };
    }

    static List<EntryFieldLabel> getFileHeaders() {
        List<EntryFieldLabel> headers = new ArrayList<>(3);
        headers.add(EntryFieldLabel.SEQ_TRACE_FILES);
        headers.add(EntryFieldLabel.SEQ_FILENAME);
        headers.add(EntryFieldLabel.ATT_FILENAME);
        return headers;
    }
}
