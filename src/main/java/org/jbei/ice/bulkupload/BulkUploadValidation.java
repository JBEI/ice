package org.jbei.ice.bulkupload;

import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
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
    private final BulkUploadModel model;
    private final BulkUploadDAO dao;

    public BulkUploadValidation(String userId, long uploadId) {
        this.dao = DAOFactory.getBulkUploadDAO();
        this.model = this.dao.get(uploadId);

        if (this.model == null)
            throw new IllegalArgumentException("Invalid bulk upload id: " + uploadId);
        this.failedFields = new HashSet<>();
    }

    public void perform() {

    }

    /**
     * @return the list of fields that have failed validation if called after isValid()
     * otherwise returns an empty list
     */
    public Set<EntryFieldLabel> getFailedFields() {
        return new HashSet<>(this.failedFields);
    }

}
