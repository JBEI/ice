package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
        service.removeSequence(AppController.sessionId, entryId, new AsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (callback != null)
                    callback.onSucess(true);
            }

            @Override
            public void onFailure(Throwable caught) {
                if (callback != null)
                    callback.onFailure();
            }
        });
    }
}
