package org.jbei.ice.lib.search;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.executor.TaskStatus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Search for other ICE instances
 *
 * @author Hector Plahar
 */
public class WebSearch {

    private final RemotePartnerDAO remotePartnerDAO;

    public WebSearch() {
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
    }

    /**
     * Searches all registries in the web of registries configuration with this
     * registry. Without some sort of indexing locally or in some central location,
     * this will be slow for large numbers of results
     *
     * @param query               wrapper around search query
     * @param includeThisInstance whether to include results from this instance of ICE
     * @return list of search results
     */
    public WebSearchResults run(SearchQuery query, boolean includeThisInstance) {
        List<RemotePartner> partners = this.remotePartnerDAO.getRegistryPartners();

        if (partners == null)
            return null;

        List<SearchTask> searchTasks = new LinkedList<>();

        // for each approved partner run the search task
        for (RemotePartner partner : partners) {
            if (partner.getUrl() == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;

            SearchTask task = runSearchThread(partner, query);
            searchTasks.add(task);
        }

        WebSearchResults searchResults = new WebSearchResults(searchTasks.size() + 1);

        // search this instance (gives the threads time to complete)
//        String inWWoR = Utils.getConfigValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        if (includeThisInstance) {
            SearchController searchController = new SearchController();
            SearchResults results = searchController.runSearch(null, query);
            searchResults.setQuery(query);

            WebResult thisResult = new WebResult();
            thisResult.getResults().addAll(results.getResults());
            thisResult.setCount(results.getResultCount());

            String url = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
            String projectName = Utils.getConfigValue(ConfigurationKey.PROJECT_NAME);
            RegistryPartner thisPartner = new RegistryPartner();
            thisPartner.setUrl(url);
            thisPartner.setName(projectName);

            thisResult.setPartner(thisPartner);
            searchResults.getResults().add(thisResult);
            searchResults.setTotalCount(thisResult.getCount());
        }

        // go through tasks and check if completed
        while (!searchTasks.isEmpty()) {
            Iterator<SearchTask> iterator = searchTasks.iterator();

            // todo : set time limit and abandon task (shutdown) if exceeded (or after give it till after this instance is searched)
            while (iterator.hasNext()) {
                SearchTask task = iterator.next();
                TaskStatus status = task.getStatus();
                if (status == TaskStatus.COMPLETED || status == TaskStatus.EXCEPTION) {
                    iterator.remove();
                    if (status == TaskStatus.COMPLETED) {
                        WebResult webResult = new WebResult();
                        webResult.setPartner(task.getPartner().toDataTransferObject());

                        SearchResults partnerResults = task.getResults();
                        webResult.setCount(partnerResults.getResultCount());
                        webResult.getResults().addAll(partnerResults.getResults());

                        searchResults.getResults().add(webResult);
                        searchResults.setTotalCount(searchResults.getTotalCount() + partnerResults.getResultCount());
                    }
                }
            }
        }

        return searchResults;
    }

    protected SearchTask runSearchThread(RemotePartner partner, SearchQuery query) {
        SearchTask searchTask = new SearchTask(partner, query);
        IceExecutorService.getInstance().runTask(searchTask);
        return searchTask;
    }

    public static class SearchTask extends Task {

        private final RemotePartner partner;
        private final SearchQuery query;
        private final SearchResults results;

        public SearchTask(RemotePartner partner, SearchQuery query) {
            this.partner = partner;
            this.query = query;
            this.results = new SearchResults();
        }

        @Override
        public void execute() {
            IceRestClient client = IceRestClient.getInstance();
            SearchResults results = client.postWor(partner.getUrl(), "/rest/search", query,
                    SearchResults.class, null, partner.getApiKey());
            if (results == null)
                return;

            RegistryPartner registryPartner = partner.toDataTransferObject();
            for (SearchResult result : results.getResults()) {
                result.setPartner(registryPartner);
                this.results.getResults().add(result);
            }
            this.results.setResultCount(results.getResultCount());
        }

        public SearchResults getResults() {
            return this.results;
        }

        public RemotePartner getPartner() {
            return this.partner;
        }

        // partner id -> total results from that partner
        // partner id -> results from that partner (paged)

    }
}
