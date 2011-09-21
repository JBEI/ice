package org.jbei.ice.client.search;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.HasEntryDataViewDataProvider;
import org.jbei.ice.client.component.table.HasEntryDataTable;
import org.jbei.ice.shared.dto.BlastResultInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.Range;

class BlastSearchDataProvider extends HasEntryDataViewDataProvider<BlastResultInfo> {

    public BlastSearchDataProvider(HasEntryDataTable<BlastResultInfo> view,
            ArrayList<BlastResultInfo> data, RegistryServiceAsync service) {

        super(view, service);

        long i = 1;
        for (BlastResultInfo info : data) {
            info.setId(i);
            i += 1;
            valueIds.add(i);
        }

        results.clear();
        results.addAll(data);
        updateRowCount(data.size(), true);
        //        this.setValues(valueIds);
        final Range range = getDataTable().getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd;
        if ((rangeStart + range.getLength()) > valueIds.size())
            rangeEnd = valueIds.size();
        else
            rangeEnd = (rangeStart + range.getLength());

        // TODO : you have access to the sort info from the table
        // TODO : this goes with the above todo. if we clear all the sort info then we use default else use the top sort
        // TODO : look at the sort method for an example of how to do this
        fetchHasEntryData(rangeStart, rangeEnd);
    }

    @Override
    protected void retrieveValues(LinkedList<Long> values, final int rangeStart,
            final int rangeEnd, boolean asc) {
        // check the cache first 
        if (results.size() >= rangeEnd) {
            LinkedList<BlastResultInfo> show = new LinkedList<BlastResultInfo>();
            show.addAll(results.subList(rangeStart, rangeEnd));
            updateRowData(rangeStart, show);
        } else {

            // TODO : blast, All results are returned. Need to redo
            Window.alert("Could not page");
            //            service.retrieveSampleInfo(AppController.sessionId, values, asc,
            //                new AsyncCallback<LinkedList<BlastResultInfo>>() {
            //
            //                    @Override
            //                    public void onSuccess(LinkedList<BlastResultInfo> result) {
            //                        results.addAll(result);
            //                        //                        Window.alert("Retrieved samples from [" + rangeStart + " to " + rangeEnd
            //                        //                                + "]. Data size is " + result.size() + ". First - "
            //                        //                                + result.get(0).getId() + ", Last - "
            //                        //                                + result.get(result.size() - 1).getId());
            //                        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
            //                    }
            //
            //                    @Override
            //                    public void onFailure(Throwable caught) {
            //                        Window.alert("Error retrieving sample values: " + caught.getMessage());
            //                    }
            //                });
        }

    }

}
