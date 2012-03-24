package org.jbei.ice.client.collection;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SamplesDataProvider extends HasEntryDataViewDataProvider<SampleInfo> {

    public SamplesDataProvider(HasEntryDataTable<SampleInfo> view, RegistryServiceAsync service) {
        super(view, service, ColumnField.CREATED);
    }

    @Override
    protected void retrieveValues(LinkedList<Long> values, final int rangeStart,
            final int rangeEnd, boolean asc) {

        // check the cache first 
        if (results.size() >= rangeEnd) {
            LinkedList<SampleInfo> show = new LinkedList<SampleInfo>();
            show.addAll(results.subList(rangeStart, rangeEnd));
            updateRowData(rangeStart, show);
        } else {

            service.retrieveSampleInfo(AppController.sessionId, values, asc,
                new AsyncCallback<LinkedList<SampleInfo>>() {

                    @Override
                    public void onSuccess(LinkedList<SampleInfo> result) {
                        results.addAll(result);
                        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error retrieving sample values: " + caught.getMessage());
                    }
                });
        }
    }

    public void setValues(ArrayList<Long> data) {
        this.valueIds.clear();
        this.valueIds.addAll(data);
    }
}
