package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Entry model for remote service communication
 *
 * @author Hector Plahar
 */

public class EntryModel {

    private final IEntryView display;
    private final HandlerManager eventBus;
    private final RegistryServiceAsync service;
    private final HashMap<EntryType, SampleLocation> cache;

    public EntryModel(final RegistryServiceAsync service, IEntryView display, HandlerManager eventBus) {
        this.display = display;
        this.eventBus = eventBus;
        this.service = service;
        this.cache = new HashMap<EntryType, SampleLocation>();
    }

    public void retrieveStorageSchemes(final PartData currentInfo) {
        SampleLocation cacheLocation = cache.get(currentInfo.getType());
        if (cacheLocation != null) {
            display.setSampleOptions(cacheLocation);
            display.setSampleFormVisibility(!display.getSampleFormVisibility());
            SampleAddHandler handler = new SampleAddHandler(currentInfo);
            display.addSampleSaveHandler(handler);
            return;
        }

        service.retrieveStorageSchemes(ClientController.sessionId, currentInfo.getType(),
                                       new AsyncCallback<HashMap<PartSample, ArrayList<String>>>() {

                                           @Override
                                           public void onFailure(Throwable caught) {
                                               String msg = "Failed to retrieve the sample location data";
                                               eventBus.fireEvent(new FeedbackEvent(true, msg));
                                           }

                                           @Override
                                           public void onSuccess(HashMap<PartSample, ArrayList<String>> result) {
                                               if (result == null)
                                                   return;

                                               SampleLocation sampleLocation = new SampleLocation(result);
                                               cache.put(currentInfo.getType(), sampleLocation);
                                               display.setSampleOptions(sampleLocation);
                                               display.setSampleFormVisibility(!display.getSampleFormVisibility());
                                               SampleAddHandler handler = new SampleAddHandler(currentInfo);
                                               display.addSampleSaveHandler(handler);
                                           }
                                       });
    }

    public ServiceDelegate<PartSample> createDeleteSampleHandler() {
        return new ServiceDelegate<PartSample>() {
            @Override
            public void execute(final PartSample partSample) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.deleteSample(ClientController.sessionId, partSample, callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        // TODO : remove from display
                    }
                }.go(eventBus);
            }
        };
    }

    private class SampleAddHandler implements ClickHandler {

        private final PartData currentInfo;

        public SampleAddHandler(PartData currentInfo) {
            this.currentInfo = currentInfo;
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
                    service.createSample(ClientController.sessionId, sample, currentInfo.getId(), callback);
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
                    display.setSampleData(currentInfo.getSampleStorage(), createDeleteSampleHandler());
                    display.getMenu().incrementMenuCount(MenuItem.Menu.SAMPLES);
                }
            }.go(eventBus);
        }
    }
}
