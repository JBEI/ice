package org.jbei.ice.client.bulkimport;

import org.jbei.ice.client.Callback;

public interface IDeleteMenuHandler {

    void delete(long id, Callback<BulkImportMenuItem> deleteCallback);
}
