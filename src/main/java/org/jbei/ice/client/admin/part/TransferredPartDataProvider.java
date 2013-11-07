package org.jbei.ice.client.admin.part;

import java.util.ArrayList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * Data provider for the transferred part table
 *
 * @author Hector Plahar
 */
public class TransferredPartDataProvider extends EntryDataViewDataProvider {

    public TransferredPartDataProvider(DataTable<PartData> view, RegistryServiceAsync service) {
        super(view, service);
    }

    @Override
    protected void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, boolean reset) {
    }

    @Override
    protected void onRangeChanged(HasData<PartData> display) {
        if (resultSize == 0)   // display changed its range of interest but no data
            return;

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > resultSize ? resultSize
                : (rangeStart + range.getLength());

        // user is sorting
        if (sortChanged(rangeEnd)) {
            fetchEntryData(lastSortField, lastSortAsc, 0, range.getLength(), true);
            return;
        }

        // sort did not change, use data in cache
        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
    }

    public void setData(ArrayList<PartData> data, boolean resetSort) {
        if (resetSort)
            lastSortField = null;

        reset();  // lastSortField is set reset to CREATED if set to null (resetSort == true)
        if (data == null) {
            updateRowCount(0, true);
            return;
        }

        cachedEntries.addAll(data);
        resultSize = data.size();
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        Range range = this.table.getVisibleRange();
        int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
        table.setPageStart(0);
    }
}
