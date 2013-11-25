package org.jbei.ice.client;

import org.jbei.ice.client.service.RegistryService;
import org.jbei.ice.client.service.RegistryServiceAsync;

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
        ClientController clientViewer = new ClientController(service, eventBus);
        clientViewer.go(RootPanel.get());
    }
}
