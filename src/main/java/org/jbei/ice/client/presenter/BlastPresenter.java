package org.jbei.ice.client.presenter;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.BlastOption;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class BlastPresenter implements Presenter {

    public interface Display {

        String getSequence();

        BlastOption getProgram();

        HasClickHandlers getSubmit();

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public BlastPresenter(RegistryServiceAsync service, HandlerManager eventBus, Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        this.display.getSubmit().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // service call here
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
