package org.jbei.ice.client.search.blast;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.HasEntryData;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.search.SearchResult;

import com.google.gwt.view.client.Range;

public class BlastSearchDataProvider extends HasEntryDataViewDataProvider<SearchResult> {

    public BlastSearchDataProvider(HasEntryDataTable<SearchResult> view, RegistryServiceAsync service) {
        super(view, service, ColumnField.BIT_SCORE);
    }

    public void setBlastData(LinkedList<SearchResult> data) {
        reset();
        if (data == null) {
            updateRowCount(0, true);
            return;
        }

        Collections.sort(data, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult o1, SearchResult o2) {
                return Float.compare(o1.getScore(), o2.getScore());
            }
        });

        results.addAll(data);
        resultSize = data.size();  // TODO : need a blastResult object as a wrapper
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        final Range range = this.dataTable.getVisibleRange();
        final int rangeStart = 0;
        int rangeEnd = range.getLength() > resultSize ? resultSize : range.getLength();
        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
        dataTable.setPageStart(0);
    }

    @Override
    public PartData getCachedData(long entryId, String recordId) {
        for (HasEntryData result : results) {
            PartData info = result.getEntryInfo();

            if (info.getId() == entryId)
                return info;
        }
        return null;
    }

    @Override
    public int indexOfCached(PartData info) {
        int i = 0;
        for (HasEntryData result : results) {

            if (result.getEntryInfo().getId() == info.getId())
                return i;
            i += 1;
        }
        return -1;
    }

    @Override
    public int getSize() {
        return resultSize;
    }

    @Override
    public PartData getNext(PartData info) {
        int idx = indexOfCached(info);
        if (idx == -1)
            return null;
        return results.get(idx + 1).getEntryInfo();
    }

    @Override
    public PartData getPrev(PartData info) {
        int idx = indexOfCached(info);
        if (idx == -1)
            return null;
        return results.get(idx - 1).getEntryInfo();
    }

    @Override
    protected void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, boolean reset) {
        //To change body of implemented methods use File | Settings | File Templates.
        // TODO
    }
}
