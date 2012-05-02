package org.jbei.ice.client.home;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

public class HomePagePresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IHomePageView display;

    public HomePagePresenter(RegistryServiceAsync service, HandlerManager eventBus,
            IHomePageView display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
