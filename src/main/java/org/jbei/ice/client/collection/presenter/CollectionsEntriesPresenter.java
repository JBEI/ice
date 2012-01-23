package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.menu.CollectionEntryMenu;
import org.jbei.ice.client.collection.menu.CollectionUserMenu;
import org.jbei.ice.client.collection.menu.EntrySelectionModelMenu;
import org.jbei.ice.client.collection.menu.UserCollectionMultiSelect;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

// TODO : show table of collections on click, change view
public class CollectionsEntriesPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final ICollectionEntriesView display;

    private final EntryDataViewDataProvider entryDataProvider;
    private final CollectionEntriesDataTable collectionsDataTable;

    // data providers
    private final ListDataProvider<FolderDetails> userListProvider;
    private final ListDataProvider<FolderDetails> systemListProvider;

    // selection menu
    private final EntrySelectionModelMenu subMenu;
    private final Button addToSubmit;
    private final Button moveToSubmit;

    public CollectionsEntriesPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final ICollectionEntriesView display, String param) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        // initialize all parameters
        this.collectionsDataTable = new CollectionEntriesDataTable();
        this.userListProvider = new ListDataProvider<FolderDetails>(new KeyProvider());
        this.systemListProvider = new ListDataProvider<FolderDetails>(new KeyProvider());
        this.entryDataProvider = new EntryDataViewDataProvider(collectionsDataTable, service);

        // Collections
        initCollectionsView();

        // selection models used for menus
        initMenus();

        // init text box
        initCreateCollectionHandlers();

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
            public void onClick(ClickEvent event) {
                super.onClick(event);
                subMenu.getCollectionMenu().hidePopup();
                Set<FolderDetails> folders = subMenu.getCollectionMenu().getAddToDestination();
                display.getUserCollectionMenu().setBusyIndicator(folders);
                // TODO : return list of folders that have the busy indicator set to enable easy updateCounts();
            }

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

            @Override
            public void onAddSuccess(ArrayList<FolderDetails> results) {
                //                Set<FolderDetails> folders = subMenu.getCollectionMenu().getAddToDestination();
                //                for (FolderDetails folder : folders) {
                //                    
                //                }
                display.getUserCollectionMenu().updateCounts(results);
            }
        });

        // retrieve the referenced folder
        if (param != null)
            retrieveEntriesForFolder(Long.decode(param));
    }

    private void initCreateCollectionHandlers() {
        final TextBox quickAddBox = this.display.getUserCollectionMenu().getQuickAddBox();
        quickAddBox.setVisible(false);

        quickAddBox.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() != KeyCodes.KEY_ENTER)
                    return;

                if (quickAddBox.getText().isEmpty()) {
                    quickAddBox.setStyleName("entry_input_error");
                    return;
                }

                quickAddBox.setVisible(false);
                saveCollection(quickAddBox.getText());
                display.getUserCollectionMenu().hideQuickText();
            }
        });

        quickAddBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                quickAddBox.setText("");
            }
        });
    }

    private void saveCollection(String value) {
        if (value == null || value.isEmpty())
            return;

        // TODO : actual save
        service.createUserCollection(AppController.sessionId, value, "",
            new AsyncCallback<FolderDetails>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error creating folder");
                }

                @Override
                public void onSuccess(FolderDetails result) {
                    userListProvider.getList().add(result);
                    display.getUserCollectionMenu().addFolderDetail(result);
                }
            });
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
    private void initMenus() {

        // system collection menu
        final CollectionEntryMenu menu = this.display.getSystemCollectionMenu();
        menu.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!menu.isValidClick(event))
                    return;

                retrieveEntriesForFolder(menu.getCurrentSelection());
            }
        });

        // user collection menu
        final CollectionUserMenu userMenu = this.display.getUserCollectionMenu();
        userMenu.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!userMenu.isValidClick(event))
                    return;

                retrieveEntriesForFolder(userMenu.getCurrentSelection());
            }
        });

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

                    display.getSystemCollectionMenu().setFolderDetails(systemFolder);
                    display.getUserCollectionMenu().setFolderDetails(userFolders);
                    userListProvider.getList().addAll(userFolders);
                    systemListProvider.getList().addAll(systemFolder);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error retrieving Collections: " + caught.getMessage());
                }
            });
    }

    private void retrieveEntriesForFolder(long folderId) {
        History.newItem(Page.COLLECTIONS.getLink() + ";id=" + folderId, false);

        service.retrieveEntriesForFolder(AppController.sessionId, folderId,
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
        container.add(this.display.asWidget());
    }

    private class KeyProvider implements ProvidesKey<FolderDetails> {

        @Override
        public Long getKey(FolderDetails item) {
            return item.getId();
        }
    }
}
