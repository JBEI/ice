package org.jbei.ice.client.entry.view.model;

import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * A suggest oracle for making rpc calls for
 * the suggestions
 */

public class PermissionSuggestOracle extends SuggestOracle {

    // TODO : this probably should not be here
    private final RegistryServiceAsync service = GWT.create(RegistryService.class);

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        service.getPermissionSuggestions(request, new AsyncCallback<SuggestOracle.Response>() {

            @Override
            public void onSuccess(Response result) {
                callback.onSuggestionsReady(request, result);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onSuggestionsReady(request, new Response());
            }
        });
    }
}
