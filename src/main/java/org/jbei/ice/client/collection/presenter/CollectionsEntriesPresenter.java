package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.menu.EntrySelectionModelMenu;
import org.jbei.ice.client.collection.menu.UserCollectionMultiSelect;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

// TODO : show table of collections on click, change view
public class CollectionsEntriesPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final ICollectionEntriesView display;

    private final EntryDataViewDataProvider entryDataProvider;

    private final CollectionEntriesDataTable collectionsDataTable;
    private final SingleSelectionModel<FolderDetails> systemFolderSelectionModel;
    private final SingleSelectionModel<FolderDetails> userFolderSelectionModel;

    // data providers
    private final ListDataProvider<FolderDetails> userListProvider;
    private final ListDataProvider<FolderDetails> systemListProvider;

    private final String param;

    // selection menu
    private final EntrySelectionModelMenu subMenu;
    private final Button addToSubmit;
    private final Button moveToSubmit;

    public CollectionsEntriesPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            ICollectionEntriesView display, String param) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        this.param = param;

        // initialize all parameters
        this.collectionsDataTable = new CollectionEntriesDataTable();
        this.systemFolderSelectionModel = new SingleSelectionModel<FolderDetails>();
        this.userFolderSelectionModel = new SingleSelectionModel<FolderDetails>();
        this.userListProvider = new ListDataProvider<FolderDetails>(new KeyProvider());
        this.systemListProvider = new ListDataProvider<FolderDetails>(new KeyProvider());
        this.entryDataProvider = new EntryDataViewDataProvider(collectionsDataTable, service);

        // Collections
        initCollectionsView();

        // selection models used for menus
        initMenuSelectionModels();

        setMenuOptions();

        // init text box
        initCreateCollectionHandlers();

        initDataProviders();

        display.setDataView(collectionsDataTable);

        // collection sub menu
        addToSubmit = new Button("Submit");
        moveToSubmit = new Button("Submit");

        UserCollectionMultiSelect add = new UserCollectionMultiSelect(addToSubmit,
                this.userListProvider);
        UserCollectionMultiSelect move = new UserCollectionMultiSelect(moveToSubmit,
                this.userListProvider);

        subMenu = new EntrySelectionModelMenu(add, move);
        this.display.setCollectionSubMenu(subMenu.asWidget());

        // handlers for the collection sub menu
        addToSubmit.addClickHandler(new AddToFolderHandler(this.service) {

            @Override
            protected ArrayList<FolderDetails> getDestination() {
                ArrayList<FolderDetails> list = new ArrayList<FolderDetails>();
                list.addAll(subMenu.getCollectionMenu().getAddToDestination());
                return list;
            }

            @Override
            protected ArrayList<Long> getEntryIds() {
                // TODO : inefficient
                ArrayList<Long> ids = new ArrayList<Long>();
                for (EntryInfo datum : collectionsDataTable.getEntries()) {
                    ids.add(Long.decode(datum.getRecordId()));
                }
                return ids;
            }
        });
    }

    private void initDataProviders() {

        // user list
        this.userListProvider.addDataDisplay(this.display.getUserCollectionMenu());
        this.display.getUserCollectionMenu().setSelectionModel(userFolderSelectionModel);

        // system list
        this.systemListProvider.addDataDisplay(this.display.getSystemCollectionMenu());
        this.display.getSystemCollectionMenu().setSelectionModel(systemFolderSelectionModel);
    }

    private void initCreateCollectionHandlers() {
        QuickCollectionAddHandler handler = new QuickCollectionAddHandler(
                this.display.getQuickAddButton(), this.display.getQuickAddBox(),
                this.userListProvider);
        handler.hideCollectionBox();
    }

    private void initCollectionsView() {

        // collections table view. single view used for all collections
        collectionsDataTable.addColumnSortHandler(new AsyncHandler(collectionsDataTable));
        DataTable<EntryInfo>.DataTableColumn<?> createdField = collectionsDataTable
                .getColumn(ColumnField.CREATED);
        collectionsDataTable.getColumnSortList().push(createdField);
    }

    private void checkAndAddEntryTable(DataTable<EntryInfo> display) {
        if (this.entryDataProvider.getDataDisplays().contains(display))
            return;

        this.entryDataProvider.addDataDisplay(display);
    }

    /**
     * Initializes the selection models used for the menu items
     * by adding the selection change handlers
     */
    private void initMenuSelectionModels() {

        // system collection menu
        this.display.getSystemCollectionMenu().setSelectionModel(systemFolderSelectionModel);
        systemFolderSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {

                final FolderDetails selected = systemFolderSelectionModel.getSelectedObject();
                if (selected == null) {
                    return;
                }

                // clear userFolderSelection....if any
                clearSelection(true);

                service.retrieveEntriesForFolder(AppController.sessionId, selected.getId(),
                    new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

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

        // user collection menu
        this.display.getUserCollectionMenu().setSelectionModel(userFolderSelectionModel);
        userFolderSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final FolderDetails selected = userFolderSelectionModel.getSelectedObject();
                if (selected == null)
                    return;

                // clear userFolderSelection....if any
                clearSelection(false);

                service.retrieveEntriesForFolder(AppController.sessionId, selected.getId(),
                    new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

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
    }

    /**
     * Clears a selection model
     * 
     * @param clearUserSelection
     *            determines which selection model to clear. true if
     *            userFoldersSelectionModel should be cleared. False otherwise.
     */

    protected void clearSelection(boolean clearUserSelection) {
        SingleSelectionModel<FolderDetails> toClear;
        if (clearUserSelection)
            toClear = userFolderSelectionModel;
        else
            toClear = systemFolderSelectionModel;

        FolderDetails prevSelected = toClear.getSelectedObject();
        if (prevSelected != null) {
            toClear.setSelected(prevSelected, false);
        }
    }

    protected void setMenuOptions() {

        // list of collections for menu
        service.retrieveCollections(AppController.sessionId,
            new AsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {

                    // split into user and system
                    if (result == null || result.isEmpty())
                        return;

                    ArrayList<FolderDetails> userFolders = new ArrayList<FolderDetails>();
                    ArrayList<FolderDetails> systemFolder = new ArrayList<FolderDetails>();
                    for (FolderDetails folder : result) {
                        if (folder.isSystemFolder())
                            systemFolder.add(folder);
                        else
                            userFolders.add(folder);
                    }

                    display.getSystemCollectionMenu().setRowData(0, systemFolder);
                    display.getSystemCollectionMenu().setSelectionModel(systemFolderSelectionModel);
                    //                    display.getUserCollectionMenu().setRowData(0, userFolders);
                    //                    display.getUserCollectionMenu().setSelectionModel(userFolderSelectionModel);
                    userListProvider.getList().addAll(userFolders);

                    // selection menu
                    //                    display.setSelectionMenu(menu);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error retrieving Collections: " + caught.getMessage());
                }
            });
    }

    protected void clearDataDisplayFromProviders() {
        if (entryDataProvider.getDataDisplays() == null
                || entryDataProvider.getDataDisplays().isEmpty())
            return;

        for (HasData<EntryInfo> view : entryDataProvider.getDataDisplays()) {
            entryDataProvider.removeDataDisplay(view);
        }
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        retrieveFolderDetails();
        container.add(this.display.asWidget());
    }

    protected void retrieveFolderDetails() {

        try {
            final long folderId = Long.decode(param);
            service.retrieveFolderDetails(AppController.sessionId, folderId,
                new AsyncCallback<FolderDetails>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failed to retrieve folder details for folderId " + folderId);
                    }

                    @Override
                    public void onSuccess(FolderDetails result) {
                        if (result.isSystemFolder())
                            systemFolderSelectionModel.setSelected(result, true);
                        else
                            userFolderSelectionModel.setSelected(result, true);
                    }
                });
        } catch (NumberFormatException nfe) {
        }
    }

    //
    // inner classes
    //
    public class KeyProvider implements ProvidesKey<FolderDetails> {

        @Override
        public Object getKey(FolderDetails item) {
            return item.getId();
        }
    }
}
