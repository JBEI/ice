package org.jbei.ice.client.admin;

import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;

public interface AdminPanelPresenter<T> {

    void go(RegistryServiceAsync service, HandlerManager eventBus);

    AdminPanel getView();
}
