package org.jbei.ice.client.collection;

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
            final int rangeEnd, ColumnField sortField, boolean asc) {

        service.retrieveSampleInfo(AppController.sessionId, values, sortField, asc,
            new AsyncCallback<LinkedList<SampleInfo>>() {

                @Override
                public void onSuccess(LinkedList<SampleInfo> result) {
                    results.addAll(result);
                    int end = rangeEnd;
                    if (rangeEnd > results.size())
                        end = results.size();
                    updateRowData(rangeStart, results.subList(rangeStart, end));
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error retrieving sample values: " + caught.getMessage());
                }
            });
    }
}
