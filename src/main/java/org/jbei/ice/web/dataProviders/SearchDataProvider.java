package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.lucene.SearchResult;

public class SearchDataProvider extends SortableDataProvider<SearchResult> {
    private static final long serialVersionUID = 2L;

    private ArrayList<SearchResult> searchResults;
    private ArrayList<Entry> entries = new ArrayList<Entry>();

    public SearchDataProvider(ArrayList<SearchResult> searchResults) {
        super();

        this.searchResults = searchResults;
        this.setSort("score", false);
    }

    public Iterator<SearchResult> iterator(int first, int count) {
        entries.clear();
        
        int numSearchResults = searchResults.size();

        if (first > numSearchResults - 1) {
            first = numSearchResults - 1;
        }

        if (first + count > numSearchResults) {
            count = numSearchResults - 1 - first;
        }

        for (int i = first; i < first + count; i++) {
            entries.add(searchResults.get(i).getEntry());
        }

        Comparator<SearchResult> c = new SearchResultComparator();
        Collections.sort(searchResults, c);
        return (Iterator<SearchResult>) searchResults.subList(first, first + count).iterator();
    }

    public IModel<SearchResult> model(SearchResult searchResult) {
        return new Model<SearchResult>(searchResult);
    }

    public int size() {
        return searchResults.size();
    }

    public void detach() {
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    private class SearchResultComparator implements Comparator<SearchResult> {
        @Override
        public int compare(SearchResult o1, SearchResult o2) {

            int result = 0;
            if (getSort() == null)
                return result;

            String property = getSort().getProperty();

            PropertyModel<Comparable<Object>> model1 = new PropertyModel<Comparable<Object>>(o1,
                    property);
            PropertyModel<Comparable<Object>> model2 = new PropertyModel<Comparable<Object>>(o2,
                    property);

            result = model1.getObject().compareTo(model2.getObject());

            if (!getSort().isAscending())
                result *= -1;

            return result;
        }

    }
}
