package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.lib.dto.StorageInfo;

/**
 * Data transfer object for a sample stored in a 96 well plate
 *
 * @author Hector Plahar
 */
public class Plate96Sample extends PartSample {

    private StorageInfo well;
    private StorageInfo tube;

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
}
