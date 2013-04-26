package org.jbei.ice.client.search.advanced;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.search.SearchQuery;
import org.jbei.ice.shared.dto.search.SearchResults;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Model for search that communicates search requests to the remote service
 *
 * @author Hector Plahar
 */
public class SearchModel {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public SearchModel(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public void performSearch(final SearchQuery query, final boolean isWeb, final Callback<SearchResults> callback) {

        new IceAsyncCallback<SearchResults>() {

            @Override
            protected void callService(AsyncCallback<SearchResults> callback) throws AuthenticationException {
                service.performSearch(ClientController.sessionId, query, isWeb, callback);
            }

            @Override
            public void onSuccess(SearchResults result) {
                if (result == null)
                    callback.onFailure();
                callback.onSuccess(result);
            }

            @Override
            public void serverFailure() {
                callback.onFailure();
            }
        }.go(eventBus);
    }

    public HandlerManager getEventBus() {
        return eventBus;
    }
}
