package org.jbei.ice.lib.shared.dto.sample;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Data transfer object for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequest implements IDTOModel {

    private long entryId;
    private SampleRequestType requestType;

    public SampleRequest() {
    }


    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public SampleRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(SampleRequestType requestType) {
        this.requestType = requestType;
    }
}
