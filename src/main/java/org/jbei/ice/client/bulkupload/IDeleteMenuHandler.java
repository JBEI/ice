package org.jbei.ice.client.bulkupload;

import org.jbei.ice.client.Callback;

public interface IDeleteMenuHandler {

    void delete(long id, Callback<BulkUploadMenuItem> deleteCallback);
}
