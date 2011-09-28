package org.jbei.ice.client.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.model.StorageTreeModel;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class StoragePresenter extends Presenter {

    public interface Display {

        void setTreeModel(StorageTreeModel model);

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public StoragePresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        // get storage root
        service.retrieveChildren("", 0, new AsyncCallback<ArrayList<StorageInfo>>() {

            @Override
            public void onSuccess(ArrayList<StorageInfo> result) {
                StorageTreeModel model = new StorageTreeModel(result);
                display.setTreeModel(model);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve storage \n\n" + caught.getMessage());
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
