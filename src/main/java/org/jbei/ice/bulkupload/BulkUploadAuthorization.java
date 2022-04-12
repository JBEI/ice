package org.jbei.ice.bulkupload;

import org.jbei.ice.access.Authorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.BulkUpload;

/**
 * @author Hector Plahar
 */
public class BulkUploadAuthorization extends Authorization<BulkUpload> {

    public BulkUploadAuthorization() {
        super(DAOFactory.getBulkUploadDAO());
    }
}
