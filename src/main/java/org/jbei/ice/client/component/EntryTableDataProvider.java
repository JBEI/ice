package org.jbei.ice.client.component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.EntryDataView;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class EntryTableDataProvider extends AsyncDataProvider<EntryDataView> {

    private final List<Long> values;
    private final LinkedList<EntryDataView> results;
    private RegistryServiceAsync service;

    public EntryTableDataProvider(List<Long> data, RegistryServiceAsync service) {
        this.values = new LinkedList<Long>(data);
        this.service = service;

        // set total number available
        updateRowCount(data.size(), true);
        results = new LinkedList<EntryDataView>();
    }

    @Override
    public void updateRowCount(int size, boolean exact) {
        super.updateRowCount(size, exact);
    }

    @Override
    public void updateRowData(int start, List<EntryDataView> values) {
        super.updateRowData(start, values);
    }

    @Override
    protected void onRangeChanged(final HasData<EntryDataView> display) {

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > values.size() ? values.size()
                : (rangeStart + range.getLength());

        //        GWT.log("Showing " + rangeStart + " to " + rangeEnd);

        // check if it has been prefetched
        if (results.size() >= rangeEnd) {

            // we have the data
            ArrayList<EntryDataView> show = new ArrayList<EntryDataView>();
            show.addAll(results.subList(rangeStart, rangeEnd));
            updateRowData(rangeStart, show);
        } else {

            // id range to display
            int factor = (rangeEnd - rangeStart) * 9;
            factor = (factor + rangeEnd) > values.size() ? values.size() : (factor + rangeEnd);
            List<Long> subList = values.subList(rangeStart, factor);
            final ArrayList<Long> realValues = new ArrayList<Long>(subList);

            //            GWT.log("Fetching " + rangeStart + " to " + factor);
            //            TODO : pass this as a callback function instead of having this provider has-a service impl
            service.retrieveEntryViews(realValues, new AsyncCallback<ArrayList<EntryDataView>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Failed: " + caught.getMessage());
                }

                @Override
                public void onSuccess(ArrayList<EntryDataView> result) {
                    //                    GWT.log("Adding " + result.size() + " to results");
                    results.addAll(result);
                    //                    GWT.log("Results is now " + results.size());
                    updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
                }
            });
        }
    }
}
