package org.jbei.ice.client.bulkupload;

import org.jbei.ice.client.Callback;

/**
 * @author Hector Plahar
 */
public interface IRevertBulkUploadHandler {

    void revert(long id, Callback<Long> revertCallback);
}
