package org.jbei.ice.client.admin;

import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;

public abstract class AdminPanelPresenter {

    protected final RegistryServiceAsync service;
    protected final HandlerManager eventBus;

    public AdminPanelPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public abstract AdminPanel getView();
}
