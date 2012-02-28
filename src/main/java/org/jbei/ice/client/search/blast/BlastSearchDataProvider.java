package org.jbei.ice.client.search.blast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.BlastResultInfo;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class BlastSearchDataProvider extends HasEntryDataViewDataProvider<BlastResultInfo> {

    public BlastSearchDataProvider(HasEntryDataTable<BlastResultInfo> view,
            ArrayList<BlastResultInfo> data, RegistryServiceAsync service) {

        super(view, service, ColumnField.BIT_SCORE);

        for (BlastResultInfo info : data) {
            valueIds.add(info.getEntryInfo().getId());
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

        //        view.setVisibleRangeAndClearData(range, false);

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

            // TODO : with blast, all results are returned need to redo
            Window.alert("Results has size " + results.size() + " but requesting range ["
                    + rangeStart + ", " + rangeEnd + "]");
        }
    }

    @Override
    protected void onRangeChanged(HasData<BlastResultInfo> display) {
        if (results.isEmpty()) // problem here is that when the display is added to the dataProvider, onRangeChanged() is triggered
            return;

        final Range range = display.getVisibleRange();
        final ColumnSortList sortList = this.getDataTable().getColumnSortList();
        int start = range.getStart();
        int end = range.getLength() + start;
        if (end > results.size())
            end = results.size();

        sortByColumn(this.getSortField(), sortList.get(0).isAscending());
        this.getDataTable().setRowData(start, results.subList(start, end));
    }

    protected void sortByColumn(final ColumnField field, final boolean asc) {
        // sort the data by the sortList
        Collections.sort(results, new Comparator<BlastResultInfo>() {

            @Override
            public int compare(BlastResultInfo o1, BlastResultInfo o2) {
                if (o1 == o2)
                    return 0;

                int diff = -1;

                switch (field) {
                case TYPE:
                    diff = o1.getEntryInfo().getType().toString()
                            .compareToIgnoreCase(o2.getEntryInfo().getType().toString());
                    break;

                case PART_ID:
                    diff = o1.getEntryInfo().getPartId().compareTo(o2.getEntryInfo().getPartId());
                    break;

                case NAME:
                    diff = o1.getEntryInfo().getName().compareTo(o2.getEntryInfo().getName());
                    break;

                case ALIGNED_BP:
                    diff = (o1.getAlignmentLength() < o2.getAlignmentLength()) ? -1 : 1;
                    break;

                case ALIGNED_IDENTITY:
                    diff = (o1.getPercentId() < o2.getPercentId()) ? -1 : 1;
                    break;

                case BIT_SCORE:
                    diff = (o1.getBitScore() < o2.getBitScore()) ? -1 : 1;
                    break;

                case E_VALUE:
                    diff = (o1.geteValue() < o2.geteValue()) ? -1 : 1;
                    break;
                }

                return asc ? diff : -diff;
            }
        });
    }

    public void setData(ArrayList<BlastResultInfo> data) {

        valueIds.clear();

        for (BlastResultInfo info : data) {
            valueIds.add(info.getEntryInfo().getId());
        }

        this.results.clear();
        this.results.addAll(data);

        updateRowCount(this.valueIds.size(), true);

        lastSortAsc = false;
        lastSortField = this.defaultSort;

        // retrieve the first page of results and updateRowData
        final Range range = this.dataTable.getVisibleRange();
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
}
