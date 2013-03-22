package org.jbei.ice.client.entry.view.view;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class SampleAddHandler implements ClickHandler {

    private final EntryInfo currentInfo;
    private final IEntryView display;
    private final HandlerManager eventBus;
    private final RegistryServiceAsync service;

    public SampleAddHandler(EntryInfo currentInfo, final RegistryServiceAsync service, IEntryView display,
            HandlerManager eventBus) {
        this.currentInfo = currentInfo;
        this.display = display;
        this.eventBus = eventBus;
        this.service = service;
    }

    @Override
    public void onClick(ClickEvent event) {
        final SampleStorage sample = display.getSampleAddFormValues();
        if (sample == null)
            return;

        new IceAsyncCallback<SampleStorage>() {

            @Override
            protected void callService(AsyncCallback<SampleStorage> callback)
                    throws AuthenticationException {
                service.createSample(AppController.sessionId, sample, currentInfo.getId(), callback);
            }

            @Override
            public void onSuccess(SampleStorage result) {
                if (result == null) {
                    FeedbackEvent feedback = new FeedbackEvent(true, "Could not save sample");
                    eventBus.fireEvent(feedback);
                    return;
                }
                display.setSampleFormVisibility(false);
                currentInfo.getSampleStorage().add(result);
                display.setSampleData(currentInfo.getSampleStorage());
                // TODO : update counts and show the loading indicator when the sample is being created
                // TODO : on click.
            }
        }.go(eventBus);
    }
}
