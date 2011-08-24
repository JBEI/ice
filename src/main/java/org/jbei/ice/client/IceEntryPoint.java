package org.jbei.ice.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class IceEntryPoint implements EntryPoint {

    /**
     * Create a remote service proxy to talk to the server-side
     */
    private final RegistryServiceAsync service = GWT.create(RegistryService.class);

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        HandlerManager eventBus = new HandlerManager(null);
        AppController appViewer = new AppController(service, eventBus);
        appViewer.go(RootPanel.get());
    }
}
