package org.jbei.ice.lib.dto.folder;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Type of folder sharing, which indicates where it will be displayed
 * and the kinds of operations that are permitted
 *
 * @author Hector Plahar
 */
public enum FolderType implements IDataTransferModel {
    PUBLIC("folders"),
    PRIVATE("folders"),
    SHARED("folders"),
    UPLOAD ("upload");

    private final String type;

    FolderType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
