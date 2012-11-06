package org.jbei.ice.client.search.advanced;

import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SearchResultInfo;
import org.jbei.ice.shared.dto.SearchResults;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class AdvancedSearchDataProvider extends HasEntryDataViewDataProvider<SearchResultInfo>
        implements IHasNavigableData {

    private final RegistryServiceAsync service;
    private final LinkedList<SearchResultInfo> data;
    private SearchResults searchResults;
    private int resultSize;

    public AdvancedSearchDataProvider(AdvancedSearchResultsTable table, RegistryServiceAsync rpcService) {
        super(table, rpcService, ColumnField.RELEVANCE);
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
        return resultSize;
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

//    protected void fetchHasEntryData(ColumnField sortField, boolean asc, final int rangeStart, final int rangeEnd) {
//
//    }


    @Override
    protected void onRangeChanged(final HasData<SearchResultInfo> display) {

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > resultSize ? resultSize
                : (rangeStart + range.getLength());

        //
        // sort method start
        //
        ColumnSortList sortList = this.dataTable.getColumnSortList();
        boolean sortAsc = lastSortAsc;
        final ColumnField sortField;
        int colIndex = -1;
        if (sortList.size() > 0) {
            sortAsc = sortList.get(0).isAscending();
            colIndex = this.dataTable.getColumns().indexOf(sortList.get(0).getColumn());
        }

        if (colIndex < 0)
            sortField = lastSortField;
        else
            sortField = this.dataTable.getColumns().get(colIndex).getField();

        // sorting or paging
        if (lastSortAsc == sortAsc && lastSortField == sortField) {
            // paging; check if enough records are cached
            if (results.size() >= rangeEnd) {
                updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
            } else {
                // not so fetch more; fetch more
                retrieveValues(null, rangeStart, rangeEnd, sortField, sortAsc);
            }
        } else {
            // sort has changed so restart using new sort params
            results.clear();
            lastSortAsc = sortAsc;
            lastSortField = sortField;
            retrieveValues(null, rangeStart, rangeEnd, sortField, sortAsc);
        }
    }

    /**
     * @param values     list of ids for records to retrieve
     * @param rangeStart start of range to show
     * @param rangeEnd   end of range to show
     * @param asc        sort Type
     */
    protected void retrieveValues(LinkedList<Long> values, int rangeStart, int rangeEnd, ColumnField sortField,
            boolean asc) {
        int factor = (rangeEnd - rangeStart) * 2; // factor is used to retrieve more data than will be shown; for
        // caching
        factor = (factor + rangeEnd) > resultSize ? resultSize : (factor + rangeEnd);
        service.retrieveSearchResults(AppController.sessionId, searchResults.getSearchFilters(), rangeStart, factor,
                                      new AsyncCallback<SearchResults>() {

                                          @Override
                                          public void onSuccess(SearchResults success) {
                                              reset();
                                              setData(success, false);
                                          }

                                          @Override
                                          public void onFailure(Throwable caught) {
                                              Window.alert("Failure to retrieve results");
                                              reset();
                                          }

                                          public void reset() {
                                              Utils.showDefaultCursor(null);
                                          }
                                      });
    }

    public void setData(SearchResults searchResults, boolean reset) {

        if (reset)
            reset();

        this.searchResults = searchResults;
        if (searchResults != null) {
            for (SearchResultInfo info : searchResults.getResults()) {
                results.add(info);
            }

            // number of search results available
            resultSize = (int) searchResults.getResultCount();
        }

        updateRowCount(resultSize, true);
        lastSortAsc = false;
        lastSortField = this.defaultSort;

        // retrieve the first page of results and updateRowData
        final Range range = this.dataTable.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd;
        if ((rangeStart + range.getLength()) > resultSize)
            rangeEnd = resultSize;
        else
            rangeEnd = (rangeStart + range.getLength());

//        fetchHasEntryData(this.getSortField(), true, rangeStart, rangeEnd);
        if (results.size() >= rangeEnd) {
            updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
        } else {
            // not so fetch more; fetch more
            retrieveValues(null, rangeStart, rangeEnd, this.getSortField(), true);
        }
    }
}
