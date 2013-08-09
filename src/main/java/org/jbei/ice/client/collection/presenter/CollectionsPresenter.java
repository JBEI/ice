package org.jbei.ice.client.collection.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
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
import org.jbei.ice.client.entry.display.EntryPresenter;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.event.ShowEntryListEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.search.advanced.ISearchView;
import org.jbei.ice.client.search.advanced.SearchPresenter;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;
import org.jbei.ice.lib.shared.dto.folder.FolderType;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

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

public class CollectionsPresenter extends AbstractPresenter {

    private enum Mode {
        SEARCH, COLLECTION, ENTRY
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

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus, final ICollectionView display,
            ISearchView searchView, SearchQuery query) {
        this(service, eventBus, display);
        search(searchView, query);
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

    private ServiceDelegate<PartData> createDelegate() {
        return new ServiceDelegate<PartData>() {
            @Override
            public void execute(PartData entryInfo) {
                EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                context.setNav(folderDataProvider);
                context.setId(entryInfo.getId());
                context.setRecordId(entryInfo.getRecordId());
                showEntryView(context);
            }
        };
    }

    private ServiceDelegate<EntryContext> createSearchDelegate() {
        return new ServiceDelegate<EntryContext>() {
            @Override
            public void execute(EntryContext context) {
                showEntryView(context);
            }
        };
    }

    public CollectionsPresenter(RegistryServiceAsync service, HandlerManager eventBus, final ICollectionView display) {
        super(service, eventBus);
        this.display = display;
        this.model = new CollectionsModel(service, eventBus);
        this.deleteHandler = new DeleteItemHandler(model.getService(), model.getEventBus(), display);

        // initialize all parameters
        this.collectionsDataTable = new CollectionDataTable(createDelegate());

        this.folderDataProvider = new FolderEntryDataProvider(collectionsDataTable, model.getService());

        // selection models used for menus
        initMenus();

        initCollectionTableSelectionHandler();

        // handler for exporting
        initExportAsHandler();

        // init text box
        initCreateCollectionHandlers();

        // create entry handler
        final SingleSelectionModel<EntryAddType> selectionModel = display.getAddEntrySelectionHandler();
        if (selectionRegistration != null)
            selectionRegistration.removeHandler();

        selectionRegistration = selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                EntryAddType type = selectionModel.getSelectedObject();
                if (type == null)
                    return;

                if (entryViewPresenter == null) {
                    entryViewPresenter = new EntryPresenter(model.getService(), CollectionsPresenter.this,
                                                            model.getEventBus(), null);
                    entryViewPresenter.setDeleteHandler(new DeleteEntryHandler());
                    // TODO : sequence panel is null
                }

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

        // retrieve web of registries settings to set the transfer widget options (admin only)
        setTransferWidgetOptions();

        setPromotionDelegate();
        setDemotionDelegate();
    }

    private void setTransferWidgetOptions() {
        if (ClientController.account.isAdmin()) {
            model.retrieveWebOfRegistryPartners(new Callback<WebOfRegistries>() {

                @Override
                public void onSuccess(WebOfRegistries result) {
                    if (result == null)
                        return;

                    ArrayList<OptionSelect> values = new ArrayList<OptionSelect>();
                    for (RegistryPartner partner : result.getPartners()) {
                        OptionSelect select = new OptionSelect(partner.getId(), partner.getUrl());
                        values.add(select);
                    }

                    display.setTransferOptions(values);
                }

                @Override
                public void onFailure() {
                }
            });
        }
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
        final EntrySelectionModel<PartData> selectionModel = this.collectionsDataTable.getSelectionModel();
        this.collectionsDataTable.getSelectionModel().addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                boolean hasSelection = (selectionModel.getSelectedSet().size() > 0);
                display.enableExportAs(hasSelection);

                boolean canRemove = currentFolder.getOwner() != null
                        && ClientController.account.getEmail().equals(currentFolder.getOwner().getEmail());
                if (!canRemove && currentFolder.getAccessPermissions() != null) {
                    for (AccessPermission accessPermission : currentFolder.getAccessPermissions()) {
                        // if you can see the folder then it has been shared with you so we only need to check access
                        if (accessPermission.isCanWrite()) {
                            canRemove = true;
                            break;
                        }
                    }
                }

                canRemove = (canRemove && hasSelection);
                display.setCanMove(canRemove);
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
                    builder.append(id).append(", ");
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
        if (event.getPartnerUrl() == null || event.getPartnerUrl().trim().isEmpty())
            History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + event.getId(), false);
        display.enableExportAs(true);
        display.setMainContent(entryViewPresenter.getView().asWidget());
        boolean enable = false;
        if (currentFolder != null && currentFolder.getAccessPermissions() != null) {
            for (AccessPermission accessPermission : currentFolder.getAccessPermissions()) {
                if (accessPermission.isCanWrite()) {
                    enable = true;
                    break;
                }
            }
        }

        display.setCanMove(enable);
    }

    protected void search(ISearchView searchView, SearchQuery query) {
        if (searchPresenter == null) {
            searchPresenter = new SearchPresenter(model.getService(), model.getEventBus(), searchView,
                                                  createSearchDelegate());
            searchPresenter.addTableSelectionModelChangeHandler(new Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    boolean enable = (searchPresenter.getResultSelectedSet().size() > 0);
                    display.setCanMove(false);
                    if (ClientController.account.isAdmin())
                        display.enableExportAs(enable);
                }
            });
        }

        search(query);
    }

    public void search(SearchQuery query) {
        display.setMainContent(searchPresenter.getView().asWidget());
        searchPresenter.search(query);
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

        display.addQuickAddHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveCollection(display.getCollectionInputValue());
                display.hideQuickAddInput();
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
                    display.showFeedbackMessage("Error updating collection.", true);
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

                if (folder.getType() == FolderType.PRIVATE)
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
        final SingleSelectionModel<MenuItem> userModel = display.getMenuModel(FolderType.PRIVATE);
        final SingleSelectionModel<MenuItem> systemModel = display.getMenuModel(FolderType.PUBLIC);
        final SingleSelectionModel<MenuItem> sharedModel = display.getMenuModel(FolderType.SHARED);

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
        display.setCanMove(false);
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

            Collections.sort(folders, new Comparator<FolderDetails>() {
                @Override
                public int compare(FolderDetails o1, FolderDetails o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (FolderDetails folder : folders) {
                MenuItem item = new MenuItem(folder.getId(), folder.getName(), folder.getCount());
                item.setType(folder.getType());

                switch (folder.getType()) {
                    case PUBLIC:
                        systemMenuItems.add(item);
                        if (ClientController.account.isAdmin())
                            item.setAccessPermissions(folder.getAccessPermissions());
                        break;

                    case PRIVATE:
                        item.setAccessPermissions(folder.getAccessPermissions());
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
                    return collectionsDataTable.getSelectedEntrySet();

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
                            MenuItem updateItem = new MenuItem(result.getId(), result.getName(), result.getCount());
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
            final PartData toDelete = entryViewPresenter.getCurrentInfo();
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
                        MenuItem myItems = new MenuItem(0, "My Entries", ClientController.account.getUserEntryCount());
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
                        model.getService().removePermission(ClientController.sessionId, data.getAccess(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        data.getInfoCallback().onSuccess(data);
                    }
                };
            } else {
                asyncCallback = new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        model.getService().addPermission(ClientController.sessionId, data.getAccess(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        data.getInfoCallback().onSuccess(data);
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
