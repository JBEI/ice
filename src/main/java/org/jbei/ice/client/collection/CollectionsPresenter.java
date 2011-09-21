package org.jbei.ice.client.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.component.EntryDataViewDataProvider;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.client.component.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.EntryMenu;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class CollectionsPresenter implements Presenter {

    public interface Display {

        HasData<FolderDetails> getCollectionMenu();

        HasData<EntryMenu> getEntryMenu();

        Widget asWidget();

        // active data view
        void setDataView(DataTable<?> table);
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    private EntryMenu menuSelection;
    private FolderDetails folderSelection;

    private EntryDataViewDataProvider entryDataProvider;
    private SamplesDataProvider samplesDataProvider;

    private DataTable<EntryData> collectionsDataTable;
    private DataTable<EntryData> entriesDataTable;
    private DataTable<EntryData> recentlyViewedDataView;
    private HasEntryDataTable<SampleInfo> samplesDataView;

    private SingleSelectionModel<EntryMenu> menuSelectionModel;
    private SingleSelectionModel<FolderDetails> folderSelectionModel;
    private final String sid;

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        // "My Entries"
        initializeMyEntriesView();

        // All Entries

        // Recently Viewed
        initializeRecentlyViewedView();

        // Samples
        initializeSamplesView();

        // Workspace

        // Collections
        initCollectionsView();

        // selection models used for menus
        initMenuSelectionModels();

        setMenuOptions();
        sid = AppController.sessionId;
        display.setDataView(entriesDataTable);
    }

    /**
     * Creates table and provider for "My entries" and adds the sorting handler
     * Pushes the "created" field onto the sort list
     */
    private void initializeMyEntriesView() {

        entriesDataTable = new CollectionEntriesDataTable();
        entryDataProvider = new EntryDataViewDataProvider(entriesDataTable, service);
    }

    private void initializeRecentlyViewedView() {
        recentlyViewedDataView = new RecentlyViewedDataTable();
    }

    private void initializeSamplesView() {
        samplesDataView = new SamplesDataTable();
        samplesDataProvider = new SamplesDataProvider(samplesDataView, service);
    }

    private void initCollectionsView() {

        // collections table view. single view used for all collections
        collectionsDataTable = new CollectionsDataTable();
        collectionsDataTable.addColumnSortHandler(new AsyncHandler(collectionsDataTable));
        DataTable<EntryData>.DataTableColumn<?> createdField = collectionsDataTable
                .getColumn(ColumnField.CREATED);
        collectionsDataTable.getColumnSortList().push(createdField);
    }

    private void checkAndAddEntryTable(DataTable<EntryData> display) {
        if (this.entryDataProvider.getDataDisplays().contains(display))
            return;

        this.entryDataProvider.addDataDisplay(display);
    }

    /**
     * Initializes the selection models used for the menu items
     * by adding the selection change handlers
     */
    private void initMenuSelectionModels() {

        menuSelectionModel = new SingleSelectionModel<EntryMenu>();
        folderSelectionModel = new SingleSelectionModel<FolderDetails>();

        // collection menu
        this.display.getCollectionMenu().setSelectionModel(folderSelectionModel);
        folderSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {

                final FolderDetails selected = folderSelectionModel.getSelectedObject();
                if (selected == null) {
                    return;
                }

                service.retrieveEntriesForFolder(sid, selected,
                    new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

                            // clear current menu selection                            
                            if (menuSelectionModel != null)
                                menuSelectionModel.setSelected(menuSelection, false);

                            folderSelection = selected;
                            //                            clearDataDisplayFromProviders();
                            entryDataProvider.setValues(result);
                            collectionsDataTable.setVisibleRangeAndClearData(
                                collectionsDataTable.getVisibleRange(), false);
                            checkAndAddEntryTable(collectionsDataTable);
                            display.setDataView(collectionsDataTable);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error: " + caught.getMessage());
                        }
                    });
            }
        });

        // entries menu
        menuSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final EntryMenu selection = menuSelectionModel.getSelectedObject();
                if (selection == null)
                    return;

                switch (selection) {

                case MINE:
                    service.retrieveUserEntries(sid, null, new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

                            // clear folder selection
                            if (folderSelection != null)
                                folderSelectionModel.setSelected(folderSelection, false);

                            menuSelection = selection;
                            entryDataProvider.setValues(result);
                            entriesDataTable.setVisibleRangeAndClearData(
                                entriesDataTable.getVisibleRange(), false);
                            checkAndAddEntryTable(entriesDataTable);
                            display.setDataView(entriesDataTable);
                        }
                    });
                    break;

                case ALL:
                    service.retrieveAllEntryIDs(sid, new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

                            // clear folder selection
                            if (folderSelection != null)
                                folderSelectionModel.setSelected(folderSelection, false);

                            menuSelection = selection;
                            checkAndAddEntryTable(entriesDataTable);

                            entriesDataTable.setVisibleRangeAndClearData(
                                entriesDataTable.getVisibleRange(), false);
                            entryDataProvider.setValues(result);
                            display.setDataView(entriesDataTable);
                        }
                    });
                    break;

                case RECENTLY_VIEWED:
                    service.retrieveRecentlyViewed(sid, new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

                            if (folderSelection != null)
                                folderSelectionModel.setSelected(folderSelection, false);

                            menuSelection = selection;
                            clearDataDisplayFromProviders();
                            entryDataProvider.addDataDisplay(recentlyViewedDataView);

                            recentlyViewedDataView.setVisibleRangeAndClearData(
                                recentlyViewedDataView.getVisibleRange(), false);
                            entryDataProvider.setValues(result);
                            display.setDataView(recentlyViewedDataView);
                        }
                    });

                    break;

                case SAMPLES:
                    service.retrieveSamplesByDepositor(sid, null, null, false,
                        new AsyncCallback<LinkedList<Long>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert("Error: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(LinkedList<Long> result) {
                                if (result == null)
                                    return;

                                if (folderSelection != null)
                                    folderSelectionModel.setSelected(folderSelection, false);

                                menuSelection = selection;
                                samplesDataProvider.setValues(result);
                                display.setDataView(samplesDataView);
                            }
                        });

                    break;

                case WORKSPACE:
                    service.retrieveWorkspaceEntries(sid, new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Error: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

                            if (folderSelection != null)
                                folderSelectionModel.setSelected(folderSelection, false);

                            menuSelection = selection;
                            entryDataProvider.setValues(result);
                        }
                    });

                    break;

                default:
                    Window.alert("Could not handle menu selection of : " + selection.getDisplay());
                }
            }
        });
    }

    protected void setMenuOptions() {

        // list of collections for menu
        service.retrieveCollections(sid, new AsyncCallback<ArrayList<FolderDetails>>() {

            @Override
            public void onSuccess(ArrayList<FolderDetails> result) {
                display.getCollectionMenu().setRowData(0, result);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error retrieving Collections: " + caught.getMessage());
            }
        });

        // TODO
        // entries menu (set default of my entries)
        // TODO : clearly needs to move to a separate class

        this.display.getEntryMenu().setRowData(0, Arrays.asList(EntryMenu.values()));
        this.display.getEntryMenu().setSelectionModel(menuSelectionModel);

        // set default
        menuSelection = EntryMenu.MINE;
        menuSelectionModel.setSelected(menuSelection, true);
    }

    protected void clearDataDisplayFromProviders() {
        if (entryDataProvider.getDataDisplays() == null
                || entryDataProvider.getDataDisplays().isEmpty())
            return;

        for (HasData<EntryData> view : entryDataProvider.getDataDisplays()) {
            entryDataProvider.removeDataDisplay(view);
        }
    }

    @Override
    public void go(HasWidgets container) {

        // TODO : validate the session Id. if not valid then 
        //         History.newItem(Pages.LOGIN.getToken());

        container.clear();
        container.add(this.display.asWidget());
    }

    //
    // inner classes
    //

}
