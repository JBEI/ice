package org.jbei.ice.lib.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.search.BlastQuery;
import org.jbei.ice.shared.dto.search.SearchQuery;
import org.jbei.ice.shared.dto.search.SearchResultInfo;
import org.jbei.ice.shared.dto.search.SearchResults;

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
        return runLocalSearch(account, query);
    }

    public SearchResults runWebSearch(SearchQuery query) {
        Service service = RegistryAPIServiceClient.getInstance().getService();
        Iterator<QName> ports = service.getPorts();

        SearchResults results = null;

        while (ports.hasNext()) {
            QName name = ports.next();
            if (name.getNamespaceURI() == null) {
                Logger.warn("Encountered port with null name in service client");
                continue;
            }
            IRegistryAPI hw = service.getPort(name, IRegistryAPI.class);
            Logger.info("Retrieved port for " + name.getNamespaceURI());

            try {
                if (results == null)
                    results = hw.runSearch(query);
                else {
                    SearchResults tmpResults = hw.runSearch(query);
                    results.getResults().addAll(tmpResults.getResults());
                    results.setResultCount(results.getResultCount() + tmpResults.getResultCount());
                }
            } catch (Exception e) {
                Logger.error(e.getMessage());
                continue;
            }
        }

        return results;
    }

    public SearchResults runLocalSearch(Account account, SearchQuery query) throws ControllerException {
        // TODO run this only if we are searching from the api.
        ConfigurationController configurationController = ApplicationController.getConfigurationController();
        String projectName = configurationController.getPropertyValue(ConfigurationKey.PROJECT_NAME);
        String projectURI = configurationController.getPropertyValue(ConfigurationKey.URI_PREFIX);

        String queryString = query.getQueryString();
        // TODO : split on \" first for phrase query
        SearchResults searchResults;

        // blast query only
        if (query.hasBlastQuery() && (queryString == null || queryString.isEmpty())) {
            try {
                searchResults = new SearchResults();
                LinkedList<SearchResultInfo> infoList = runBlast(account, query.getBlastQuery(), query.getParameters());
                searchResults.setResults(infoList);
                return searchResults;
            } catch (ProgramTookTooLongException e) {
                throw new ControllerException(e);
            }
        }

        // text query (may also include blast)
        // no filter type indicates a term or phrase query
        Iterator<String> iterable;
        if (queryString != null && !queryString.isEmpty()) {
            iterable = Splitter.on(" ").omitEmptyStrings().split(queryString).iterator();
            searchResults = HibernateSearch.getInstance().executeSearch(account, iterable, query, projectName,
                                                                        projectURI);
            return searchResults;
        }

        // advanced search filters only (e.g. has attachment etc)
        return HibernateSearch.getInstance().executeSearchNoTerms(account, query, projectName, projectURI);
    }

    protected LinkedList<SearchResultInfo> runBlast(Account account, BlastQuery blastQuery,
            final SearchQuery.Parameters parameters) throws ProgramTookTooLongException, ControllerException {
        String query = blastQuery.getSequence();
        if (query == null || query.isEmpty())
            return new LinkedList<>();

        Blast blast = new Blast();

        try {
            final ColumnField sort;
            if (parameters.getSortField() == null || parameters.getSortField() == ColumnField.RELEVANCE)
                sort = ColumnField.ALIGNED_BP;
            else
                sort = parameters.getSortField();

            HashMap<String, SearchResultInfo> map = blast.query(account, blastQuery.getSequence(),
                                                                blastQuery.getBlastProgram());
            LinkedList<SearchResultInfo> results = new LinkedList<>(map.values());
            Collections.sort(results, new Comparator<SearchResultInfo>() {
                @Override
                public int compare(SearchResultInfo o1, SearchResultInfo o2) {
                    switch (sort) {
                        case RELEVANCE:
                        default:
                            Float tmp = o1.getRelativeScore() - o2.getRelativeScore();
                            return (tmp.intValue());

                        case ALIGNED_BP:
                            return (o1.getAlignmentLength() - o2.getAlignmentLength());

                        case ALIGNED_IDENTITY:
                            tmp = (o1.getPercentId() - o2.getPercentId());
                            return tmp.intValue();

                        case BIT_SCORE:
                            tmp = o1.getBitScore() - o1.getBitScore();
                            return tmp.intValue();
                    }
                }
            });

            Logger.info("Found " + results.size());
            return results;
        } catch (BlastException be) {
            Logger.error(be);
            throw new ControllerException(be);
        }
    }

    public void initHibernateSearch() throws ControllerException {
        try {
            dao.initHibernateSearch();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
