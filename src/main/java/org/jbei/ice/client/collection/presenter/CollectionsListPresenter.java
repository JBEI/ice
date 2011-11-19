package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.table.CollectionListTable;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * Presenter for showing list of collections
 * 
 * @author Hector Plahar
 */
public class CollectionsListPresenter extends AbstractPresenter {

    public interface Display {

        Button getAddCollectionButton();

        Button getCancelSubmitCollectionButton();

        Button getSubmitCollectionButton();

        void showAddCollectionWidget();

        void hideAddCollectionWidget();

        CollectionListTable getDataTable();

        CollectionListTable getUserDataTable();

        Widget asWidget();

        String getCollectionName();

        String getCollectionDescription();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public CollectionsListPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            Display display) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        init();
    }

    protected void init() {

        service.retrieveCollections(AppController.sessionId,
            new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    if (result == null || result.isEmpty())
                        return;

                    // TODO : find a way to pass this on to the EntriesView
                    ArrayList<FolderDetails> system = new ArrayList<FolderDetails>();
                    ArrayList<FolderDetails> user = new ArrayList<FolderDetails>();

                    for (FolderDetails folder : result) {
                        if (folder.isSystemFolder())
                            system.add(folder);
                        else
                            user.add(folder);
                    }

                    display.getDataTable().setData(system);
                    display.getUserDataTable().setData(user);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error retrieving Collections: " + caught.getMessage());
                }
            });

        this.display.getAddCollectionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.showAddCollectionWidget();
            }
        });

        this.display.getCancelSubmitCollectionButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                display.hideAddCollectionWidget();
            }
        });

        this.display.getSubmitCollectionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String collectionName = display.getCollectionName();
                String collectionDescription = display.getCollectionDescription();
                service.createUserCollection(AppController.sessionId, collectionName,
                    collectionDescription, new AsyncCallback<Long>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error creating collection: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Long result) {
                            Window.alert("Successfully created collection with id " + result
                                    + ". Please refresh");
                            display.getUserDataTable().redraw();
                        }
                    });
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
