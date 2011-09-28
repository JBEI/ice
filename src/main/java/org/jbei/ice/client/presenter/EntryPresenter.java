package org.jbei.ice.client.presenter;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.entryview.EntryDetailView;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class EntryPresenter extends Presenter {

    public interface Display {

        Widget asWidget();

        void setEntryDetailView(EntryDetailView view);
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;
    private final String sid = AppController.sessionId;

    public EntryPresenter(RegistryServiceAsync service, HandlerManager eventBus, Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        //        bind();
    }

    public EntryPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final Display display, String entryId) {
        this(service, eventBus, display);

        final long id = Long.decode(entryId);
        service.retrieveEntryDetails(sid, id, new AsyncCallback<EntryInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve entry details: " + caught.getMessage());
            }

            @Override
            public void onSuccess(EntryInfo result) {
                if (result == null)
                    Window.alert("Received null entry"); // TODO
                display.setEntryDetailView(new EntryDetailView(result));
            }
        });
    }

    protected void bind() {
        String test = Window.Location.getParameter("test");
        System.out.println(test);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
