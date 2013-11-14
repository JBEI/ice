package org.jbei.ice.lib.shared.dto.sample;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.entry.PartData;

/**
 * Data transfer object for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequest implements IDTOModel {

    private SampleRequestType requestType;
    private PartData partData;
    private SampleRequestStatus requestStatus;

    public SampleRequest() {
    }

    public SampleRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(SampleRequestType requestType) {
        this.requestType = requestType;
    }

    public PartData getPartData() {
        return partData;
    }

    public void setPartData(PartData partData) {
        this.partData = partData;
    }

    public SampleRequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(SampleRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}
