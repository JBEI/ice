package org.jbei.ice.lib.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.search.IndexType;
import org.jbei.ice.lib.shared.dto.search.SearchBoostField;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResult;
import org.jbei.ice.lib.shared.dto.search.SearchResults;
import org.jbei.ice.lib.shared.dto.user.AccountType;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;

import com.google.common.base.Splitter;

/**
 * Controller for running searches on the ice platform
 *
 * @author Hector Plahar
 */
public class SearchController {

    private final SearchDAO dao;

    public SearchController() {
        dao = new SearchDAO();
    }

    public SearchResults runSearch(Account account, SearchQuery query, boolean searchWeb) throws ControllerException {
        if (searchWeb)
            return runWebSearch(query);
        return runLocalSearch(account, query, false);
    }

    public SearchResults runWebSearch(SearchQuery query) {
        Service service = RegistryAPIServiceClient.getService();
        Iterator<QName> ports = service.getPorts();

        SearchResults results = null;

        while (ports.hasNext()) {
            QName name = ports.next();
            if (name.getNamespaceURI() == null) {
                Logger.warn("Encountered port with null name in service client");
                continue;
            }

            IRegistryAPI api = service.getPort(name, IRegistryAPI.class);
            Logger.info("Retrieved API proxy for " + name.getNamespaceURI());

            try {
                if (results == null)
                    results = api.runSearch(query);
                else {
                    SearchResults tmpResults = api.runSearch(query);
                    results.getResults().addAll(tmpResults.getResults());
                    results.setResultCount(results.getResultCount() + tmpResults.getResultCount());
                }
            } catch (Exception e) {
                Logger.error(e.getMessage());
            }
        }

        return results;
    }

    /**
     * Executes search using parameters specified in the query.
     *
     * @param account
     * @param query
     * @return
     * @throws ControllerException
     */
    public SearchResults runLocalSearch(Account account, SearchQuery query, boolean isAPISearch)
            throws ControllerException {
        String projectName = "";
        String projectURI = "";
        if (isAPISearch) {
            ConfigurationController configurationController = ControllerFactory.getConfigurationController();
            projectName = configurationController.getPropertyValue(ConfigurationKey.PROJECT_NAME);
            projectURI = configurationController.getPropertyValue(ConfigurationKey.URI_PREFIX);
        }

        String queryString = query.getQueryString();
        // TODO : split on \" first for phrase query

        // blast query only
        if (query.hasBlastQuery() && (queryString == null || queryString.isEmpty())) {
            try {
                HashMap<String, SearchResult> results = BlastPlus.runBlast(account, query.getBlastQuery());
                if (results.isEmpty())
                    return new SearchResults();
                return HibernateSearch.getInstance().runSearchFilter(account, results);
            } catch (BlastException e) {
                throw new ControllerException(e);
            }
        }

        // text query (may also include blast)
        // no filter type indicates a term or phrase query
        Iterator<String> iterable;
        HibernateSearch hibernateSearch = HibernateSearch.getInstance();

        if (queryString != null && !queryString.isEmpty()) {
            iterable = Splitter.on(" ").omitEmptyStrings().split(queryString).iterator();
            HashMap<String, String> results = ControllerFactory.getPreferencesController().
                    retrieveUserPreferenceList(account, Arrays.asList(SearchBoostField.values()));
            HashMap<String, Float> mapping = new HashMap<>();
            for (Map.Entry<String, String> entry : results.entrySet()) {
                try {
                    String field = SearchBoostField.valueOf(entry.getKey()).getField();
                    mapping.put(field, Float.valueOf(entry.getValue()));
                } catch (NumberFormatException nfe) {
                    Logger.error(nfe);
                }
            }
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
