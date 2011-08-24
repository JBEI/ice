package org.jbei.ice.client.presenter;

import java.util.ArrayList;
import java.util.Arrays;

import org.jbei.ice.client.EntryMenu;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.EntryDataView;
import org.jbei.ice.shared.Folder;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class CollectionsPresenter implements Presenter {

    public interface Display {

        HasData<EntryDataView> getDataView();

        HasData<Folder> getCollectionMenu();

        HasData<EntryMenu> getEntryMenu();

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    private EntryMenu menuSelection;
    private Folder folderSelection;
    final private SingleSelectionModel<EntryMenu> menuModel;
    final private SingleSelectionModel<Folder> folderModel;

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        // selection models
        menuModel = new SingleSelectionModel<EntryMenu>();
        folderModel = new SingleSelectionModel<Folder>();

        menuSelection = EntryMenu.MINE;

        bind();
    }

    protected void bind() {

        // selection model for collection menu
        this.display.getCollectionMenu().setSelectionModel(folderModel);
        folderModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final Folder selected = folderModel.getSelectedObject();
                if (selected == null) {
                    return;
                }

                service.retrieveEntriesForFolder("", selected,
                    new AsyncCallback<ArrayList<EntryDataView>>() {
                        @Override
                        public void onSuccess(ArrayList<EntryDataView> result) {
                            if (result == null)
                                return;

                            // clear current menu selection                            
                            menuModel.setSelected(menuSelection, false);
                            folderSelection = selected;

                            // set data
                            ListDataProvider<EntryDataView> dataProvider = new ListDataProvider<EntryDataView>();
                            dataProvider.getList().addAll(result);
                            dataProvider.addDataDisplay(display.getDataView());
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error: " + caught.getMessage());
                        }
                    });
            }
        });

        // list of collections for menu
        service.retrieveCollections("", new AsyncCallback<ArrayList<Folder>>() {

            @Override
            public void onSuccess(ArrayList<Folder> result) {
                display.getCollectionMenu().setRowData(0, result);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error retrieving Collections: " + caught.getMessage());
            }
        });

        // entries menu (set default of my entries)
        this.display.getEntryMenu().setRowData(0, Arrays.asList(EntryMenu.values()));
        this.display.getEntryMenu().setSelectionModel(menuModel);
        menuModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final EntryMenu selection = menuModel.getSelectedObject();
                if (selection == null)
                    return;

                service.retrieveEntriesForMenu("", selection,
                    new AsyncCallback<ArrayList<EntryDataView>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<EntryDataView> result) {
                            if (result == null)
                                return;

                            // clear folder selection
                            if (folderSelection != null)
                                folderModel.setSelected(folderSelection, false);

                            menuSelection = selection;

                            ListDataProvider<EntryDataView> dataProvider = new ListDataProvider<EntryDataView>();
                            dataProvider.getList().addAll(result);
                            dataProvider.addDataDisplay(display.getDataView());
                        }
                    });
            }
        });

        // set default
        menuModel.setSelected(menuSelection, true);
    }

    @Override
    public void go(HasWidgets container) {

        // TODO : validate the session Id. if not valid then 
        // History.newItem(Pages.LOGIN.getToken());

        container.clear();
        container.add(this.display.asWidget());
    }
}
