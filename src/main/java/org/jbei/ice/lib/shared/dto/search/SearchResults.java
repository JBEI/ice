package org.jbei.ice.lib.shared.dto.search;

import java.util.LinkedList;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Wrapper around a list of search results which also contains information about the search.
 * Information such as query, result count
 *
 * @author Hector Plahar
 */
public class SearchResults implements IDTOModel {

    private static final long serialVersionUID = 1l;

    private long resultCount;
    private LinkedList<SearchResult> results;
    private SearchQuery query;

    public SearchResults() {
    }

    public LinkedList<SearchResult> getResults() {
        return this.results;
    }

    public void setResults(LinkedList<SearchResult> results) {
        this.results = results;
    }

    public void setResultCount(long count) {
        this.resultCount = count;
    }

    /**
     * @return total query result count. not just the count of results returned
     */
    public long getResultCount() {
        return this.resultCount;
    }

    public SearchQuery getQuery() {
        return query;
    }

    public void setQuery(SearchQuery query) {
        this.query = query;
    }
}
