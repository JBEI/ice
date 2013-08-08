package org.jbei.ice.client.entry.display.model;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.RegistryService;
import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * A suggest oracle for making rpc calls for the permission suggestions.
 * <p/>
 * It accepts a request that essentially contains a substring to be
 * used to find matching user/group name/labels.
 *
 * @author Hector Plahar
 */
public class PermissionSuggestOracle extends SuggestOracle {

    private final RegistryServiceAsync service = GWT.create(RegistryService.class);

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {
        try {
            service.getPermissionSuggestions(ClientController.sessionId, request, new AsyncCallback<Response>() {

                @Override
                public void onSuccess(Response result) {
                    callback.onSuggestionsReady(request, result);
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.onSuggestionsReady(request, new Response());
                }
            });
        } catch (org.jbei.ice.client.exception.AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
