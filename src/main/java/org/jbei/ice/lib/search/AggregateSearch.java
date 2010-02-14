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

        Query query = Query.getInstance();
        ArrayList<String[]> queries = new ArrayList<String[]>();

        LinkedHashSet<Entry> exactNameMatches = new LinkedHashSet<Entry>();
        ArrayList<SearchResult> exactNameResult = new ArrayList<SearchResult>();
        queries.add(new String[] { "name_or_alias", "=" + queryString });
        exactNameMatches.addAll(query.query(queries));
        queries = new ArrayList<String[]>();
        queries.add(new String[] { "part_number", "=" + queryString });
        exactNameMatches.addAll(query.query(queries));
        for (Entry entry : exactNameMatches) {
            exactNameResult.add(new SearchResult(entry.getRecordId(), 2.0F));
        }

        LinkedHashSet<Entry> substringMatches = new LinkedHashSet<Entry>();
        ArrayList<SearchResult> substringResults = new ArrayList<SearchResult>();
        queries = new ArrayList<String[]>();
        queries.add(new String[] { "name_or_alias", "~" + queryString });
        substringMatches.addAll(query.query(queries));
        queries = new ArrayList<String[]>();
        queries.add(new String[] { "part_number", "~" + queryString });
        substringMatches.addAll(query.query(queries));
        for (Entry entry : substringMatches) {
            substringResults.add(new SearchResult(entry.getRecordId(), 1.0F));
        }

        SearchResult.sumSearchResults(substringResults, exactNameResult);

        LuceneSearch ls = LuceneSearch.getInstance();
        result = ls.query(queryString);

        SearchResult.sumSearchResults(result, substringResults);
        return result;
    }

}
