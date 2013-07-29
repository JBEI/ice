package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.HasEntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.HasEntryData;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.search.SearchResult;
import org.jbei.ice.lib.shared.dto.search.SearchResults;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class SearchDataProvider extends HasEntryDataViewDataProvider<SearchResult> {

    private SearchResults searchResults;
    private final EntryTablePager pager;
    private final boolean webSearch;
    private final HandlerManager eventBus;

    public SearchDataProvider(SearchResultsTable table, RegistryServiceAsync rpcService, HandlerManager eventBus,
            boolean webSearch) {
        super(table, rpcService, ColumnField.RELEVANCE);
        this.pager = table.getPager();
        this.webSearch = webSearch;
        this.eventBus = eventBus;
    }

    @Override
    public PartData getCachedData(long id, String recordId) {
        for (HasEntryData result : results) {
            PartData info = result.getEntryInfo();
            if (recordId != null && info.getRecordId().equalsIgnoreCase(recordId))
                return info;

            if (info.getId() == id)
                return info;
        }
        return null;
    }

    @Override
    public int indexOfCached(PartData info) {
        for (int i = 0; i < results.size(); i += 1) {
            SearchResult searchResult = results.get(i);
            if (searchResult.getEntryInfo().getId() == info.getId())
                return i;
        }
        return 0;
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
    protected void fetchEntryData(ColumnField field, boolean ascending, int start, int factor, final boolean reset) {
        if (!reset)
            pager.setLoading();

        searchResults.getQuery().getParameters().setSortField(field);
        searchResults.getQuery().getParameters().setSortAscending(ascending);
        searchResults.getQuery().getParameters().setStart(start);
        searchResults.getQuery().getParameters().setRetrieveCount(factor);
        new IceAsyncCallback<SearchResults>() {

            @Override
            protected void callService(AsyncCallback<SearchResults> callback) throws AuthenticationException {
                service.performSearch(ClientController.sessionId, searchResults.getQuery(), webSearch, callback);
            }

            @Override
            public void onSuccess(SearchResults result) {
                searchResults = result;
                Utils.showDefaultCursor(null);
                if (result == null)
                    return;

                if (reset)
                    setSearchData(result);
                else
                    results.addAll(result.getResults());

                pager.determineSetNextEnabled();
                pager.setDefaultHTML();
            }
        }.go(eventBus);
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
        int rangeEnd = range.getLength() > resultSize ? range.getLength() : resultSize;
        updateRowData(rangeStart, results.subList(rangeStart, rangeEnd));
        dataTable.setPageStart(0);
    }
}
