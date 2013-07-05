package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.entry.HasEntryInfo;
import org.jbei.ice.lib.shared.dto.search.SearchResultInfo;
import org.jbei.ice.lib.shared.dto.search.SearchResults;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class SearchDataProvider extends HasEntryDataViewDataProvider<SearchResultInfo> {

    private SearchResults searchResults;
    private final EntryTablePager pager;
    private final boolean webSearch;

    public SearchDataProvider(SearchResultsTable table, RegistryServiceAsync rpcService, boolean webSearch) {
        super(table, rpcService, ColumnField.RELEVANCE);
        this.pager = table.getPager();
        this.webSearch = webSearch;
    }

    @Override
    public EntryInfo getCachedData(long id, String recordId) {
        for (HasEntryInfo result : results) {
            EntryInfo info = result.getEntryInfo();
            if (recordId != null && info.getRecordId().equalsIgnoreCase(recordId))
                return info;

            if (info.getId() == id)
                return info;
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
            pager.setLoading();

        try {
            searchResults.getQuery().getParameters().setSortField(field);
            searchResults.getQuery().getParameters().setSortAscending(ascending);
            searchResults.getQuery().getParameters().setStart(start);
            searchResults.getQuery().getParameters().setRetrieveCount(factor);
            service.performSearch(ClientController.sessionId, searchResults.getQuery(), webSearch,
                                  new AsyncCallback<SearchResults>() {

                                      @Override
                                      public void onSuccess(SearchResults success) {
                                          searchResults = success;
                                          Utils.showDefaultCursor(null);
                                          if (success == null)
                                              return;

                                          if (reset)
                                              setSearchData(success);
                                          else
                                              results.addAll(success.getResults());

                                          pager.determineSetNextEnabled();
                                          pager.setDefaultHTML();
                                      }

                                      @Override
                                      public void onFailure(Throwable caught) {
                                          Window.alert("Failed to retrieve results");
                                          Utils.showDefaultCursor(null);
                                      }
                                  });
        } catch (AuthenticationException e) {
            GWT.log(e.getMessage(), e);
        }
    }

    public void setSearchData(SearchResults searchResults) {
        reset();
        this.searchResults = searchResults;
        if (searchResults == null || searchResults.getResultCount() == 0) {
            updateRowCount(0, true);
            return;
        }

        results.addAll(searchResults.getResults());
        final Range range = this.dataTable.getVisibleRange();

        resultSize = (int) searchResults.getResultCount();
        if (searchResults.getResults().size() < range.getLength() && searchResults.getResults().size() != resultSize) {
            resultSize = searchResults.getResults().size();
        }
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        final int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
        dataTable.setPageStart(0);
    }
}
