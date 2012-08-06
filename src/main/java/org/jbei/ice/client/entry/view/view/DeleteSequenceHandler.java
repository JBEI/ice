package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.exception.AuthenticationException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler for deleting sequence
 *
 * @author Hector Plahar
 */
public class DeleteSequenceHandler implements ClickHandler {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final long entryId;
    private Callback<Boolean> callback;

    public DeleteSequenceHandler(RegistryServiceAsync service, HandlerManager eventBus, long entryId) {
        this.service = service;
        this.eventBus = eventBus;
        this.entryId = entryId;
    }

    public void setCallback(Callback<Boolean> callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(ClickEvent event) {
        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> serviceCallback) throws AuthenticationException {
                try {
                    service.removeSequence(AppController.sessionId, entryId, serviceCallback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(Boolean result) {
                if (callback != null)
                    callback.onSuccess(true);
            }

            @Override
            public void onFailure(Throwable caught) {
                if (callback != null)
                    callback.onFailure();
            }
        }.go(eventBus);
    }
}
