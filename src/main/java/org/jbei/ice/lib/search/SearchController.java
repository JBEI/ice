package org.jbei.ice.lib.search;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.search.*;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.storage.hibernate.search.HibernateSearch;

import java.util.HashMap;

/**
 * Controller for running searches on the ice platform
 *
 * @author Hector Plahar
 */
public class SearchController {

    private AccountController accountController = new AccountController();

    /**
     * Executes search using parameters specified in the query.
     *
     * @param userId unique user identifier making the request. This can be null if the request is via web of
     *               registries
     * @param query  wrapper around search query
     * @return wrapper around the list of search results
     */
    public SearchResults runSearch(String userId, SearchQuery query) {
        String queryString = query.getQueryString();
        HashMap<String, SearchResult> blastResults = null;

        // check if there is a blast result and run first
        if (query.hasBlastQuery()) {
            if (query.getBlastQuery().getBlastProgram() == null)
                query.getBlastQuery().setBlastProgram(BlastProgram.BLAST_N);

            try {
                blastResults = BlastPlus.runBlast(query.getBlastQuery());
            } catch (BlastException e) {
                Logger.error("Exception running blast " + e.getMessage());
            }
        }

        // if no other search query or filter and there are blast results (not null) then return the blast results
        if (StringUtils.isEmpty(queryString) && blastResults != null && !query.hasFilter()) {
            if (blastResults.isEmpty())
                return new SearchResults();

            int start = query.getParameters().getStart();
            int count = query.getParameters().getRetrieveCount();
            return HibernateSearch.getInstance().filterBlastResults(userId, start, count, query, blastResults);
        }

        // text query (may also include blast)
        // no filter type indicates a term or phrase query
        HibernateSearch hibernateSearch = HibernateSearch.getInstance();

        if (!StringUtils.isEmpty(queryString)) {
            HashMap<String, QueryType> terms = parseQueryString(queryString);
            return hibernateSearch.executeSearch(userId, terms, query, blastResults);
        } else {
            return hibernateSearch.executeSearchNoTerms(userId, blastResults, query);
        }
    }

    /**
     * Rebuilds the search indices. Admin privileges required
     *
     * @param userId unique identifier for user making request
     * @param type   type of search index to rebuild
     * @throws PermissionException      if requesting user does not have administrative privileges
     * @throws IllegalArgumentException on unsupported index type
     */
    public void rebuildIndexes(String userId, IndexType type) {
        if (!accountController.isAdministrator(userId)) {
            Logger.warn(userId + " attempting to rebuild search index " + type + " without admin privs");
            throw new PermissionException("Administrative privileges required to perform this action");
        }

        Logger.info(userId + ": rebuilding search index " + type);
        switch (type) {
            case LUCENE:
                IceExecutorService.getInstance().runTask(new RebuildLuceneIndexTask());
                break;

            case BLAST:
                try {
                    BlastPlus.rebuildDatabase(true);
                } catch (BlastException e) {
                    Logger.error(e);
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid type");
        }
    }

    /**
     * Parses the query string checking for terms and phrases. A quote is used to indicate
     * the boundaries of a phrase
     * <p>e.g. <code>"quick brown" fox "jumped"</code> is parsed into two phrases and one term
     *
     * @param queryString query string to be parsed for phrases and terms
     * @return a mapping of the phrases and terms to clauses that indicate how the matches should appear
     * in the document. Phrases must appear in the result document
     */
    HashMap<String, QueryType> parseQueryString(String queryString) {
        HashMap<String, QueryType> terms = new HashMap<>();

        if (queryString == null || queryString.trim().length() == 0)
            return terms;

        StringBuilder builder = new StringBuilder();
        boolean startedPhrase = false;
        for (int i = 0; i < queryString.trim().length(); i += 1) {
            char c = queryString.charAt(i);
            if (c == '\"' || c == '\'') {
                if (startedPhrase) {
                    terms.put(builder.toString(), QueryType.PHRASE);
                    builder = new StringBuilder();
                    startedPhrase = false;
                } else {
                    startedPhrase = true;
                }
                continue;
            }

            // check for space
            if (c == ' ') {
                if (builder.length() == 0)
                    continue;

                if (!startedPhrase) {
                    terms.put(builder.toString(), QueryType.TERM);
                    builder = new StringBuilder();
                    continue;
                }
            }

            builder.append(c);
        }
        if (builder.length() > 0) {
            if (startedPhrase)
                terms.put(builder.toString(), QueryType.PHRASE);
            else
                terms.put(builder.toString(), QueryType.TERM);
        }

        return terms;
    }
}
