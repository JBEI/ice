package org.jbei.ice.lib.dto.bulkupload;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Sheet edit mode that is used to determine the sheet view to show to users
 *
 * @author Hector Plahar
 */
public enum EditMode implements IDataTransferModel {

    DEFAULT,  // default edit mode

    ADMIN_APPROVAL,

    BULK_EDIT,

    NEW
}
