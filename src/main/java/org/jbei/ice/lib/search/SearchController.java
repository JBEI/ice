package org.jbei.ice.lib.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.SearchDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.dto.search.SearchBoostField;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause;

/**
 * Controller for running searches on the ice platform
 *
 * @author Hector Plahar
 */
public class SearchController {

    private final SearchDAO dao;

    public SearchController() {
        dao = DAOFactory.getSearchDAO();
    }

    public SearchResults runSearch(String userId, SearchQuery query, boolean searchWeb) {
        if (searchWeb)
            return runWebSearch(query);
        return runLocalSearch(userId, query, false);
    }

    /**
     * Searches all registries in the web of registries configuration with this
     * registry
     *
     * @param query
     * @return
     */
    public SearchResults runWebSearch(SearchQuery query) {
//        RestClient client = new RestClient();

        SearchResults results = null;
        // TODO

//        while (ports.hasNext()) {
//            QName name = ports.next();
//            if (name.getNamespaceURI() == null) {
//                Logger.warn("Encountered port with null name in service client");
//                continue;
//            }
//
//            String myUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
//            String apiKey;
//            try {
//                apiKey = new WoRController().getApiKey(name.getNamespaceURI());
//                if (apiKey == null)
//                    continue;
//            } catch (ControllerException e) {
//                continue;
//            }
//
//            try {
//                IRegistryAPI api = service.getPort(name, IRegistryAPI.class);
//                if (results == null)
//                    results = api.runSearch(myUrl, apiKey, query);
//                else {
//                    SearchResults tmpResults = api.runSearch(myUrl, apiKey, query);
//                    if (tmpResults.getResultCount() == 0)
//                        continue;
//
//                    if (results.getResults() == null) {
//                        results.setResults(tmpResults.getResults());
//                    } else {
//                        results.getResults().addAll(tmpResults.getResults());
//                    }
//                    results.setResultCount(results.getResultCount() + tmpResults.getResultCount());
//                }
//            } catch (Exception e) {
//                Logger.error(e);
//            }
//        }

        return results;
    }

    /**
     * Executes search using parameters specified in the query.
     *
     * @param userId
     * @param query
     * @return wrapper around the list of search results
     */
    public SearchResults runLocalSearch(String userId, SearchQuery query, boolean isAPISearch) {
        String projectName = "";
        String projectURI = "";
        if (isAPISearch) {
            ConfigurationController configurationController = new ConfigurationController();
            projectName = configurationController.getPropertyValue(ConfigurationKey.PROJECT_NAME);
            projectURI = configurationController.getPropertyValue(ConfigurationKey.URI_PREFIX);
        }

        String queryString = query.getQueryString();
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

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
            return hibernateSearch.executeSearch(account, terms, query, projectName, projectURI, mapping);
        } else {
            return hibernateSearch.executeSearchNoTerms(account, query, projectName, projectURI);
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

    public void initHibernateSearch() throws ControllerException {
        try {
            dao.initHibernateSearch();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
