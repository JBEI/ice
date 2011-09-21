package org.jbei.ice.client.presenter;

import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.LogoutEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class HomePagePresenter implements Presenter {

    public interface Display {

        Widget asWidget();

        ILogoutHandler getLogoutHandler();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public HomePagePresenter(RegistryServiceAsync service, HandlerManager eventBus, Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        bind();
    }

    protected void bind() {
        HasClickHandlers handler = this.display.getLogoutHandler().getClickHandler();
        if (handler == null)
            return; // TODO fire logout event?

        handler.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new LogoutEvent());
            }
        });
    }

    @Override
    public void go(HasWidgets container) {

        container.clear();
        container.add(this.display.asWidget());
    }
}
