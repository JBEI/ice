package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SearchResultInfo;
import org.jbei.ice.shared.dto.SearchResults;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class AdvancedSearchDataProvider extends HasEntryDataViewDataProvider<SearchResultInfo> {

    private SearchResults searchResults;
    private final EntryTablePager pager;

    public AdvancedSearchDataProvider(AdvancedSearchResultsTable table, RegistryServiceAsync rpcService) {
        super(table, rpcService, ColumnField.RELEVANCE);
        pager = table.getPager();
    }

    @Override
    public EntryInfo getCachedData(long id) {
        for (SearchResultInfo info : results) {
            if (info.getEntryInfo().getId() == id)
                return info.getEntryInfo();
        }
        return null;
    }

    @Override
    public int indexOfCached(EntryInfo info) {
        for (int i = 0; i < results.size(); i += 1) {
            SearchResultInfo searchResultInfo = results.get(i);
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

    @Override
    protected void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, final boolean reset) {
        if (!reset)
            pager.setNextEnabled(false);
        service.retrieveSearchResults(AppController.sessionId, searchResults.getSearchFilters(),
                                      searchResults.getSearchTypes(), field, ascending, start, factor,
                                      new AsyncCallback<SearchResults>() {

                                          @Override
                                          public void onSuccess(SearchResults success) {
                                              searchResults = success;
                                              Utils.showDefaultCursor(null);
                                              if (success == null)
                                                  return;

                                              if (reset)
                                                  setSearchData(success);
                                              else {
                                                  results.addAll(success.getResults());
                                                  pager.setNextEnabled(true);
                                              }
                                          }

                                          @Override
                                          public void onFailure(Throwable caught) {
                                              Window.alert("Failed to retrieve results");
                                              Utils.showDefaultCursor(null);
                                          }
                                      });
    }

    public void setSearchData(SearchResults searchResults) {
        reset();
        this.searchResults = searchResults;
        if (searchResults == null) {
            updateRowCount(0, true);
            return;
        }

        results.addAll(searchResults.getResults());
        resultSize = (int) searchResults.getResultCount();
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        final Range range = this.dataTable.getVisibleRange();
        final int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
        dataTable.setPageStart(0);
    }
}
