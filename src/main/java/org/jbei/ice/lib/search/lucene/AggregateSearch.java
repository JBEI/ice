package org.jbei.ice.lib.search.lucene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.lib.query.QueryException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Combine different searches into one interface, with heuristics built in to
 * give most relevant answers.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class AggregateSearch {
    /**
     * Perform full text aggregated query on entries.
     * <p>
     * This weighs positively on {@link Entry}.name or .alias fields, or matches in
     * {@link PartNumber}.partNumber.
     * 
     * @param queryString
     *            string to query
     * @param account
     *            Account of the query
     * @return - ArrayList of {@link SearchResult}s.
     * @throws SearchException
     */
    public static ArrayList<SearchResult> query(String queryString, Account account)
            throws SearchException {
        EntryController entryController = new EntryController(account);

        ArrayList<SearchResult> result = new ArrayList<SearchResult>();

        try {
            ArrayList<String[]> queries = new ArrayList<String[]>();

            LinkedHashSet<Entry> exactNameMatches = new LinkedHashSet<Entry>();
            ArrayList<SearchResult> exactNameResult = new ArrayList<SearchResult>();
            queries.add(new String[] { "name_or_alias", "=" + queryString });
            ArrayList<Long> queryResultIds = Query.getInstance().query(queries);
            queryResultIds = new ArrayList<Long>(
                    entryController.filterEntriesByPermission(queryResultIds));
            Collections.reverse(queryResultIds);
            ArrayList<Entry> matchedEntries = entryController.getEntriesByIdSet(queryResultIds);

            if (matchedEntries != null) {
                exactNameMatches.addAll(matchedEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[] { "part_number", "=" + queryString });
            queryResultIds = Query.getInstance().query(queries);
            Iterator<Long> iter = queryResultIds.iterator();
            while (iter.hasNext()) {
                long id = iter.next();
                if (!entryController.hasReadPermissionById(id)) {
                    iter.remove();
                }
            }

            Collections.reverse(queryResultIds);
            matchedEntries = entryController.getEntriesByIdSet(queryResultIds);
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
            queryResultIds = Query.getInstance().query(queries);
            queryResultIds = new ArrayList<Long>(
                    entryController.filterEntriesByPermission(queryResultIds));
            Collections.reverse(queryResultIds);
            ArrayList<Entry> matchedSubstringEntries = entryController
                    .getEntriesByIdSet(queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[] { "part_number", "~" + queryString });
            queryResultIds = Query.getInstance().query(queries);
            queryResultIds = new ArrayList<Long>(
                    entryController.filterEntriesByPermission(queryResultIds));
            Collections.reverse(queryResultIds);
            matchedSubstringEntries = entryController.getEntriesByIdSet(queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            // Remove duplicates 
            // If getEntriesByQueris is non-lazy, this may contain duplicates
            ArrayList<Long> seenBefore = new ArrayList<Long>();
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
        } catch (QueryException e) {
            throw new SearchException(e);
        }

        return result;
    }
}
