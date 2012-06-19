package org.jbei.ice.client.entry.view.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;

public class DeleteSequenceHandler implements ClickHandler {

    private final RegistryServiceAsync service;
    private final long entryId;
    private Callback<Boolean> callback;

    public DeleteSequenceHandler(RegistryServiceAsync service, long entryId) {
        this.service = service;
        this.entryId = entryId;
    }

    public void setCallback(Callback<Boolean> callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(ClickEvent event) {
        try {
            service.removeSequence(AppController.sessionId, entryId, new AsyncCallback<Boolean>() {

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
            });
        } catch (org.jbei.ice.client.exception.AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
