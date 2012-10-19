package org.jbei.ice.shared.dto;

import java.util.LinkedList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Wrapper around a list of search results which also contains information about the search.
 * Information such as query, result count
 *
 * @author Hector Plahar
 */
public class SearchResults implements IsSerializable {
    private long resultCount;
    private final LinkedList<SearchResultInfo> resultInfos;

    public SearchResults() {
        this.resultInfos = new LinkedList<SearchResultInfo>();
    }

    public LinkedList<SearchResultInfo> getResults() {
        return this.resultInfos;
    }

    public void setResults(LinkedList<SearchResultInfo> results) {

        if (results == null)
            return;

        this.resultInfos.clear();
        this.resultInfos.addAll(results);
    }

    public void setResultCount(long count) {
        this.resultCount = count;
    }

    public long getResultCount() {
        return this.resultCount;
    }
}
