package org.jbei.ice.lib.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import org.jbei.ice.lib.dto.search.IndexType;
import org.jbei.ice.lib.dto.search.SearchBoostField;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.services.rest.RestClient;

import com.google.common.base.Splitter;

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

    public SearchResults runSearch(String userId, SearchQuery query, boolean searchWeb) throws ControllerException {
        if (searchWeb)
            return runWebSearch(query);
        return runLocalSearch(userId, query, false);
    }

    public SearchResults runWebSearch(SearchQuery query) {
        RestClient client = new RestClient();

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
     * @return
     * @throws ControllerException
     */
    public SearchResults runLocalSearch(String userId, SearchQuery query, boolean isAPISearch)
            throws ControllerException {
        String projectName = "";
        String projectURI = "";
        if (isAPISearch) {
            ConfigurationController configurationController = new ConfigurationController();
            projectName = configurationController.getPropertyValue(ConfigurationKey.PROJECT_NAME);
            projectURI = configurationController.getPropertyValue(ConfigurationKey.URI_PREFIX);
        }

        String queryString = query.getQueryString();
        // TODO : split on \" first for phrase query  e.g. this "little piggy"

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        // blast query only
        if (query.hasBlastQuery() && (queryString == null || queryString.isEmpty())) {
            try {
                HashMap<String, SearchResult> results = BlastPlus.runBlast(account, query.getBlastQuery());
                if (results.isEmpty())
                    return new SearchResults();
                return HibernateSearch.getInstance().runSearchFilter(account, results, query.getParameters());
            } catch (BlastException e) {
                throw new ControllerException(e);
            }
        }

        // text query (may also include blast)
        // no filter type indicates a term or phrase query
        Iterator<String> iterable;
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

        if (queryString != null && !queryString.isEmpty()) {
            iterable = Splitter.on(" ").omitEmptyStrings().split(queryString).iterator();
            return hibernateSearch.executeSearch(account, iterable, query, projectName, projectURI, mapping);
        } else {
            return hibernateSearch.executeSearchNoTerms(account, query, projectName, projectURI);
        }
    }

    public boolean rebuildIndexes(Account account, IndexType type) {
        Logger.info(account.getEmail() + ": rebuilding search indexes");
        if (account.getType() != AccountType.ADMIN) {
            Logger.warn(account.getEmail() + " does not have privileges to complete action");
            return false;
        }

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

    public void initHibernateSearch() throws ControllerException {
        try {
            dao.initHibernateSearch();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
