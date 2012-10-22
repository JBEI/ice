package org.jbei.ice.lib.search.lucene;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.search.Query;
import org.jbei.ice.lib.search.QueryException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Combine different searches into one interface, with heuristics built in to
 * give most relevant answers.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
public class AggregateSearch {
    /**
     * Perform full text aggregated query on entries.
     * <p/>
     * This weighs positively on {@link Entry}.name or .alias fields, or matches in
     * {@link PartNumber}.partNumber.
     *
     * @param queryString string to query
     * @param account     Account of the query
     * @return - ArrayList of {@link SearchResult}s.
     * @throws SearchException
     */
    public static ArrayList<SearchResult> query(String queryString, Account account)
            throws SearchException {
        EntryController entryController = new EntryController();

        try {
            ArrayList<String[]> queries = new ArrayList<String[]>();

            // name or alias exact match
            LinkedHashSet<Entry> exactNameMatches = new LinkedHashSet<Entry>();
            ArrayList<SearchResult> exactNameResult = new ArrayList<SearchResult>();
            queries.add(new String[]{"name_or_alias", "=" + queryString});
            ArrayList<Long> queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            ArrayList<Entry> matchedEntries = entryController.getEntriesByIdSet(account, queryResultIds);

            if (matchedEntries != null) {
                exactNameMatches.addAll(matchedEntries);
            }

            // part number exact match
            queries = new ArrayList<String[]>();
            queries.add(new String[]{"part_number", "=" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            matchedEntries = entryController.getEntriesByIdSet(account, queryResultIds);
            if (matchedEntries != null) {
                exactNameMatches.addAll(matchedEntries);
            }

            for (Entry entry : exactNameMatches) {
                exactNameResult.add(new SearchResult(entry, 2.0F));
            }

            // name or alias substring match
            LinkedHashSet<Entry> substringMatches = new LinkedHashSet<Entry>();
            ArrayList<SearchResult> substringResults = new ArrayList<SearchResult>();
            queries = new ArrayList<String[]>();
            queries.add(new String[]{"name_or_alias", "~" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            ArrayList<Entry> matchedSubstringEntries = entryController.getEntriesByIdSet(account,
                                                                                         queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[]{"part_number", "~" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            // search by creator or owner
            queries = new ArrayList<String[]>();
            queries.add(new String[]{"owner", "~" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[]{"creator", "~" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            // add keywords
            queries = new ArrayList<String[]>();
            queries.add(new String[]{"keywords", "~" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

            queries = new ArrayList<String[]>();
            queries.add(new String[]{"description", "~" + queryString});
            queryResultIds = Query.getInstance().query(queries);
            Collections.reverse(queryResultIds);
            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
            if (matchedSubstringEntries != null) {
                substringMatches.addAll(matchedSubstringEntries);
            }

//            queries = new ArrayList<String[]>();
//            queries.add(new String[]{"long_description", "~" + queryString});
//            queryResultIds = Query.getInstance().query(queries);
//            Collections.reverse(queryResultIds);
//            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
//            if (matchedSubstringEntries != null) {
//                substringMatches.addAll(matchedSubstringEntries);
//            }
//
//            queries = new ArrayList<String[]>();
//            queries.add(new String[]{"literature_references", "~" + queryString});
//            queryResultIds = Query.getInstance().query(queries);
//            Collections.reverse(queryResultIds);
//            matchedSubstringEntries = entryController.getEntriesByIdSet(account, queryResultIds);
//            if (matchedSubstringEntries != null) {
//                substringMatches.addAll(matchedSubstringEntries);
//            }

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

            return SearchResult.sumSearchResults(substringResults, exactNameResult);

//            result = LuceneSearch.getInstance().query(queryString);

//            SearchResult.sumSearchResults(result, substringResults);
        } catch (ControllerException e) {
            throw new SearchException(e);
        } catch (QueryException e) {
            throw new SearchException(e);
        }

//        return result;
    }
}
