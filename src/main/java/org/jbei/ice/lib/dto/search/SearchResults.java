package org.jbei.ice.lib.dto.search;

import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper around a list of search results which also contains information about the search.
 * Information such as query, result count
 *
 * @author Hector Plahar
 */
public class SearchResults implements IDataTransferModel {

    private static final long serialVersionUID = 1l;

    private long resultCount;
    private LinkedList<SearchResult> results;
    private SearchQuery query;

    public SearchResults() {
        results = new LinkedList<>();
    }

    public LinkedList<SearchResult> getResults() {
        return this.results;
    }

    public void setResults(List<SearchResult> results) {
        if (this.results == null)
            this.results = new LinkedList<>();

        this.results.clear();
        this.results.addAll(results);
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

    public static void sort(ColumnField sortField, LinkedList<SearchResult> results) {
        Comparator<SearchResult> comparator;
        switch (sortField) {
            default:
            case ALIGNMENT:
                comparator = new AlignmentComparator();
        }

        Collections.sort(results, comparator);
    }

    /**
     * Comparator for sorting by the blast result alignment
     */
    private static class AlignmentComparator implements Comparator<SearchResult> {
        @Override
        public int compare(SearchResult o1, SearchResult o2) {
            // expected input format is "x/y (z%)
            String[] o1AlignmentSplit = o1.getAlignment().split("/");
            String[] o2AlignmentSplit = o2.getAlignment().split("/");

            Integer o21 = Integer.valueOf(o2AlignmentSplit[0]);
            Integer o11 = Integer.valueOf(o1AlignmentSplit[0]);
            if (o21.intValue() != o11.intValue())
                return o21.compareTo(o11);

            // expect y (z%)
            String[] o1RemainderSplit = o1AlignmentSplit[1].split("\\(");
            String[] o2RemainderSplit = o2AlignmentSplit[1].split("\\(");

            // first value is equal check second value
            Integer o22 = Integer.valueOf(o2RemainderSplit[0].trim());
            Integer o12 = Integer.valueOf(o1RemainderSplit[0].trim());
            if (o22.intValue() != o12.intValue()) {
                return Double.compare((double) (o21 / o22), (double) (o11 / o12));
            }

            // return third value (%)
            return Integer.valueOf(o2RemainderSplit[1].substring(0, o2RemainderSplit[1].length() - 2)).compareTo(
                    Integer.valueOf(o1RemainderSplit[1].substring(0, o1RemainderSplit[1].length() - 2)));
        }
    }
}
