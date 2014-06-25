package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Status used to indicate whether a bulk upload is pending approval or still being worked
 * on. In cases where approval is not required, this is ignored
 *
 * @author Hector Plahar
 */
public enum BulkUploadStatus implements IDataTransferModel {

    PENDING_APPROVAL,

    IN_PROGRESS
}
