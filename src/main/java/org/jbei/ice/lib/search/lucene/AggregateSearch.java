package org.jbei.ice.lib.search.lucene;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.IceSession;

/**
 * Combine different searches into one interface, with heuristics built in to
 * give most relevant answers.
 * 
 * @author tham
 * 
 */
public class AggregateSearch {
    public static ArrayList<SearchResult> query(String queryString) throws SearchException {
        EntryController entryController = new EntryController(IceSession.get().getAccount());

        ArrayList<SearchResult> result = new ArrayList<SearchResult>();

        try {
            ArrayList<String[]> queries = new ArrayList<String[]>();

            LinkedHashSet<Entry> exactNameMatches = new LinkedHashSet<Entry>();
            ArrayList<SearchResult> exactNameResult = new ArrayList<SearchResult>();
            queries.add(new String[] { "name_or_alias", "=" + queryString });

            ArrayList<Entry> matchedEntries = entryController.getEntriesByQueries(queries, 0, -1);

            if (matchedEntries != null) {
                exactNameMatches.addAll(matchedEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[] { "part_number", "=" + queryString });

            matchedEntries = entryController.getEntriesByQueries(queries, 0, -1);
            if (matchedEntries != null) {
                exactNameMatches.addAll(matchedEntries);
            }

            for (Entry entry : exactNameMatches) {
                exactNameResult.add(new SearchResult(entry, 2.0F));
            }

            LinkedHashSet<Entry> substringMatches = new LinkedHashSet<Entry>();
            ArrayList<SearchResult> substringResults = new ArrayList<SearchResult>();
            queries = new ArrayList<String[]>();
            queries.add(new String[] { "name_or_alias", "~" + queryString });

            ArrayList<Entry> matchedSubstringEntries = entryController.getEntriesByQueries(queries,
                0, -1);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[] { "part_number", "~" + queryString });

            matchedSubstringEntries = entryController.getEntriesByQueries(queries, 0, -1);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            // Remove duplicates 
            // If getEntriesByQueris is non-lazy, this may contain duplicates
            ArrayList<Integer> seenBefore = new ArrayList<Integer>();
            LinkedHashSet<Entry> newSubstringMatches = new LinkedHashSet<Entry>();
            for (Entry entry : substringMatches) {
                if (!seenBefore.contains(entry.getId())) {
                    seenBefore.add(entry.getId());
                    newSubstringMatches.add(entry);
                }
            }
            substringMatches = newSubstringMatches;

            for (Entry entry : substringMatches) {
                substringResults.add(new SearchResult(entry, 1.0F));
            }

            SearchResult.sumSearchResults(substringResults, exactNameResult);

            result = LuceneSearch.getInstance().query(queryString);

            SearchResult.sumSearchResults(result, substringResults);
        } catch (ControllerException e) {
            throw new SearchException(e);
        }

        return result;
    }
}
