package org.jbei.ice.client;

import org.jbei.ice.client.service.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

public abstract class AbstractPresenter {

    protected final RegistryServiceAsync service;
    protected final HandlerManager eventBus;

    public AbstractPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        this.eventBus = eventBus;
        this.service = service;
    }

    /**
     * "Entry Point" for any presenter that implements this
     *
     * @param container Container for displaying views
     */
    public abstract void go(final HasWidgets container);
}
