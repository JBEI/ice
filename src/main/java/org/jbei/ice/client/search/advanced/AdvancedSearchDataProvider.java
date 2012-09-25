package org.jbei.ice.client.search.advanced;

import java.util.LinkedList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SearchResultInfo;

import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class AdvancedSearchDataProvider extends HasEntryDataViewDataProvider<SearchResultInfo>
        implements IHasNavigableData {

    private final AdvancedSearchResultsTable table;
    private final RegistryServiceAsync service;
    private final LinkedList<SearchResultInfo> data;

    public AdvancedSearchDataProvider(AdvancedSearchResultsTable table, RegistryServiceAsync rpcService) {

        super(table, rpcService, ColumnField.RELEVANCE);
        this.table = table;
        this.service = rpcService;
        this.data = new LinkedList<SearchResultInfo>();
    }

    @Override
    public EntryInfo getCachedData(long id) {
        for (SearchResultInfo info : data) {
            if (info.getEntryInfo().getId() == id)
                return info.getEntryInfo();
        }
        return null;
    }

    @Override
    public int indexOfCached(EntryInfo info) {
        for (int i = 0; i < data.size(); i += 1) {
            SearchResultInfo searchResultInfo = data.get(i);
            if (searchResultInfo.getEntryInfo().getId() == info.getId())
                return i;
        }
        return 0;
    }

    @Override
    public int getSize() {
        return data.size();  //To change body of implemented methods use File | Settings | File Templates.
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

    /**
     * @param values     list of ids for records to retrieve
     * @param rangeStart start of range to show
     * @param rangeEnd   end of range to show
     * @param asc        sort Type
     */
    @Override
    protected void retrieveValues(LinkedList<Long> values, int rangeStart, int rangeEnd, ColumnField sortField,
            boolean asc) {
        if (results.size() >= rangeEnd) {
            LinkedList<SearchResultInfo> show = new LinkedList<SearchResultInfo>();
            show.addAll(results.subList(rangeStart, rangeEnd));
            updateRowData(rangeStart, show);
        } else {

            // TODO : with blast, all results are returned need to redo
            //            Window.alert("Results has size " + results.size() + " but requesting range ["
            //                    + rangeStart + ", " + rangeEnd + "]");
        }
    }

//    @Override
//    protected void onRangeChanged(HasData<SearchResultInfo> display) {
//
//        if (results.isEmpty()) // problem here is that when the display is added to the dataProvider,
//            // onRangeChanged() is triggered
//            return;
//
//        final Range range = display.getVisibleRange();
//        final ColumnSortList sortList = this.getDataTable().getColumnSortList();
//        int start = range.getStart();
//        int end = range.getLength() + start;
//        if (end > results.size())
//            end = results.size();
//
//        sortByColumn(this.getSortField(), sortList.get(0).isAscending());
//        this.getDataTable().setRowData(start, results.subList(start, end));
//    }

    public void setData(LinkedList<SearchResultInfo> data) {
        reset();

        if (data != null) {
            for (SearchResultInfo info : data) {  // TODO : user iterator?
                valueIds.add(info.getEntryInfo().getId());
                results.add(info);
            }
        }

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
        fetchHasEntryData(this.getSortField(), true, rangeStart, rangeEnd);
    }
}
