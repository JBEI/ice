package org.jbei.ice.client.entry.view.handler;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploadPasteSequenceHandler implements ClickHandler {

    private SequenceViewPanelPresenter presenter;
    private RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public UploadPasteSequenceHandler(RegistryServiceAsync service, HandlerManager eventBus,
            SequenceViewPanelPresenter presenter) {
        this.service = service;
        this.eventBus = eventBus;
        this.presenter = presenter;
    }

    @Override
    public void onClick(ClickEvent event) {
        final String sequence = presenter.getSequence();
        final EntryInfo info = presenter.getEntry();

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.saveSequence(ClientController.sessionId, info.getId(), sequence, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                presenter.setHasSequence(result);
                if (result) {
                    presenter.getEntry().setHasSequence(true);
                    presenter.getEntry().setHasOriginalSequence(true);
                    presenter.updateSequenceView();
                } else {
                    Window.alert("Could not save sequence");
                }
            }
        }.go(eventBus);
    }
}
