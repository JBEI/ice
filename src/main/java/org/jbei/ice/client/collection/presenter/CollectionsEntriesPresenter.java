package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.menu.SelectionMenu;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

// TODO : show table of collections on click, change view
public class CollectionsEntriesPresenter extends AbstractPresenter {

    public interface Display {

        HasData<FolderDetails> getSystemCollectionMenu();

        HasData<FolderDetails> getUserCollectionMenu();

        Widget asWidget();

        void setSelectionMenu(Widget widget);

        TextBox getQuickAddBox();

        Button getQuickAddButton();

        // active data view
        void setDataView(DataTable<?> table);
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    private EntryDataViewDataProvider entryDataProvider;

    private CollectionEntriesDataTable collectionsDataTable;
    private SingleSelectionModel<FolderDetails> systemFolderSelectionModel;
    private SingleSelectionModel<FolderDetails> userFolderSelectionModel;

    // data providers
    private ListDataProvider<FolderDetails> userListProvider;
    private ListDataProvider<FolderDetails> menuListProvider;

    private final String param;

    // selection menu
    private SelectionMenu menu;

    public CollectionsEntriesPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            Display display, String param) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        this.param = param;

        // initialize all parameters
        this.collectionsDataTable = new CollectionEntriesDataTable();
        this.systemFolderSelectionModel = new SingleSelectionModel<FolderDetails>();
        this.userFolderSelectionModel = new SingleSelectionModel<FolderDetails>();
        this.userListProvider = new ListDataProvider<FolderDetails>(new KeyProvider());
        this.menuListProvider = new ListDataProvider<FolderDetails>(new KeyProvider());
        this.menu = new SelectionMenu(this.userListProvider);
        this.entryDataProvider = new EntryDataViewDataProvider(collectionsDataTable, service);

        // Collections
        initCollectionsView();

        // selection models used for menus
        initMenuSelectionModels();

        setMenuOptions();

        // adds handlers for the selection menu "add to" and "move to" buttons
        initSelectionMenuHandlers();

        // init text box
        initCreateCollectionHandlers();

        initDataProviders();

        display.setDataView(collectionsDataTable);
    }

    private void initDataProviders() {

        // user list
        this.userListProvider.addDataDisplay(this.display.getUserCollectionMenu());
        this.display.getUserCollectionMenu().setSelectionModel(userFolderSelectionModel);

        // system list
        this.menuListProvider.addDataDisplay(this.display.getSystemCollectionMenu());
        this.display.getSystemCollectionMenu().setSelectionModel(systemFolderSelectionModel);
    }

    private void initSelectionMenuHandlers() {

        menu.getAddToSubmit().addClickHandler(new SubmitHandler(this.service) {

            @Override
            protected ArrayList<FolderDetails> getSource() {
                ArrayList<FolderDetails> selections = new ArrayList<FolderDetails>();
                FolderDetails selected = systemFolderSelectionModel.getSelectedObject();
                if (selected != null)
                    selections.add(selected);
                else {
                    selections.add(userFolderSelectionModel.getSelectedObject());
                }
                return selections;
            }

            @Override
            protected ArrayList<FolderDetails> getDestination() {
                // TODO : how to distinguish between the two. empty set is not a stringent enough test
                ArrayList<FolderDetails> destination = new ArrayList<FolderDetails>();
                if (menu.getMoveToDestination() != null)
                    destination.addAll(menu.getMoveToDestination()); // TODO : let never return null
                if (menu.getAddToDestination() != null)
                    destination.addAll(menu.getAddToDestination());
                return destination;
            }

            @Override
            protected ArrayList<Long> getEntryIds() {
                ArrayList<Long> ids = new ArrayList<Long>();
                for (EntryData datum : collectionsDataTable.getSelectedEntries()) {
                    ids.add(datum.getRecordId());
                }
                return ids;
            }
        });
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
                    display.setSelectionMenu(menu);
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

        for (HasData<EntryData> view : entryDataProvider.getDataDisplays()) {
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
