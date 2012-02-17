package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.add.EntryAddPresenter;
import org.jbei.ice.client.collection.event.EntryIdsEvent;
import org.jbei.ice.client.collection.event.EntryIdsEventHandler;
import org.jbei.ice.client.collection.event.FolderEvent;
import org.jbei.ice.client.collection.event.FolderEventHandler;
import org.jbei.ice.client.collection.event.FolderRetrieveEvent;
import org.jbei.ice.client.collection.event.FolderRetrieveEventHandler;
import org.jbei.ice.client.collection.menu.EntrySelectionModelMenu;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.menu.UserCollectionMultiSelect;
import org.jbei.ice.client.collection.model.CollectionsModel;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

// TODO : show table of collections on click, change view
public class CollectionsEntriesPresenter extends AbstractPresenter {

    //    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final ICollectionEntriesView display;

    private final EntryDataViewDataProvider entryDataProvider;
    private final CollectionEntriesDataTable collectionsDataTable;

    //  data providers for the sub menu
    private final ListDataProvider<FolderDetails> userListProvider;
    private final ListDataProvider<FolderDetails> systemListProvider;

    // selection menu
    // TODO : three widgets below should be moved to view
    private final EntrySelectionModelMenu subMenu;
    private final Button addToSubmit;
    private final Button moveToSubmit;

    private final CollectionsModel model;

    // feedback panel
    private final FeedbackPanel feedbackPanel;

    private EntryAddPresenter entryPresenter;

    public CollectionsEntriesPresenter(final RegistryServiceAsync service,
            final HandlerManager eventBus, final ICollectionEntriesView display, String param) {

        //        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        feedbackPanel = new FeedbackPanel("450px");
        display.setFeedback(feedbackPanel);
        model = new CollectionsModel(service, this.eventBus);

        // initialize all parameters
        this.collectionsDataTable = new CollectionEntriesDataTable(new EntryTablePager());
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
                this.userListProvider, new SingleSelectionHandler());
        UserCollectionMultiSelect move = new UserCollectionMultiSelect(moveToSubmit,
                this.userListProvider, new SingleSelectionHandler());

        subMenu = new EntrySelectionModelMenu(add, move);
        this.display.setCollectionSubMenu(subMenu.asWidget());

        // handlers for the collection sub menu
        CollectionEntryAddToFolderHandler addToFolderHandler = new CollectionEntryAddToFolderHandler(
                service);
        addToSubmit.addClickHandler(addToFolderHandler);

        // retrieve the referenced folder
        // TODO : highlight menu
        if (param != null)
            retrieveEntriesForFolder(param);

        // entry add
        entryPresenter = new EntryAddPresenter(service, eventBus, feedbackPanel);

        // create entry handler
        final SingleSelectionModel<EntryAddType> selectionModel = display
                .getAddEntrySelectionHandler();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                entryPresenter.setType(selectionModel.getSelectedObject());
                display.setMainContent(entryPresenter.getView());
            }
        });
    }

    private void initCreateCollectionHandlers() {
        this.display.setQuickAddVisibility(false);
        this.display.addQuickAddKeyHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() != KeyCodes.KEY_ENTER)
                    return;

                display.setQuickAddVisibility(false);
                saveCollection(display.getCollectionInputValue());
                display.hideQuickAddInput();
            }
        });

        // quick edit
        display.addQuickEditBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                handle();
            }
        });

        display.addQuickEditKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() != KeyCodes.KEY_ENTER)
                    return;
                handle();
            }
        });
    }

    private void handle() {
        if (!display.getQuickEditVisibility())
            return;

        final String oldCollectionName = display.getCurrentMenuItemSelection().getName();
        String newName = display.getQuickEditInput();

        final MenuItem item = display.getCurrentMenuEditSelection();

        if (!oldCollectionName.equals(newName)) {
            // RPC with newName
            // TODO : show busy signal
            item.setName(newName);
            FolderDetails editFolder = new FolderDetails();
            editFolder.setCount(item.getCount());
            editFolder.setName(item.getName());
            editFolder.setId(Long.decode(item.getId()));

            model.updateFolder(Long.decode(item.getId()), editFolder, new FolderEventHandler() {

                @Override
                public void onFolderEvent(FolderEvent event) {
                    FolderDetails folder = event.getFolder();
                    if (folder == null) {
                        feedbackPanel
                                .setFailureMessage("Error updating collection. Please try again");
                        item.setName(oldCollectionName);
                        display.setMenuItem(item);
                        return;
                    }
                    MenuItem resultItem = new MenuItem(folder.getId() + "", folder.getName(),
                            folder.getCount(), folder.isSystemFolder());
                    display.setMenuItem(resultItem);
                }
            });
        }
    }

    private void saveCollection(String value) {
        if (value == null || value.isEmpty())
            return;

        model.createFolder(value, new FolderEventHandler() {

            @Override
            public void onFolderEvent(FolderEvent event) {
                FolderDetails folder = event.getFolder();
                if (folder == null) {
                    feedbackPanel.setFailureMessage("Error creating new folder. Please try again");
                    return;
                }

                userListProvider.getList().add(folder);
                MenuItem newItem = new MenuItem(folder.getId() + "", folder.getName(), folder
                        .getCount(), folder.isSystemFolder());
                display.addMenuItem(newItem);
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
        display.addMenuSelectionHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                feedbackPanel.setVisible(false);
                retrieveEntriesForFolder(display.getCurrentMenuItemSelection().getId());
            }
        });

        // retrieve folders to use as menu
        model.retrieveFolders(new FolderRetrieveEventHandler() {

            @Override
            public void onMenuRetrieval(FolderRetrieveEvent event) {
                ArrayList<FolderDetails> folders = event.getItems();

                ArrayList<MenuItem> userMenuItems = new ArrayList<MenuItem>();
                ArrayList<FolderDetails> userFolders = new ArrayList<FolderDetails>();

                ArrayList<MenuItem> systemMenuItems = new ArrayList<MenuItem>();
                ArrayList<FolderDetails> systemFolder = new ArrayList<FolderDetails>();

                for (FolderDetails folder : folders) {
                    MenuItem item = new MenuItem(folder.getId() + "", folder.getName(), folder
                            .getCount(), folder.isSystemFolder());

                    if (folder.isSystemFolder()) {
                        systemMenuItems.add(item);
                    } else {
                        userMenuItems.add(item);
                    }
                }

                // my entries
                MenuItem item = new MenuItem(AppController.accountInfo.getEmail(), "My Entries",
                        AppController.accountInfo.getUserEntryCount(), true);
                userMenuItems.add(0, item);
                MenuItem allEntriesItem = new MenuItem(AppController.accountInfo.getEmail(),
                        "Available Entries", AppController.accountInfo.getVisibleEntryCount(), true);
                systemMenuItems.add(0, allEntriesItem);
                display.setSystemCollectionMenuItems(systemMenuItems);
                display.setUserCollectionMenuItems(userMenuItems);

                userListProvider.getList().addAll(userFolders);
                systemListProvider.getList().addAll(systemFolder);
            }
        });
    }

    private void retrieveEntriesForFolder(String id) {
        History.newItem(Page.COLLECTIONS.getLink() + ";id=" + id, false);

        model.retrieveEntriesForFolder(id, new EntryIdsEventHandler() {

            @Override
            public void onEntryIdsEvent(EntryIdsEvent event) {
                if (event == null || event.getIds() == null) {
                    feedbackPanel.setFailureMessage("Error connecting to server. Please try again");
                    return;
                }

                ArrayList<Long> ids = event.getIds();
                entryDataProvider.setValues(ids);
                collectionsDataTable.setVisibleRangeAndClearData(
                    collectionsDataTable.getVisibleRange(), false);
                checkAndAddEntryTable(collectionsDataTable);
                display.setDataView(collectionsDataTable);
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

    // TODO shares elements with SubmitHandler. 
    // TODO this entire class does not feel right
    private class SingleSelectionHandler implements MultiSelectSelectionHandler {

        private int entrySize;

        @Override
        public void onSingleSelect(FolderDetails details) {
            subMenu.getCollectionMenu().hidePopup();
            HashSet<FolderDetails> folders = new HashSet<FolderDetails>();
            folders.add(details);

            Set<String> menuItemIds = new HashSet<String>();
            for (FolderDetails folder : folders) {
                menuItemIds.add(folder.getId() + "");
            }
            display.setBusyIndicator(menuItemIds);

            ArrayList<Long> destinationFolderIds = new ArrayList<Long>();
            destinationFolderIds.add(details.getId());

            // TODO : inefficient
            ArrayList<Long> ids = new ArrayList<Long>();
            for (EntryInfo datum : collectionsDataTable.getEntries()) {
                if (datum == null)
                    continue;
                ids.add(Long.decode(datum.getRecordId()));
            }

            entrySize = collectionsDataTable.getEntries().size();

            // service call to actually add
            model.addEntriesToFolder(destinationFolderIds, ids, new FolderRetrieveEventHandler() {

                @Override
                public void onMenuRetrieval(FolderRetrieveEvent event) {
                    if (event == null || event.getItems() == null) {
                        feedbackPanel
                                .setFailureMessage("An error occured while connecting to the server.");
                        return;
                    }

                    ArrayList<FolderDetails> results = event.getItems();
                    ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                    for (FolderDetails result : results) {
                        items.add(new MenuItem(result.getId() + "", result.getName(), result
                                .getCount(), result.isSystemFolder()));
                    }
                    display.updateMenuItemCounts(items);
                    String msg = "<b>" + entrySize + "</b> entries successfully added to ";
                    msg += ("\"" + results.get(0).getName() + "\" collection.");
                    feedbackPanel.setSuccessMessage(msg);
                }
            });
        }
    }

    // helper class for adding to folder
    class CollectionEntryAddToFolderHandler extends AddToFolderHandler {
        private int entrySize;

        public CollectionEntryAddToFolderHandler(RegistryServiceAsync service) {
            super(service);
        }

        @Override
        public void onClick(ClickEvent event) {
            super.onClick(event);
            subMenu.getCollectionMenu().hidePopup();
            Set<FolderDetails> folders = subMenu.getCollectionMenu().getAddToDestination();
            Set<String> ids = new HashSet<String>();
            for (FolderDetails folder : folders) {
                ids.add(folder.getId() + "");
            }
            display.setBusyIndicator(ids);
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
            entrySize = ids.size();
            return ids;
        }

        @Override
        public void onAddSuccess(ArrayList<FolderDetails> results) {

            ArrayList<MenuItem> items = new ArrayList<MenuItem>();
            for (FolderDetails result : results) {
                items.add(new MenuItem(result.getId() + "", result.getName(), result.getCount(),
                        result.isSystemFolder()));
            }

            String msg = "<b>" + entrySize + "</b> entries successfully added to ";
            if (results.size() == 1)
                msg += ("\"" + results.get(0).getName() + "\" collection.");
            else
                msg += (results.size() + " collections.");
            feedbackPanel.setSuccessMessage(msg);
        }

        @Override
        public void onAddFailure(String msg) {
            feedbackPanel.setFailureMessage("An error occurred while adding the entries.");
        }
    };
}
