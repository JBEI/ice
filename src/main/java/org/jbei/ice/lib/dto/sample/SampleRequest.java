package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for sample requests
 *
 * @author Hector Plahar
 */
public class SampleRequest implements IDataTransferModel {

    private long id;
    private AccountTransfer requester;
    private SampleRequestType requestType;
    private PartData partData;
    private SampleRequestStatus status;
    private long requestTime;
    private long updateTime;
    private int growthTemperature;
    private List<PartSample> location;

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

    public SampleRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SampleRequestStatus status) {
        this.status = status;
    }

    public AccountTransfer getRequester() {
        return requester;
    }

    public void setRequester(AccountTransfer requester) {
        this.requester = requester;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getGrowthTemperature() {
        return growthTemperature;
    }

    public void setGrowthTemperature(int growthTemperature) {
        this.growthTemperature = growthTemperature;
    }

    public List<PartSample> getLocation() {
        return location;
    }

    public void setLocation(List<PartSample> location) {
        this.location = new ArrayList<>(location);
    }
}
