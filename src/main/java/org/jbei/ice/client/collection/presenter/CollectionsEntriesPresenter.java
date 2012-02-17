package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
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
import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;
import org.jbei.ice.client.search.advanced.AdvancedSearchPresenter;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

// TODO : show table of collections on click, change view
public class CollectionsEntriesPresenter extends AbstractPresenter {

    private final ICollectionEntriesView display;

    private final EntryDataViewDataProvider entryDataProvider;
    private final CollectionEntriesDataTable collectionsDataTable;

    //  data providers for the sub menu
    private final ListDataProvider<FolderDetails> userListProvider;
    private final ListDataProvider<FolderDetails> systemListProvider;

    // selection menu
    private final EntrySelectionModelMenu subMenu;
    private final CollectionsModel model;

    // feedback panel
    private final FeedbackPanel feedbackPanel;
    private EntryAddPresenter entryPresenter;
    private AdvancedSearchPresenter searchPresenter;

    // id of the folder currently selected
    private long currentFolderId;

    public CollectionsEntriesPresenter(final RegistryServiceAsync service,
            final HandlerManager eventBus, final ICollectionEntriesView display, String param) {

        this(new CollectionsModel(service, eventBus), service, eventBus, display, param);
        eventBus.addHandler(SearchEvent.TYPE, new SearchEventHandler() {

            @Override
            public void onSearch(SearchEvent event) {
                search(event.getOperands());
            }
        });
    }

    public CollectionsEntriesPresenter(final RegistryServiceAsync service,
            final HandlerManager eventBus, final ICollectionEntriesView display,
            ArrayList<FilterOperand> operands) {
        this(new CollectionsModel(service, eventBus), service, eventBus, display, null);
        search(operands);
    }

    public CollectionsEntriesPresenter(CollectionsModel model, final RegistryServiceAsync service,
            final HandlerManager eventBus, final ICollectionEntriesView display, String param) {

        this.display = display;
        feedbackPanel = new FeedbackPanel("450px");
        display.setFeedback(feedbackPanel);
        this.model = model;

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

        // collection sub menu
        UserCollectionMultiSelect add = new UserCollectionMultiSelect(this.userListProvider,
                new SingleSelectionHandler());
        add.addSubmitHandler(new CollectionEntryAddToFolderHandler(service));

        UserCollectionMultiSelect move = new UserCollectionMultiSelect(this.userListProvider,
                new SingleSelectionHandler());
        move.addSubmitHandler(new MoveEntryHandler(service));

        subMenu = new EntrySelectionModelMenu(add, move);
        this.display.setCollectionSubMenu(subMenu.asWidget());

        // retrieve the referenced folder
        // TODO : highlight menu
        long id = 0;
        try {
            if (param != null)
                id = Long.decode(param);
        } catch (NumberFormatException nfe) {
            id = 0;
        }
        retrieveEntriesForFolder(id);

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

    private void search(ArrayList<FilterOperand> operands) {
        if (operands == null)
            return;

        if (searchPresenter == null)
            searchPresenter = new AdvancedSearchPresenter(model.getService(), model.getEventBus(),
                    operands);

        display.setMainContent(searchPresenter.getView());
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

        String newName = display.getQuickEditInput();
        final MenuItem item = display.getCurrentMenuEditSelection();

        // RPC with newName
        item.setName(newName);
        FolderDetails editFolder = new FolderDetails();
        editFolder.setCount(item.getCount());
        editFolder.setName(item.getName());
        editFolder.setId(item.getId());

        model.updateFolder(item.getId(), editFolder, new FolderEventHandler() {

            @Override
            public void onFolderEvent(FolderEvent event) {
                FolderDetails folder = event.getFolder();
                if (folder == null) {
                    feedbackPanel.setFailureMessage("Error updating collection. Please try again");
                    return;
                }
                MenuItem resultItem = new MenuItem(folder.getId(), folder.getName(), folder
                        .getCount(), folder.isSystemFolder());
                display.setMenuItem(resultItem);
            }
        });
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
                MenuItem newItem = new MenuItem(folder.getId(), folder.getName(),
                        folder.getCount(), folder.isSystemFolder());
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
        final SingleSelectionModel<MenuItem> userModel = display.getUserMenuModel();
        final SingleSelectionModel<MenuItem> systemModel = display.getSystemMenuModel();

        userModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MenuItem selection = userModel.getSelectedObject();
                if (selection == null)
                    return;

                feedbackPanel.setVisible(false);
                retrieveEntriesForFolder(selection.getId());
            }
        });

        systemModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MenuItem selection = systemModel.getSelectedObject();
                if (selection == null)
                    return;

                feedbackPanel.setVisible(false);
                retrieveEntriesForFolder(selection.getId());
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
                    MenuItem item = new MenuItem(folder.getId(), folder.getName(), folder
                            .getCount(), folder.isSystemFolder());

                    if (folder.isSystemFolder()) {
                        systemMenuItems.add(item);
                        systemFolder.add(folder);
                    } else {
                        userMenuItems.add(item);
                        userFolders.add(folder);
                    }
                }

                // my entries
                MenuItem item = new MenuItem(0, "My Entries", AppController.accountInfo
                        .getUserEntryCount(), true);
                userMenuItems.add(0, item);
                MenuItem allEntriesItem = new MenuItem(-1, "Available Entries",
                        AppController.accountInfo.getVisibleEntryCount(), true);
                systemMenuItems.add(0, allEntriesItem);
                display.setSystemCollectionMenuItems(systemMenuItems);
                display.setUserCollectionMenuItems(userMenuItems);

                userListProvider.getList().addAll(userFolders);
                systemListProvider.getList().addAll(systemFolder);
            }
        });
    }

    private void retrieveEntriesForFolder(final long id) {

        model.retrieveEntriesForFolder(id, new EntryIdsEventHandler() {

            @Override
            public void onEntryIdsEvent(EntryIdsEvent event) {
                if (event == null || event.getIds() == null) {
                    feedbackPanel.setFailureMessage("Error connecting to server. Please try again");
                    return;
                }

                currentFolderId = id;
                display.setCurrentMenuSelection(id);
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
                        items.add(new MenuItem(result.getId(), result.getName(), result.getCount(),
                                result.isSystemFolder()));
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
        public void onSubmitSuccess(ArrayList<FolderDetails> results) {

            ArrayList<MenuItem> items = new ArrayList<MenuItem>();
            for (FolderDetails result : results) {
                items.add(new MenuItem(result.getId(), result.getName(), result.getCount(), result
                        .isSystemFolder()));
            }

            String msg = "<b>" + entrySize + "</b> entries successfully added to ";
            if (results.size() == 1)
                msg += ("\"" + results.get(0).getName() + "\" collection.");
            else
                msg += (results.size() + " collections.");
            feedbackPanel.setSuccessMessage(msg);
        }

        @Override
        public void onSubmitFailure(String msg) {
            feedbackPanel.setFailureMessage("An error occurred while adding the entries.");
        }
    };

    // handler for moving entries
    class MoveEntryHandler extends SubmitHandler {

        private int entrySize;

        public MoveEntryHandler(RegistryServiceAsync service) {
            super(service);
        }

        @Override
        protected ArrayList<Long> getSource() { // TODO : can you even move from multiple folders
            ArrayList<Long> source = new ArrayList<Long>();
            if (currentFolderId <= 0)
                return source;

            try {
                source.add(currentFolderId);
            } catch (NumberFormatException nfe) {
                GWT.log(nfe.getMessage());
            }
            return source;
        }

        @Override
        protected ArrayList<FolderDetails> getDestination() {
            ArrayList<FolderDetails> list = new ArrayList<FolderDetails>();
            list.addAll(subMenu.getCollectionMenu().getMoveToDestination());
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
        protected void onSubmitSuccess(ArrayList<FolderDetails> results) {
            ArrayList<MenuItem> items = new ArrayList<MenuItem>();
            for (FolderDetails result : results) {
                items.add(new MenuItem(result.getId(), result.getName(), result.getCount(), result
                        .isSystemFolder()));
            }

            String msg = "<b>" + entrySize + "</b> entries successfully moved to ";
            if (results.size() == 1)
                msg += ("\"" + results.get(0).getName() + "\" collection.");
            else
                msg += (results.size() + " collections.");
            feedbackPanel.setSuccessMessage(msg);
        }

        @Override
        protected void onSubmitFailure(String msg) {
            feedbackPanel.setFailureMessage("An error occurred while moving entries.");
        }
    }
}
