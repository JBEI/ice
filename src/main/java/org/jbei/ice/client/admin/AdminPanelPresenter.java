package org.jbei.ice.client.admin;

import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Parent abstract Presenter class for admin presenters
 *
 * @author Hector Plahar
 */
public abstract class AdminPanelPresenter {

    protected final RegistryServiceAsync service;
    protected final HandlerManager eventBus;

    public AdminPanelPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public abstract IAdminPanel getView();
}
