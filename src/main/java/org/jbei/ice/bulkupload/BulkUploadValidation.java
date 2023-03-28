package org.jbei.ice.bulkupload;

import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.storage.model.BulkUploadModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Validation class for Bulk Upload Entries
 *
 * @author Hector Plahar
 */
public class BulkUploadValidation {

    private final Set<EntryFieldLabel> failedFields;

    public BulkUploadValidation(BulkUploadModel upload) {
        if (upload == null)
            throw new IllegalArgumentException("Cannot validate null upload");
        this.failedFields = new HashSet<>();
    }

    /**
     * @return the list of fields that have failed validation if called after isValid()
     * otherwise returns an empty list
     */
    public Set<EntryFieldLabel> getFailedFields() {
        return new HashSet<>(this.failedFields);
    }

}
