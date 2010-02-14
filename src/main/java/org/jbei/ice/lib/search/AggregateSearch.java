package org.jbei.ice.lib.search;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Query;

/**
 * Combine different searches into one interface, with heuristics built in to
 * give most relevant answers.
 * 
 * @author tham
 * 
 */
public class AggregateSearch {
    public static ArrayList<SearchResult> query(String queryString) {
        ArrayList<SearchResult> result = new ArrayList<SearchResult>();

        ArrayList<SearchResult> queryResult = new ArrayList<SearchResult>();

        Query query = Query.getInstance();
        ArrayList<String[]> queries = new ArrayList<String[]>();
        queries.add(new String[] { "name_or_alias", "~" + queryString });
        LinkedHashSet<Entry> temp = query.query(queries);
        queries.clear();
        queries.add(new String[] { "part_number", "~" + queryString });
        temp.addAll(query.query(queries));
        for (Entry entry : temp) {
            queryResult.add(new SearchResult(entry.getRecordId(), 1.0F));
        }

        LuceneSearch ls = LuceneSearch.getInstance();
        result = ls.query(queryString);

        SearchResult.sumSearchResults(result, queryResult);
        return result;
    }

}
