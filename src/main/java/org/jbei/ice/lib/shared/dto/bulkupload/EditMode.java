package org.jbei.ice.lib.shared.dto.bulkupload;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Sheet edit mode that is used to determine the sheet view to show to users
 *
 * @author Hector Plahar
 */
public enum EditMode implements IDTOModel {

    DEFAULT,  // default edit mode

    ADMIN_APPROVAL,

    BULK_EDIT,

    NEW
}
