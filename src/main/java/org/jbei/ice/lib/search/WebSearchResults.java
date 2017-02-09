package org.jbei.ice.lib.search;

import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around search results received from a remote ICE instance
 *
 * @author Hector Plahar
 */
public class WebSearchResults implements IDataTransferModel {

    private long totalCount;
    private List<WebResult> results;
    private SearchQuery query;

    public WebSearchResults(int numberOfPartners) {
        this.results = new ArrayList<>(numberOfPartners);
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<WebResult> getResults() {
        return results;
    }

    public SearchQuery getQuery() {
        return query;
    }

    public void setQuery(SearchQuery query) {
        this.query = query;
    }
}
