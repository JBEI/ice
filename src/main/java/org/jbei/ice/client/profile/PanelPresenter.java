package org.jbei.ice.client.profile;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;

import com.google.gwt.event.shared.HandlerManager;

/**
 * @author Hector Plahar
 */
public abstract class PanelPresenter {

    protected final RegistryServiceAsync service;
    protected final HandlerManager eventBus;

    public PanelPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    public abstract IUserProfilePanel getView();
}
