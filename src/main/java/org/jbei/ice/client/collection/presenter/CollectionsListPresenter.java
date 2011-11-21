package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.ICollectionListView;
import org.jbei.ice.client.collection.model.CreateCollectionPanel;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Presenter for showing list of collections
 * 
 * @author Hector Plahar
 */
public class CollectionsListPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final ICollectionListView display;
    private final CreateCollectionPanel panel;
    private final ArrayList<FolderDetails> userFolderDetails;

    public CollectionsListPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            ICollectionListView display) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        panel = new CreateCollectionPanel();
        userFolderDetails = new ArrayList<FolderDetails>();

        init();
        addPanelHandlers();
    }

    protected void init() {

        service.retrieveCollections(AppController.sessionId,
            new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    if (result == null || result.isEmpty())
                        return;

                    ArrayList<FolderDetails> system = new ArrayList<FolderDetails>();
                    ArrayList<FolderDetails> user = new ArrayList<FolderDetails>();

                    // TODO : performance issue here on large result set
                    for (FolderDetails folder : result) {
                        if (folder.isSystemFolder())
                            system.add(folder);
                        else
                            user.add(folder);
                    }

                    display.getDataTable().setData(system);
                    display.getUserDataTable().setData(user);
                    userFolderDetails.addAll(user);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error retrieving Collections: " + caught.getMessage());
                }
            });

        this.display.getAddCollectionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.showAddCollectionWidget(panel);
            }
        });
    }

    private void addPanelHandlers() {

        this.panel.addSubmitHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String collectionName = panel.getCollectionName();
                String collectionDescription = panel.getCollectionDescription();
                display.hideAddCollectionWidget();
                panel.reset();

                service.createUserCollection(AppController.sessionId, collectionName,
                    collectionDescription, new AsyncCallback<FolderDetails>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error creating collection: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(FolderDetails result) {
                            // TODO : 
                            userFolderDetails.add(0, result);
                            display.getUserDataTable().setData(userFolderDetails);
                        }
                    });
            }
        });

        this.panel.addCancelHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.hideAddCollectionWidget();
                panel.reset();
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
