package org.jbei.ice.client.entry.view;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploadPasteSequenceHandler implements ClickHandler {

    private SequenceViewPanelPresenter presenter;
    private RegistryServiceAsync service;

    public UploadPasteSequenceHandler(RegistryServiceAsync service,
            SequenceViewPanelPresenter presenter) {
        this.service = service;
        this.presenter = presenter;
    }

    @Override
    public void onClick(ClickEvent event) {

        String sequence = presenter.getSequence();
        EntryInfo info = presenter.getEntry();
        service.saveSequence(AppController.sessionId, info.getId(), sequence,
            new AsyncCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean result) {
                    presenter.setHasSequence(result);
                    if (result)
                        presenter.updateSequenceView();
                    // else 
                    // TODO notify user of error

                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO notify user of error
                }
            });

    }
}
