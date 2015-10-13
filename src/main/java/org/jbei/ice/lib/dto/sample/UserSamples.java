package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Wrapper around user sample requests to include information such as the total count
 *
 * @author Hector Plahar
 */
public class UserSamples implements IDataTransferModel {

    private int count;
    private ArrayList<SampleRequest> requests;

    public UserSamples() {
        requests = new ArrayList<>();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<SampleRequest> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<SampleRequest> requests) {
        this.requests = requests;
    }
}
