package org.jbei.ice.client.presenter;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class BulkImportPresenter extends Presenter {

    public interface Display {

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public BulkImportPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            Display display) {

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
