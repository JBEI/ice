package org.jbei.ice.lib.dto;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class Curation implements IDataTransferModel {

    private boolean exclude;

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }
}
