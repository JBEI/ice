package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.lucene.SearchResult;

public class SearchDataProvider implements IDataProvider<SearchResult> {
    private static final long serialVersionUID = 1L;

    private ArrayList<SearchResult> searchResults;
    private ArrayList<Entry> entries;

    public SearchDataProvider(ArrayList<SearchResult> searchResults) {
        super();

        this.searchResults = searchResults;
    }

    public Iterator<SearchResult> iterator(int first, int count) {
        int numSearchResults = searchResults.size();

        if (first > numSearchResults - 1) {
            first = numSearchResults - 1;
        }

        if (first + count > numSearchResults) {
            count = numSearchResults - 1 - first;
        }

        entries = new ArrayList<Entry>();

        for (int i = first; i < first + count; i++) {
            entries.add(searchResults.get(i).getEntry());
        }

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
}
