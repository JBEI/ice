package org.jbei.ice.client.entry.display.handler;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.entry.display.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handler for pasting sequence as part of entry creation or edit
 *
 * @author Hector Plahar
 */
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
        final PartData info = presenter.getPartData();

        new IceAsyncCallback<PartData>() {

            @Override
            protected void callService(AsyncCallback<PartData> callback) throws AuthenticationException {
                service.saveSequence(ClientController.sessionId, info, sequence, callback);
            }

            @Override
            public void onSuccess(PartData result) {
                boolean hasSequence = result != null;
                presenter.setHasSequence(hasSequence);
                if (!hasSequence) {
                    Window.alert("Could not save sequence");
                    return;
                }

                presenter.getPartData().setHasSequence(true);
                presenter.getPartData().setHasOriginalSequence(true);
                presenter.getPartData().setId(result.getId());
                presenter.getPartData().setRecordId(result.getRecordId());
                presenter.updateSequenceView();
            }
        }.go(eventBus);
    }
}
