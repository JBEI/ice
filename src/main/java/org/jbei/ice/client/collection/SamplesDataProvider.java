package org.jbei.ice.client.collection;

import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.HasEntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SamplesDataProvider extends HasEntryDataViewDataProvider<SampleInfo> implements IHasNavigableData {

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

    @Override
    public EntryInfo getCachedData(long entryId) {
        for (HasEntryInfo result : results) {

            if (result.getEntryInfo().getId() == entryId)
                return result.getEntryInfo();
        }
        return null;
    }

    @Override
    public int indexOfCached(EntryInfo info) {
        int i = 0;
        for (HasEntryInfo result : results) {

            if (result.getEntryInfo().getId() == info.getId())
                return i;
            i += 1;
        }
        return -1;
    }

    @Override
    public int getSize() {
        return valueIds.size();
    }

    @Override
    public EntryInfo getNext(EntryInfo info) {
        int idx = indexOfCached(info);
        if (idx == -1)
            return null;
        return results.get(idx + 1).getEntryInfo();
    }

    @Override
    public EntryInfo getPrev(EntryInfo info) {
        int idx = indexOfCached(info);
        if (idx == -1)
            return null;
        return results.get(idx - 1).getEntryInfo();
    }
}
