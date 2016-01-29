package org.jbei.ice.lib.search;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.search.*;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.search.HibernateSearch;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.*;

/**
 * Controller for running searches on the ice platform
 *
 * @author Hector Plahar
 */
public class SearchController {

    public SearchResults runSearch(String userId, SearchQuery query, boolean searchWeb) {
        if (searchWeb)
            return runWebSearch(query);
        return runLocalSearch(userId, query);
    }

    /**
     * Searches all registries in the web of registries configuration with this
     * registry. Without some sort of indexing locally or in some central location,
     * this will be slow for large numbers of results
     *
     * @param query wrapper around search query
     * @return list of search results
     */
    public SearchResults runWebSearch(SearchQuery query) {
        IceRestClient client = IceRestClient.getInstance();
        List<RemotePartner> partners = DAOFactory.getRemotePartnerDAO().getRegistryPartners();

        if (partners == null)
            return null;

        int offset = query.getParameters().getStart();
        int limit = query.getParameters().getRetrieveCount();

        // limit to 50
        query.getParameters().setRetrieveCount(50);
        query.getParameters().setStart(0);

        LinkedList<SearchResult> resultsList = new LinkedList<>();
        long total = 0;

        for (RemotePartner partner : partners) {
            if (partner.getUrl() == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;

            try {
                SearchResults results = client.post(partner.getUrl(), "/rest/search", query, SearchResults.class, null);
                if (results == null)
                    continue;

                RegistryPartner registryPartner = partner.toDataTransferObject();
                for (SearchResult result : results.getResults()) {
                    result.setPartner(registryPartner);
                    resultsList.add(result);
                }

                // up to 50 returned from each partner, but total size may be greater
                total += results.getResultCount();
            } catch (Exception e) {
                Logger.warn("Exception contacting partner " + partner.getUrl() + " : " + e.getMessage());
            }
        }

        // sort the results
        Collections.sort(resultsList, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult o1, SearchResult o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });

        int toIndex = offset + limit;
        if (toIndex > resultsList.size())
            toIndex = resultsList.size();

        SearchResults searchResults = new SearchResults();
        searchResults.getResults().addAll(resultsList.subList(offset, toIndex));
        searchResults.setResultCount(total);
        return searchResults;
    }

    /**
     * Executes search using parameters specified in the query.
     *
     * @param userId unique user identifier making the request. This can be null if the request is via web of
     *               registries
     * @param query  wrapper around search query
     * @return wrapper around the list of search results
     */
    public SearchResults runLocalSearch(String userId, SearchQuery query) {
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
            return HibernateSearch.getInstance().filterBlastResults(userId, start, count, blastResults);
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
     * @return true is index rebuild is started successfully, false otherwise
     */
    public boolean rebuildIndexes(String userId, IndexType type) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId)) {
            Logger.warn(userId + " attempting to rebuild search index " + type + " without admin privs");
            return false;
        }

        Logger.info(userId + ": rebuilding search index " + type);
        if (type == IndexType.LUCENE)
            IceExecutorService.getInstance().runTask(new RebuildLuceneIndexTask());
        else if (type == IndexType.BLAST) {
            try {
                BlastPlus.rebuildDatabase(true);
            } catch (BlastException e) {
                Logger.error(e);
                return false;
            }
        } else
            return false;
        return true;
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
