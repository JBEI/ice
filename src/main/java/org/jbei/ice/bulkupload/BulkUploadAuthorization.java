package org.jbei.ice.bulkupload;

import org.jbei.ice.access.Authorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.BulkUploadModel;

/**
 * @author Hector Plahar
 */
public class BulkUploadAuthorization extends Authorization<BulkUploadModel> {

    public BulkUploadAuthorization() {
        super(DAOFactory.getBulkUploadDAO());
    }
}
