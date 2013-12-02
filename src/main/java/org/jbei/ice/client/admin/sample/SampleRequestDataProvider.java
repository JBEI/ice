package org.jbei.ice.client.admin.sample;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.shared.dto.sample.SampleRequest;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

/**
 * @author Hector Plahar
 */
public class SampleRequestDataProvider extends AsyncDataProvider<SampleRequest> {

    private ArrayList<SampleRequest> data;

    public SampleRequestDataProvider(SampleRequestTable table) {
        this.addDataDisplay(table);
        data = new ArrayList<SampleRequest>();
    }

    @Override
    protected void onRangeChanged(HasData<SampleRequest> display) {
        // todo To change body of implemented methods use File | Settings | File Templates.
    }

    public void setData(ArrayList<SampleRequest> requests) {
        data.clear();
        if(requests == null || requests.isEmpty())
        {
            updateRowCount(0, true);
            return;
        }

        updateRowData(0, requests);
        data.addAll(requests);
    }

    public void updateRow(SampleRequest existing, SampleRequest request) {
        int index = data.indexOf(existing);
        if (index == -1)
            return;
        List<SampleRequest> toUpdate = new ArrayList<SampleRequest>();
        toUpdate.add(request);
        this.updateRowData(index, toUpdate);
    }
}
