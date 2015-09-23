package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.bulkupload.SampleField;

/**
 * @author Hector Plahar
 */
public class SampleHeaderValue implements HeaderValue {

    private final SampleField sampleField;

    public SampleHeaderValue(SampleField field) {
        this.sampleField = field;
    }

    @Override
    public boolean isSampleField() {
        return true;
    }

    public SampleField getSampleField() {
        return sampleField;
    }
}
