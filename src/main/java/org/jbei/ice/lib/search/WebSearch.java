package org.jbei.ice.lib.search;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.executor.TaskStatus;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.HashMap;
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
     * @param query wrapper around search query
     * @return list of search results
     */
    public SearchResults run(SearchQuery query) {
        List<RemotePartner> partners = this.remotePartnerDAO.getRegistryPartners();

        if (partners == null)
            return null;

        final int RETRIEVE_COUNT_LIMIT_PER_INSTANCE = 20;

        // single page limit
        final int offset = query.getParameters().getStart();
        final int limit = query.getParameters().getRetrieveCount();

        // set per instance limit
        query.getParameters().setRetrieveCount(RETRIEVE_COUNT_LIMIT_PER_INSTANCE);

        int offsetCount = 0;
        int perPageOffset = 0;
        SearchResults results;

        // run search at least once
        do {
            query.getParameters().setStart(perPageOffset);
            results = runSearch(partners, query);
            if (results.getResultCount() == 0)
                break;
            offsetCount += results.getResults().size();
            perPageOffset += RETRIEVE_COUNT_LIMIT_PER_INSTANCE;
        } while (offsetCount <= offset);

        // sort the results
//        Collections.sort(resultsList, new Comparator<SearchResult>() {
//            @Override
//            public int compare(SearchResult o1, SearchResult o2) {
//                return Double.compare(o2.getScore(), o1.getScore());
//            }
//        });

        int toIndex = limit;
        int size = results.getResults().size();

        if (toIndex > size)
            toIndex = size;

        List<SearchResult> subSet = new LinkedList<>(results.getResults().subList(0, toIndex));
        results.setResults(subSet);

        return results;
    }

    // the paging params need to be removed from the query object
    protected SearchResults runSearch(List<RemotePartner> partners, SearchQuery query) {
        List<Task> searchTasks = new LinkedList<>();
        SearchResults searchResults = new SearchResults();
        HashMap<Long, Long> partnerCounts = new HashMap<>();

        for (RemotePartner partner : partners) {
            if (partner.getUrl() == null || partner.getPartnerStatus() != RemotePartnerStatus.APPROVED)
                continue;

            searchTasks.add(runSearchThread(partner, query, searchResults, partnerCounts));
        }

        while (!searchTasks.isEmpty()) {
            Iterator<Task> iterator = searchTasks.iterator();
            while (iterator.hasNext()) {
                TaskStatus status = iterator.next().getStatus();
                if (status == TaskStatus.COMPLETED || status == TaskStatus.EXCEPTION)
                    iterator.remove();
            }
        }

        long total = 0;
        for (long id : partnerCounts.keySet()) {
            total += partnerCounts.get(id);
        }
        searchResults.setResultCount(total);
        return searchResults;
    }

    protected SearchTask runSearchThread(RemotePartner partner, SearchQuery query, SearchResults searchResults,
                                         HashMap<Long, Long> counts) {
        SearchTask searchTask = new SearchTask(partner, query, searchResults, counts);
        IceExecutorService.getInstance().runTask(searchTask);
        return searchTask;
    }

    public static class SearchTask extends Task {

        private final RemotePartner partner;
        private final SearchQuery query;
        private final SearchResults results;
        private final HashMap<Long, Long> counts;

        public SearchTask(RemotePartner partner, SearchQuery query, SearchResults results, HashMap<Long, Long> counts) {
            this.partner = partner;
            this.query = query;
            this.results = results;
            this.counts = counts;
        }

        @Override
        public void execute() {
            try {
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

                counts.put(this.partner.getId(), results.getResultCount());
            } catch (Exception e) {
                Logger.warn("Exception contacting partner " + partner.getUrl() + " : " + e.getMessage());
            }
        }
    }
}
