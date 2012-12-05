package org.jbei.ice.shared.dto;

import java.util.ArrayList;
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
    private LinkedList<SearchResultInfo> resultInfos;
    private EntryType[] searchTypes;
    private ArrayList<SearchFilterInfo> searchFilters;

    public SearchResults() {
        this.resultInfos = new LinkedList<SearchResultInfo>();
        searchFilters = new ArrayList<SearchFilterInfo>();
    }

    public LinkedList<SearchResultInfo> getResults() {
        return this.resultInfos;
    }

    public void setResults(LinkedList<SearchResultInfo> results) {
        this.resultInfos.clear();
        if (results == null)
            return;

        this.resultInfos.addAll(results);
    }

    public void setResultCount(long count) {
        this.resultCount = count;
    }

    public void setSearchFilters(ArrayList<SearchFilterInfo> searchFilters) {
        this.searchFilters.clear();
        this.searchFilters.addAll(searchFilters);
    }

    /**
     * @return total query result count. not just the count of results returned
     */
    public long getResultCount() {
        return this.resultCount;
    }

    public ArrayList<SearchFilterInfo> getSearchFilters() {
        return this.searchFilters;
    }

    public EntryType[] getSearchTypes() {
        return searchTypes;
    }

    public void setSearchTypes(EntryType[] types) {
        searchTypes = types;
    }
}
