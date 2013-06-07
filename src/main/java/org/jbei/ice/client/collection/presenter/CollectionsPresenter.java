package org.jbei.ice.client.collection.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jbei.ice.client.*;
import org.jbei.ice.client.collection.FolderEntryDataProvider;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.ShareCollectionData;
import org.jbei.ice.client.collection.menu.ExportAsOption;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.model.CollectionsModel;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.client.common.table.EntrySelectionModel;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.entry.view.EntryPresenter;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.event.ShowEntryListEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.search.advanced.ISearchView;
import org.jbei.ice.client.search.advanced.SearchPresenter;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.folder.FolderShareType;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CollectionsPresenter extends AbstractPresenter {

    private enum Mode {
        SEARCH, COLLECTION, ENTRY;
    }

    private ICollectionView display;

    private FolderEntryDataProvider folderDataProvider;
    private final CollectionDataTable collectionsDataTable;

    // selection menu
    private final CollectionsModel model;
    private SearchPresenter searchPresenter;
    private EntryPresenter entryViewPresenter;
    private FolderDetails currentFolder;
    private Mode mode = Mode.COLLECTION;
    private EntryContext currentContext; // this can sometimes be null
    private final DeleteItemHandler deleteHandler;
    private HandlerRegistration selectionRegistration;
    private HandlerRegistration showListRegistration;
    private HandlerRegistration showEntryRegistration;

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus, final ICollectionView display,
                                ISearchView searchView) {
        this(service, eventBus, display);
        search(searchView);
    }

    // collections for entry view
    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus, final ICollectionView view,
                                EntryContext event) {
        this(service, eventBus, view);
        this.showEntryView(event);
    }

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus, ICollectionView display,
                                String param) {
        // collection sub menu
        this(service, eventBus, display);
        long id = 0;
        try {
            if (param != null)
                id = Long.decode(param);
        } catch (NumberFormatException nfe) {
            id = 0;
        }

        retrieveEntriesForFolder(id, null);
    }

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus, final ICollectionView display) {
        super(service, eventBus);
        this.display = display;
        this.model = new CollectionsModel(service, eventBus);
        this.deleteHandler = new DeleteItemHandler(model.getService(), model.getEventBus(), display);

        // initialize all parameters
        this.collectionsDataTable = new CollectionDataTable(new EntryTablePager()) {

            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(folderDataProvider);
                        model.getEventBus().fireEvent(event);
                    }
                };
            }
        };

        this.folderDataProvider = new FolderEntryDataProvider(collectionsDataTable, model.getService());

        // selection models used for menus
        initMenus();

        initCollectionTableSelectionHandler();

        // handler for exporting
        initExportAsHandler();

        // init text box
        initCreateCollectionHandlers();

        // init entry handler
        initEntryViewHandler();

        // create entry handler
        final SingleSelectionModel<EntryAddType> selectionModel = display.getAddEntrySelectionHandler();
        if (selectionRegistration != null)
            selectionRegistration.removeHandler();

        selectionRegistration = selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (entryViewPresenter == null) {// TODO : when user navigates to another page and returns this is null
                    entryViewPresenter = new EntryPresenter(model.getService(), CollectionsPresenter.this,
                            model.getEventBus(), null);
                    entryViewPresenter.setDeleteHandler(new DeleteEntryHandler());
                }

                EntryAddType type = selectionModel.getSelectedObject();
                if (type == null)
                    return;
                selectionModel.setSelected(type, false);
                mode = Mode.ENTRY;
                display.setMainContent(entryViewPresenter.getView().asWidget());
                entryViewPresenter.showCreateEntry(type);
                display.setCurrentMenuSelection(0);
            }
        });

        // show entry context
        if (showListRegistration != null)
            showListRegistration.removeHandler();
        showListRegistration = model.getEventBus().addHandler(ShowEntryListEvent.TYPE, new ShowEntryListEventHandler() {

            @Override
            public void onEntryListContextAvailable(ShowEntryListEvent event) {
                EntryContext context = event.getContext();
                if (context == null)
                    return;

                handleContext(context);
            }
        });

        // handler for showing feedback messages
        model.getEventBus().addHandler(FeedbackEvent.TYPE,
                new FeedbackEvent.IFeedbackEventHandler() {

                    @Override
                    public void onFeedbackAvailable(FeedbackEvent event) {
                        display.showFeedbackMessage(event.getMessage(), event.isError());
                    }
                });

        // handler for "add to" sub menu
        AddToHandler addHandler = new AddToHandler(display, new HasEntry(), model, this.collectionsDataTable);
        display.addAddToSubmitHandler(addHandler);

        // move to handler
        MoveToHandler moveHandler = new MoveToHandler(model, display, new HasEntry()) {

            @Override
            protected long getSource() {
                return currentFolder.getId();
            }

            @Override
            protected void clearTableSelection() {
                collectionsDataTable.clearSelection();
            }

            @Override
            protected void retrieveFolderEntries(long folder, String msg) {
                retrieveEntriesForFolder(folder, msg);
            }
        };

        display.addMoveSubmitHandler(moveHandler);

        // remove handler
        display.addRemoveHandler(new RemoveHandler());

        display.addTransferHandler(new TransferHandler());

        // permission delegate for the menu (user)
        display.setPermissionDelegate(new PermissionDelegate());

        // retrieve web of registries settings
        if (ClientController.account.isAdmin()) {
            model.retrieveWebOfRegistrySettings(new Callback<HashMap<String, String>>() {

                @Override
                public void onSuccess(HashMap<String, String> result) {
                    String value = result.get(ConfigurationKey.WEB_PARTNERS.name());
                    if (value == null)
                        return;

                    ArrayList<OptionSelect> values = new ArrayList<OptionSelect>();
                    for (String split : value.split(";")) {
                        if (split.isEmpty())
                            continue;

                        OptionSelect select = new OptionSelect(0, split);
                        values.add(select);
                    }
                    display.setTransferOptions(values);

                    // check if it is web enabled and set transfer widget to visible
                }

                @Override
                public void onFailure() {
                }
            });
        }

        setPromotionDelegate();
        setDemotionDelegate();
    }

    private void setPromotionDelegate() {
        display.setPromotionDelegate(new ServiceDelegate<MenuItem>() {
            @Override
            public void execute(final MenuItem menuItem) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.promoteCollection(ClientController.sessionId, menuItem.getId(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        History.newItem(Page.COLLECTIONS.getLink());
                    }
                }.go(eventBus);
            }
        });
    }

    private void setDemotionDelegate() {
        display.setDemotionDelegate(new ServiceDelegate<MenuItem>() {
            @Override
            public void execute(final MenuItem menuItem) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.demoteCollection(ClientController.sessionId, menuItem.getId(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        History.newItem(Page.COLLECTIONS.getLink());
                    }
                }.go(eventBus);
            }
        });
    }

    private void initCollectionTableSelectionHandler() {
        final EntrySelectionModel<EntryInfo> selectionModel = this.collectionsDataTable.getSelectionModel();
        this.collectionsDataTable.getSelectionModel().addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                boolean enable = (selectionModel.getSelectedSet().size() > 0);
                display.enableExportAs(enable);

                // can user edit current folder?
                if (!currentFolder.isSystemFolder()) {
                    display.setSubMenuEnable(enable, enable, enable);
                } else {
                    display.setSubMenuEnable(enable, false, false);
                }
            }
        });
    }

    private void initExportAsHandler() {
        display.getExportAsModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                StringBuilder builder = new StringBuilder();
                Set<Long> selected = new HashSet<Long>();
                ExportAsOption option = display.getExportAsModel().getSelectedObject();
                if (option == null)
                    return;

                switch (mode) {
                    case COLLECTION:
                        selected = collectionsDataTable.getSelectedEntrySet();
                        break;

                    case ENTRY:
                        selected.add(currentContext.getId());
                        break;

                    case SEARCH:
                        selected = searchPresenter.getEntrySet();
                        break;
                }

                if (selected == null || selected.isEmpty()) {
                    display.getExportAsModel().setSelected(option, false);
                    return;
                }

                for (long id : selected) {
                    builder.append(id + ", ");
                }

                Window.Location.replace("/export?type=" + option.name() + "&entries=" + builder.toString());

                // clear selected
                display.getExportAsModel().setSelected(option, false);
            }
        });
    }

    public void showEntryView(EntryContext event) {
        if (entryViewPresenter == null) {
            entryViewPresenter = new EntryPresenter(model.getService(), CollectionsPresenter.this,
                    model.getEventBus(), event);
            entryViewPresenter.setDeleteHandler(new DeleteEntryHandler());
        } else {
            entryViewPresenter.setCurrentContext(event);
            entryViewPresenter.showCurrentEntryView();
        }

        mode = Mode.ENTRY;
        currentContext = event;
        if (event.getPartnerUrl() == null)
            History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + event.getId(), false);
        display.enableExportAs(true);
        display.setMainContent(entryViewPresenter.getView().asWidget());
        boolean enable;
        if (currentFolder != null)
            enable = !currentFolder.isSystemFolder();
        else
            enable = false;

        display.setSubMenuEnable(true, enable, enable);
    }

    protected void search(ISearchView searchView) {
        if (searchPresenter == null) {
            searchPresenter = new SearchPresenter(model.getService(), model.getEventBus(), searchView);
            searchPresenter.addTableSelectionModelChangeHandler(new Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    boolean enable = (searchPresenter.getResultSelectedSet().size() > 0);
                    display.setSubMenuEnable(enable, false, false);
                    if (ClientController.account.isAdmin())
                        display.enableExportAs(enable);
                }
            });
        }

        search();
    }

    public void search() {
        display.setMainContent(searchPresenter.getView().asWidget());
        searchPresenter.search();
        mode = Mode.SEARCH;
    }

    private void handleContext(EntryContext context) {
        this.currentContext = context;

        switch (context.getType()) {
            case COLLECTION:
            case SAMPLES:
            default:
                mode = Mode.COLLECTION;
                display.setDataView(collectionsDataTable);
                break;

            case SEARCH:
                mode = Mode.SEARCH;
                if (searchPresenter != null)
                    display.setMainContent(searchPresenter.getView().asWidget());
                break;
        }
    }

    private void initCreateCollectionHandlers() {
        this.display.addQuickAddKeyHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                saveCollection(display.getCollectionInputValue());
                display.hideQuickAddInput();
            }
        });

        display.addQuickEditKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                handle();
            }
        });
    }

    private void initEntryViewHandler() {
        if (showEntryRegistration != null)
            showEntryRegistration.removeHandler();
        showEntryRegistration = this.eventBus.addHandler(
                EntryViewEvent.TYPE,
                new EntryViewEvent.EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        if (event == null || event.getContext() == null)
                            return;

                        showEntryView(event.getContext());
                    }
                });
    }

    private void handle() {
        if (!display.getQuickEditVisibility())
            return;

        String newName = display.getQuickEditInput();
        final MenuItem item = display.getCurrentMenuEditSelection();
        if (newName.trim().equals(item.getName().trim())) {
            display.setMenuItem(item, deleteHandler);
            return;
        }

        // RPC with newName
        FolderDetails editFolder = new FolderDetails();
        editFolder.setCount(item.getCount());
        editFolder.setName(newName);
        editFolder.setId(item.getId());

        model.updateFolder(item.getId(), editFolder, new Callback<FolderDetails>() {

            @Override
            public void onSuccess(FolderDetails folder) {
                if (folder == null) {
                    display.showFeedbackMessage("Error updating collection. Please try again", true);
                    return;
                }

                display.updateSubMenuFolder(new OptionSelect(folder.getId(), folder.getName()));
                MenuItem resultItem = new MenuItem(folder.getId(), folder.getName(), folder.getCount());
                display.setMenuItem(resultItem, deleteHandler);
            }

            @Override
            public void onFailure() {
                display.showFeedbackMessage("Error updating collection. Please try again", true);
            }
        });
    }

    private void saveCollection(String value) {
        if (value == null || value.isEmpty())
            return;

        model.createFolder(value, new Callback<FolderDetails>() {

            @Override
            public void onSuccess(FolderDetails folder) {
                display.addSubMenuFolder(new OptionSelect(folder.getId(), folder.getName()));
                MenuItem newItem = new MenuItem(folder.getId(), folder.getName(), folder.getCount());

                if (!folder.isSystemFolder())
                    display.addMenuItem(newItem, deleteHandler);
                else
                    display.addMenuItem(newItem, null);
            }

            @Override
            public void onFailure() {
                display.showFeedbackMessage("Error creating new folder.", true);
            }
        });
    }

    /**
     * Initializes the selection models used for the menu items
     * by adding the selection change handlers
     */
    private void initMenus() {
        final SingleSelectionModel<MenuItem> userModel = display.getMenuModel(FolderShareType.PRIVATE);
        final SingleSelectionModel<MenuItem> systemModel = display.getMenuModel(FolderShareType.PUBLIC);
        final SingleSelectionModel<MenuItem> sharedModel = display.getMenuModel(FolderShareType.SHARED);

        userModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MenuItem selection = userModel.getSelectedObject();
                if (selection == null)
                    return;

                retrieveEntriesForFolder(selection.getId(), null);
            }
        });

        systemModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MenuItem selection = systemModel.getSelectedObject();
                if (selection == null)
                    return;

                retrieveEntriesForFolder(selection.getId(), null);
            }
        });

        sharedModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MenuItem selection = sharedModel.getSelectedObject();
                if (selection == null)
                    return;

                retrieveEntriesForFolder(selection.getId(), null);
            }
        });

        // retrieve folders to use as menu
        model.retrieveFolders(new CollectionRetrieveHandler());
    }

    public void retrieveEntriesForFolder(final long id, final String msg) {
        display.setCurrentMenuSelection(id);
        folderDataProvider.updateRowCount(0, false);
        display.setDataView(collectionsDataTable);
        display.enableExportAs(false);
        display.setSubMenuEnable(false, false, false);
        int limit = collectionsDataTable.getVisibleRange().getLength();

        model.retrieveEntriesForFolder(id, new Callback<FolderDetails>() {

            @Override
            public void onSuccess(FolderDetails folder) {
                if (folder == null) {
                    History.newItem(Page.COLLECTIONS.getLink() + ";id=0");
                    return;
                }

                collectionsDataTable.clearSelection();
                History.newItem(Page.COLLECTIONS.getLink() + ";id=" + folder.getId(), false);
                display.setCurrentMenuSelection(folder.getId());
                folderDataProvider.setFolderData(folder, true);

                currentFolder = folder;
                mode = Mode.COLLECTION;
                if (msg != null && !msg.isEmpty())
                    display.showFeedbackMessage(msg, false);
            }

            @Override
            public void onFailure() {
                display.showFeedbackMessage("Error retrieving folder entries", true);
            }
        }, 0, limit);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    private class CollectionRetrieveHandler extends Callback<ArrayList<FolderDetails>> {

        @Override
        public void onSuccess(ArrayList<FolderDetails> folders) {
            ArrayList<MenuItem> userMenuItems = new ArrayList<MenuItem>();
            ArrayList<MenuItem> systemMenuItems = new ArrayList<MenuItem>();
            ArrayList<MenuItem> sharedMenuItems = new ArrayList<MenuItem>();

            for (FolderDetails folder : folders) {
                MenuItem item = new MenuItem(folder.getId(), folder.getName(), folder.getCount());
                item.setShareType(folder.getShareType());

                switch (folder.getShareType()) {
                    case PUBLIC:
                        systemMenuItems.add(item);
                        if (ClientController.account.isAdmin())
                            item.setPermissions(folder.getPermissions());
                        break;

                    case PRIVATE:
                        item.setPermissions(folder.getPermissions());
                        userMenuItems.add(item);
                        display.addSubMenuFolder(new OptionSelect(folder.getId(), folder.getName()));
                        break;

                    case SHARED:
                        item.setOwner(folder.getOwner());
                        sharedMenuItems.add(item);
                        break;
                }
            }

            // my entries
            MenuItem item = new MenuItem(0, "My Entries", ClientController.account.getUserEntryCount());
            userMenuItems.add(0, item);
            MenuItem allEntriesItem = new MenuItem(-1, "Available Entries",
                    ClientController.account.getVisibleEntryCount());
            systemMenuItems.add(0, allEntriesItem);
            display.setSystemCollectionMenuItems(systemMenuItems);
            DeleteItemHandler deleteHandler = new DeleteItemHandler(model.getService(), model.getEventBus(), display);
            display.setUserCollectionMenuItems(userMenuItems, deleteHandler);
            display.setSharedCollectionsMenuItems(sharedMenuItems);

            if (currentFolder != null)
                display.setCurrentMenuSelection(currentFolder.getId());
        }

        @Override
        public void onFailure() {
        }
    }

    //inner classes
    private class HasEntry implements IHasEntryId {

        @Override
        public Set<Long> getSelectedEntrySet() {
            switch (mode) {
                case COLLECTION:
                default:
                    if (collectionsDataTable.getSelectionModel().isAllSelected()) {
                        return null;
//                        return folderDataProvider.getData();
                    } else {
                        return collectionsDataTable.getSelectedEntrySet();
                    }

                case SEARCH:
                    return searchPresenter.getEntrySet();

                case ENTRY:
                    HashSet<Long> set = new HashSet<Long>();
                    if (currentContext != null)
                        set.add(currentContext.getId());
                    return set;
            }
        }
    }

    private class TransferHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final ArrayList<Long> ids = new ArrayList<Long>(new HasEntry().getSelectedEntrySet());
            if (ids.isEmpty())
                return;

            ArrayList<OptionSelect> selectedTransfer = display.getSelectedTransfers();
            model.requestTransfer(ids, selectedTransfer);
            retrieveEntriesForFolder(currentFolder.getId(), "Transfer requested for " + ids.size() + " entries");
        }
    }

    private class RemoveHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final ArrayList<Long> ids = new ArrayList<Long>(new HasEntry().getSelectedEntrySet());
            if (ids.isEmpty())
                return;

            model.removeEntriesFromFolder(
                    currentFolder.getId(), ids,
                    new Callback<FolderDetails>() {
                        @Override
                        public void onSuccess(FolderDetails result) {
                            ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                            MenuItem updateItem = new MenuItem(result.getId(), result.getName(), result.getCount()
                            );
                            items.add(updateItem);
                            display.updateMenuItemCounts(items);

                            String entryDisp = (ids.size() == 1) ? "entry" : "entries";
                            String msg = "<b>" + ids.size() + "</b> " + entryDisp + " successfully removed from";
                            String name = result.getName();
                            if (name.length() > 20)
                                msg += " collection.";
                            else
                                msg += ("\"<b>" + name + "</b>\" collection.");

                            retrieveEntriesForFolder(currentFolder.getId(), msg);
                            collectionsDataTable.clearSelection();
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
        }
    }

    public class DeleteEntryHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final EntryInfo toDelete = entryViewPresenter.getCurrentInfo();
            if (toDelete == null)
                return;

            new IceAsyncCallback<ArrayList<FolderDetails>>() {

                @Override
                protected void callService(AsyncCallback<ArrayList<FolderDetails>> callback)
                        throws AuthenticationException {
                    model.getService().deleteEntry(ClientController.sessionId, toDelete, callback);
                }

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    History.newItem(Page.COLLECTIONS.getLink() + ";id=" + currentFolder.getId());
                    ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                    FeedbackEvent event = new FeedbackEvent(false, "Entry deleted successfully");
                    model.getEventBus().fireEvent(event);

                    for (FolderDetails detail : result) {
                        MenuItem item = new MenuItem(detail.getId(), detail.getName(), detail.getCount());
                        menuItems.add(item);
                    }

                    if (currentFolder.getId() == 0) {
                        ClientController.account.setUserEntryCount(ClientController.account.getUserEntryCount() - 1);
                        MenuItem myItems = new MenuItem(0, "My Entries",
                                ClientController.account.getUserEntryCount());
                        menuItems.add(myItems);
                    }

                    ClientController.account.setVisibleEntryCount(ClientController.account.getVisibleEntryCount() - 1);
                    MenuItem allEntriesItem = new MenuItem(-1, "Available Entries",
                            ClientController.account.getVisibleEntryCount());
                    menuItems.add(allEntriesItem);

                    display.updateMenuItemCounts(menuItems);
                }
            }.go(model.getEventBus());
        }
    }

    private class PermissionDelegate implements Delegate<ShareCollectionData> {

        @Override
        public void execute(final ShareCollectionData data) {
            IceAsyncCallback<Boolean> asyncCallback;

            if (data.isDelete()) {
                asyncCallback = new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        model.getService().removePermission(ClientController.sessionId, data.getInfo(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        data.getInfoCallback().onSuccess(data.getInfo());
                    }
                };
            } else {
                if (data.getInfo().isCanWrite()) {
                    data.getInfo().setType(PermissionInfo.Type.WRITE_FOLDER);
                } else if (data.getInfo().isCanWrite()) {
                    data.getInfo().setType(PermissionInfo.Type.READ_FOLDER);
                }

                asyncCallback = new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        model.getService().addPermission(ClientController.sessionId, data.getInfo(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        data.getInfoCallback().onSuccess(data.getInfo());
                    }
                };
            }
            asyncCallback.go(model.getEventBus());
        }
    }

    public ICollectionView getView() {
        return display;
    }
}
