package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.lib.dto.StorageInfo;

/**
 * @author Hector Plahar
 */
public class ShelfSample extends PartSample {

    private StorageInfo well;
    private StorageInfo tube;
    private StorageInfo box;

    public StorageInfo getWell() {
        return well;
    }

    public void setWell(StorageInfo well) {
        this.well = well;
    }

    public StorageInfo getTube() {
        return tube;
    }

    public void setTube(StorageInfo tube) {
        this.tube = tube;
    }

    public StorageInfo getBox() {
        return box;
    }

    public void setBox(StorageInfo box) {
        this.box = box;
    }
}
