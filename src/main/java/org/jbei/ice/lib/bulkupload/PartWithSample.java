package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.sample.PartSample;

/**
 * Wrapper around PartData and Part Sample objects that belong together.
 * i.e., if used for entry creation, the sample should be associated with the data for PartData
 *
 * @author Hector Plahar
 */
public class PartWithSample {
    private PartSample partSample;
    private PartData partData;

    public PartWithSample(PartSample partSample, PartData partData) {
        this.partSample = partSample;
        this.partData = partData;
    }

    public PartSample getPartSample() {
        return partSample;
    }

    public PartData getPartData() {
        return partData;
    }
}
