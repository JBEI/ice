package org.jbei.ice.lib.dto.folder;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Type of folder sharing, which indicates where it will be displayed
 * and the kinds of operations that are permitted
 *
 * @author Hector Plahar
 */
public enum FolderType implements IDataTransferModel {
    PUBLIC,     // featured folder
    PRIVATE,    // same as no type; personal folder
    SHARED,     // folder shared with other users or group
    UPLOAD;     // bulk upload; implicit folder

    FolderType() {
    }
}
