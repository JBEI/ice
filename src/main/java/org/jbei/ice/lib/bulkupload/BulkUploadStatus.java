package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Status used to indicate whether a bulk upload is pending
 * approval or still being worked
 * on. In cases where approval is not required, this is ignored
 *
 * @author Hector Plahar
 */
public enum BulkUploadStatus implements IDataTransferModel {

    APPROVED,
    PENDING_APPROVAL,
    BULK_EDIT,
    IN_PROGRESS
}
