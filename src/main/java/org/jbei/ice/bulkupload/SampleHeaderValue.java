package org.jbei.ice.bulkupload;

import org.jbei.ice.dto.bulkupload.SampleField;

/**
 * @author Hector Plahar
 */
public class SampleHeaderValue implements HeaderValue {

    private final SampleField sampleField;

    SampleHeaderValue(SampleField field) {
        this.sampleField = field;
    }

    @Override
    public boolean isSampleField() {
        return true;
    }

    @Override
    public boolean isCustomField() {
        return false;
    }

    SampleField getSampleField() {
        return sampleField;
    }
}
