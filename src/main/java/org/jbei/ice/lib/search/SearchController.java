package org.jbei.ice.lib.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.dto.search.SearchBoostField;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.net.RemotePartner;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.services.rest.RestClient;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause;

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
        RestClient client = RestClient.getInstance();
        ArrayList<RemotePartner> partners = DAOFactory.getRemotePartnerDAO().retrieveRegistryPartners();

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
                SearchResults results = (SearchResults) client.post(partner.getUrl(), "/rest/search", query,
                                                                    SearchResults.class);
                if (results == null)
                    continue;

                RegistryPartner registryPartner = partner.toDataTransferObject();
                for (SearchResult result : results.getResults()) {
                    result.setPartner(registryPartner);
                    resultsList.add(result);
                }

                total += results.getResults().size();
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
        String queryString;
        if (query.getQueryString() != null)
            queryString = query.getQueryString().toLowerCase();
        else
            queryString = query.getQueryString();
        Account account = null;
        if (userId != null)
            account = DAOFactory.getAccountDAO().getByEmail(userId);

        // blast query only
        if (query.hasBlastQuery() && (queryString == null || queryString.isEmpty())) {
            if (query.getBlastQuery().getBlastProgram() == null)
                query.getBlastQuery().setBlastProgram(BlastProgram.BLAST_N);

            try {
                HashMap<String, SearchResult> results = BlastPlus.runBlast(account, query.getBlastQuery());
                if (results.isEmpty())
                    return new SearchResults();
                return HibernateSearch.getInstance().runSearchFilter(account, results, query);
            } catch (BlastException e) {
                return null;
            }
        }

        // text query (may also include blast)
        // no filter type indicates a term or phrase query
        HibernateSearch hibernateSearch = HibernateSearch.getInstance();
        List<SearchBoostField> boostFields = Arrays.asList(SearchBoostField.values());
        HashMap<String, String> results = new PreferencesController().retrieveUserPreferenceList(account, boostFields);
        HashMap<String, Float> mapping = new HashMap<>();
        for (Map.Entry<String, String> entry : results.entrySet()) {
            try {
                String field = SearchBoostField.valueOf(entry.getKey()).getField();
                mapping.put(field, Float.valueOf(entry.getValue()));
            } catch (IllegalArgumentException nfe) {
                Logger.warn(nfe.getMessage());
            }
        }

        if (!StringUtils.isEmpty(queryString)) {
            HashMap<String, BooleanClause.Occur> terms = parseQueryString(queryString);
            return hibernateSearch.executeSearch(account, terms, query, mapping);
        } else {
            return hibernateSearch.executeSearchNoTerms(account, query);
        }
    }

    public boolean rebuildIndexes(Account account, IndexType type) {
        if (account.getType() != AccountType.ADMIN) {
            Logger.warn(account.getEmail() + " attempting to rebuild search index " + type + " without admin privs");
            return false;
        }

        Logger.info(account.getEmail() + ": rebuilding search index " + type);
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

    HashMap<String, BooleanClause.Occur> parseQueryString(String queryString) {
        HashMap<String, BooleanClause.Occur> terms = new HashMap<>();

        if (queryString == null || queryString.trim().length() == 0)
            return terms;

        StringBuilder builder = new StringBuilder();
        boolean startedPhrase = false;
        for (int i = 0; i < queryString.trim().length(); i += 1) {
            char c = queryString.charAt(i);
            if (c == '\"' || c == '\'') {
                if (startedPhrase) {
                    terms.put(builder.toString(), BooleanClause.Occur.MUST);
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
                    terms.put(builder.toString(), BooleanClause.Occur.SHOULD);
                    builder = new StringBuilder();
                    continue;
                }
            }

            builder.append(c);
        }
        if (builder.length() > 0) {
            if (startedPhrase)
                terms.put(builder.toString(), BooleanClause.Occur.MUST);
            else
                terms.put(builder.toString(), BooleanClause.Occur.SHOULD);
        }

        return terms;
    }
}
