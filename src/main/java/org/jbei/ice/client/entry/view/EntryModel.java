package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.EntryType;
import org.jbei.ice.shared.dto.SampleInfo;

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

    public EntryModel(final RegistryServiceAsync service, IEntryView display,
            HandlerManager eventBus) {
        this.display = display;
        this.eventBus = eventBus;
        this.service = service;
        this.cache = new HashMap<EntryType, SampleLocation>();
    }

    public void retrieveStorageSchemes(final EntryInfo currentInfo) {
        SampleLocation cacheLocation = cache.get(currentInfo.getType());
        if (cacheLocation != null) {
            display.setSampleOptions(cacheLocation);
            display.setSampleFormVisibility(!display.getSampleFormVisibility());
            return;
        }

        service.retrieveStorageSchemes(AppController.sessionId, currentInfo.getType(),
            new AsyncCallback<HashMap<SampleInfo, ArrayList<String>>>() {

                @Override
                public void onFailure(Throwable caught) {
                    eventBus.fireEvent(new FeedbackEvent(true,
                            "Failed to retrieve the sample location data."));
                }

                @Override
                public void onSuccess(HashMap<SampleInfo, ArrayList<String>> result) {
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

    private class SampleAddHandler implements ClickHandler {

        private final EntryInfo currentInfo;

        public SampleAddHandler(EntryInfo currentInfo) {
            this.currentInfo = currentInfo;
        }

        @Override
        public void onClick(ClickEvent event) {
            SampleStorage sample = display.getSampleAddFormValues();
            if (sample == null)
                return;

            try {
                service.createSample(AppController.sessionId, sample, currentInfo.getId(),
                    new AsyncCallback<SampleStorage>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            FeedbackEvent feedback = new FeedbackEvent(true, "Could not save sample");
                            eventBus.fireEvent(feedback);
                        }

                        @Override
                        public void onSuccess(SampleStorage result) {
                            if (result == null) {
                                FeedbackEvent feedback = new FeedbackEvent(true,
                                        "Could not save sample");
                                eventBus.fireEvent(feedback);
                                return;
                            }
                            display.setSampleFormVisibility(false);
                            currentInfo.getSampleStorage().add(result);
                            display.setSampleData(currentInfo.getSampleStorage());
                            // TODO : update counts and show the loading indicator when the sample is being created
                            // TODO : on click.
                        }
                    });
            } catch (org.jbei.ice.client.exception.AuthenticationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
