package org.jbei.ice.shared;

import java.io.Serializable;

public class PartTipView extends EntryDataView implements Serializable {

    private static final long serialVersionUID = 1L;

    private String packagingFormat;

    public void setPackagingFormat(String packagingFormat) {
        this.packagingFormat = packagingFormat;
    }

    public String getPackagingFormat() {
        return packagingFormat;
    }
}
