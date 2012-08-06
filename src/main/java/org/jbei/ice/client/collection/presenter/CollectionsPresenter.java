package org.jbei.ice.client.collection.presenter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.event.FolderEvent;
import org.jbei.ice.client.collection.event.FolderEventHandler;
import org.jbei.ice.client.collection.event.FolderRetrieveEvent;
import org.jbei.ice.client.collection.event.FolderRetrieveEventHandler;
import org.jbei.ice.client.collection.menu.ExportAsOption;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.collection.model.CollectionsModel;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.client.common.table.EntrySelectionModel;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.entry.view.EntryPresenter;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.event.ShowEntryListEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.search.advanced.AdvancedSearchPresenter;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class CollectionsPresenter extends AbstractPresenter {

    private enum Mode {
        SEARCH, COLLECTION, ENTRY;
    }

    private final ICollectionView display;

    private EntryDataViewDataProvider entryDataProvider;
    private final CollectionDataTable collectionsDataTable;

    //  data providers for the sub menu
    private final ListDataProvider<FolderDetails> userListProvider;
    private final ListDataProvider<FolderDetails> systemListProvider;

    // selection menu
    private final CollectionsModel model;
    private AdvancedSearchPresenter searchPresenter;
    private EntryPresenter entryViewPresenter;
    private FolderDetails currentFolder;
    private Mode mode = Mode.COLLECTION;
    private EntryContext currentContext; // this can sometimes be null
    private final DeleteItemHandler deleteHandler;

    // handlers 
    private HandlerRegistration searchHandler;

    public CollectionsPresenter(CollectionsModel model, final ICollectionView display,
            ArrayList<SearchFilterInfo> operands) {
        this(model, display);
        search(operands);
    }

    // collections for entry view
    public CollectionsPresenter(CollectionsModel model, final ICollectionView view,
            EntryContext event) {
        this(model, view);
        this.showEntryView(event);
    }

    public CollectionsPresenter(final CollectionsModel model, final ICollectionView display) {
        this.display = display;
        this.model = model;
        this.deleteHandler = new DeleteItemHandler(model.getService(), model.getEventBus(), display);

        // initialize all parameters
        this.collectionsDataTable = new CollectionDataTable(new EntryTablePager()) {

            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(entryDataProvider);
                        model.getEventBus().fireEvent(event);
                    }
                };
            }
        };
        this.userListProvider = new ListDataProvider<FolderDetails>(new FolderDetailsKeyProvider());
        this.systemListProvider = new ListDataProvider<FolderDetails>(
                new FolderDetailsKeyProvider());
        this.entryDataProvider = new EntryDataViewDataProvider(collectionsDataTable,
                                                               model.getService());

        // selection models used for menus
        initMenus();

        initCollectionTableSelectionHandler();

        // handler for exporting
        initExportAsHandler();

        // init text box
        initCreateCollectionHandlers();

        // create entry handler
        final SingleSelectionModel<EntryAddType> selectionModel = display
                .getAddEntrySelectionHandler();
        CreateNewEntrySelectionHandler handler = new CreateNewEntrySelectionHandler(this,
                                                                                    model.getService(),
                                                                                    model.getEventBus(), display,
                                                                                    selectionModel);
        selectionModel.addSelectionChangeHandler(handler);

        if (searchHandler != null)
            searchHandler.removeHandler();

        searchHandler = model.getEventBus().addHandler(SearchEvent.TYPE, new SearchEventHandler() {

            @Override
            public void onSearch(SearchEvent event) {
                search(event.getFilters());
            }
        });

        // show entry context
        model.getEventBus().addHandler(ShowEntryListEvent.TYPE, new ShowEntryListEventHandler() {

            @Override
            public void onEntryListContextAvailable(ShowEntryListEvent event) {
                EntryContext context = event.getContext();
                if (context == null)
                    return;

                handleContext(context);
            }
        });

        // register for entry view events
        model.getEventBus().addHandler(EntryViewEvent.TYPE, new EntryViewEventHandler() {

            @Override
            public void onEntryView(EntryViewEvent event) {
                showEntryView(event.getContext());
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
        AddToHandler addHandler = new AddToHandler(display, new HasEntry(), model,
                                                   this.collectionsDataTable);
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
    }

    private void initCollectionTableSelectionHandler() {
        final EntrySelectionModel<EntryInfo> selectionModel = this.collectionsDataTable
                .getSelectionModel();
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

    public CollectionsPresenter(CollectionsModel model, final ICollectionView display, String param) {

        // collection sub menu
        this(model, display);
        long id = 0;
        try {
            if (param != null)
                id = Long.decode(param);
        } catch (NumberFormatException nfe) {
            id = 0;
        }

        retrieveEntriesForFolder(id, null);
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
                        selected.add(currentContext.getCurrent());
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

                switch (option) {
                    case XML:
                        Window.Location.replace("/export?type=xml&entries=" + builder.toString());
                        break;

                    case EXCEL:
                        Window.Location.replace("/export?type=excel&entries=" + builder.toString());
                        break;

                    default:
                        Window.alert("Not supported yet");
                }

                // clear selected
                display.getExportAsModel().setSelected(option, false);
            }
        });
    }

    public void showEntryView(EntryContext event) {
        if (entryViewPresenter == null) {
            entryViewPresenter = new EntryPresenter(model.getService(), model.getEventBus(), event);
            entryViewPresenter.setDeleteHandler(new DeleteEntryHandler());
        }
        mode = Mode.ENTRY;
        currentContext = event;
        History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + event.getCurrent(), false);
        entryViewPresenter.setCurrentContext(event);
        display.enableExportAs(true);
        display.setMainContent(entryViewPresenter.getView());
        boolean enable;
        if (currentFolder != null)
            enable = !currentFolder.isSystemFolder();
        else
            enable = false;

        display.setSubMenuEnable(true, enable, enable);
    }

    private void search(ArrayList<SearchFilterInfo> operands) {
        if (operands == null)
            return;

        if (searchPresenter == null) {
            searchPresenter = new AdvancedSearchPresenter(model.getService(), model.getEventBus());
            searchPresenter.addTableSelectionModelChangeHandler(new Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    boolean enable = (searchPresenter.getResultSelectedSet().size() > 0);
                    display.setSubMenuEnable(enable, false, false);
                    display.enableExportAs(enable);
                }
            });
        }

        display.setMainContent(searchPresenter.getView());
        searchPresenter.search(operands);
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
                    display.setMainContent(searchPresenter.getView());
                break;
        }
    }

    private void initCreateCollectionHandlers() {
        this.display.setQuickAddVisibility(false);

        this.display.addQuickAddBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if (display.getQuickEditVisibility() == false)
                    return;

                display.setQuickAddVisibility(false);
                saveCollection(display.getCollectionInputValue());
                display.hideQuickAddInput();
            }
        });

        this.display.addQuickAddKeyHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
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
                handle();
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
        editFolder.setCount(BigInteger.valueOf(item.getCount()));
        editFolder.setName(newName);
        editFolder.setId(item.getId());

        model.updateFolder(item.getId(), editFolder, new FolderEventHandler() {

            @Override
            public void onFolderEvent(FolderEvent event) {
                FolderDetails folder = event.getFolder();
                if (folder == null) {
                    display.showFeedbackMessage("Error updating collection. Please try again",
                                                false);
                    return;
                }

                display.updateSubMenuFolder(new OptionSelect(folder.getId(), folder.getName()));
                MenuItem resultItem = new MenuItem(folder.getId(), folder.getName(), folder
                        .getCount().longValue(), folder.isSystemFolder());
                display.setMenuItem(resultItem, deleteHandler);
            }
        });
    }

    private void saveCollection(String value) {
        if (value == null || value.isEmpty())
            return;

        model.createFolder(value, new FolderEventHandler() {

            @Override
            public void onFolderEvent(FolderEvent event) {
                if (event == null || event.getFolder() == null) {
                    display.showFeedbackMessage("Error creating new folder. Please try again", true);
                    return;
                }

                FolderDetails folder = event.getFolder();

                userListProvider.getList().add(folder);
                display.addSubMenuFolder(new OptionSelect(folder.getId(), folder.getName()));
                MenuItem newItem = new MenuItem(folder.getId(), folder.getName(), folder.getCount()
                                                                                        .longValue(),
                                                folder.isSystemFolder());

                if (!folder.isSystemFolder())
                    display.addMenuItem(newItem, deleteHandler);
                else
                    display.addMenuItem(newItem, null);
            }
        });
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

        // retrieve folders to use as menu
        model.retrieveFolders(new CollectionRetrieveHandler());
    }

    private void retrieveEntriesForFolder(final long id, final String msg) {
        display.setCurrentMenuSelection(id);
        entryDataProvider.updateRowCount(0, false);
        display.setDataView(collectionsDataTable);
        display.enableExportAs(false);
        display.setSubMenuEnable(false, false, false);

        model.retrieveEntriesForFolder(id, new FolderRetrieveEventHandler() {

            @Override
            public void onFolderRetrieve(FolderRetrieveEvent event) {
                if (event == null || event.getItems() == null) {
                    display.showFeedbackMessage("Error connecting to server. Please try again",
                                                true);
                    entryDataProvider.setValues(null);
                    return;
                }

                FolderDetails folder = event.getItems().get(0);
                if (folder == null) {
                    display.showFeedbackMessage("Could not retrieve collection with id " + id, true);
                    entryDataProvider.setValues(null);
                    return;
                }

                collectionsDataTable.clearSelection();
                History.newItem(Page.COLLECTIONS.getLink() + ";id=" + folder.getId(), false);
                display.setCurrentMenuSelection(folder.getId());
                ArrayList<Long> entries = folder.getContents();
                entryDataProvider.setValues(entries);

                currentFolder = folder;
                mode = Mode.COLLECTION;
                if (msg != null && !msg.isEmpty())
                    display.showFeedbackMessage(msg, false);
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    private class CollectionRetrieveHandler implements FolderRetrieveEventHandler {

        @Override
        public void onFolderRetrieve(FolderRetrieveEvent event) {
            ArrayList<FolderDetails> folders = event.getItems();

            ArrayList<MenuItem> userMenuItems = new ArrayList<MenuItem>();
            ArrayList<FolderDetails> userFolders = new ArrayList<FolderDetails>();

            ArrayList<MenuItem> systemMenuItems = new ArrayList<MenuItem>();
            ArrayList<FolderDetails> systemFolder = new ArrayList<FolderDetails>();

            for (FolderDetails folder : folders) {
                MenuItem item = new MenuItem(folder.getId(), folder.getName(), folder.getCount()
                                                                                     .longValue(),
                                             folder.isSystemFolder());

                if (folder.isSystemFolder()) {
                    systemMenuItems.add(item);
                    systemFolder.add(folder);
                } else {
                    userMenuItems.add(item);
                    userFolders.add(folder);
                    display.addSubMenuFolder(new OptionSelect(folder.getId(), folder.getName()));
                }
            }

            // my entries
            MenuItem item = new MenuItem(0, "My Entries",
                                         AppController.accountInfo.getUserEntryCount(), true);
            userMenuItems.add(0, item);
            MenuItem allEntriesItem = new MenuItem(-1, "Available Entries",
                                                   AppController.accountInfo.getVisibleEntryCount(), true);
            systemMenuItems.add(0, allEntriesItem);
            display.setSystemCollectionMenuItems(systemMenuItems);
            DeleteItemHandler deleteHandler = new DeleteItemHandler(model.getService(), model.getEventBus(), display);
            display.setUserCollectionMenuItems(userMenuItems, deleteHandler);

            userListProvider.getList().addAll(userFolders);
            systemListProvider.getList().addAll(systemFolder);
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
                        return entryDataProvider.getData();
                    } else {
                        return collectionsDataTable.getSelectedEntrySet();
                    }

                case SEARCH:
                    return searchPresenter.getEntrySet();

                case ENTRY:
                    HashSet<Long> set = new HashSet<Long>();
                    if (currentContext != null)
                        set.add(currentContext.getCurrent());
                    return set;
            }
        }
    }

    private class RemoveHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {

            final ArrayList<Long> ids = new ArrayList<Long>(new HasEntry().getSelectedEntrySet());
            if (ids.isEmpty())
                return;

            model.removeEntriesFromFolder(currentFolder.getId(), ids,
                                          new FolderRetrieveEventHandler() {

                                              @Override
                                              public void onFolderRetrieve(FolderRetrieveEvent event) {
                                                  if (event == null || event.getItems() == null) {
                                                      display.showFeedbackMessage(
                                                              "An error occured while removing entries. Please try " +
                                                                      "again.",
                                                              true);
                                                      return;
                                                  }

                                                  FolderDetails result = event.getItems().get(0);
                                                  if (result == null)
                                                      return;

                                                  ArrayList<MenuItem> items = new ArrayList<MenuItem>();
                                                  MenuItem updateItem = new MenuItem(result.getId(), result.getName(),
                                                                                     result
                                                                                             .getCount().longValue(),
                                                                                     result.isSystemFolder());
                                                  items.add(updateItem);
                                                  display.updateMenuItemCounts(items);

                                                  String entryDisp = (ids.size() == 1) ? "entry" : "entries";
                                                  String msg = "<b>" + ids.size() + "</b> " + entryDisp
                                                          + " successfully removed from";

                                                  String name = result.getName();
                                                  if (name.length() > 20)
                                                      msg += " collection.";
                                                  else
                                                      msg += ("\"<b>" + name + "</b>\" collection.");

                                                  retrieveEntriesForFolder(currentFolder.getId(), msg);
                                                  collectionsDataTable.clearSelection();
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
                    model.getService().deleteEntry(AppController.sessionId, toDelete, callback);
                }

                @Override
                public void onSuccess(ArrayList<FolderDetails> result) {
                    History.newItem(Page.COLLECTIONS.getLink() + ";id=" +
                                            currentFolder.getId());
                    ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                    FeedbackEvent event = new FeedbackEvent(false,
                                                            "Entry deleted " +
                                                                    "successfully");
                    model.getEventBus().fireEvent(event);

                    for (FolderDetails detail : result) {
                        MenuItem item = new MenuItem(detail.getId(),
                                                     detail.getName(), detail
                                .getCount().longValue(), detail.isSystemFolder());
                        menuItems.add(item);
                    }

                    if (currentFolder.getId() == 0) {
                        AppController.accountInfo.setUserEntryCount(AppController.accountInfo
                                                                                 .getUserEntryCount() - 1);
                        MenuItem myItems = new MenuItem(0, "My Entries",
                                                        AppController.accountInfo.getUserEntryCount(), true);
                        menuItems.add(myItems);
                    }

                    AppController.accountInfo.setVisibleEntryCount(AppController.accountInfo
                                                                                .getVisibleEntryCount() - 1);
                    MenuItem allEntriesItem = new MenuItem(-1, "Available Entries",
                                                           AppController.accountInfo.getVisibleEntryCount(), true);
                    menuItems.add(allEntriesItem);

                    display.updateMenuItemCounts(menuItems);
                }
            }.go(model.getEventBus());
        }
    }

    public ICollectionView getView() {
        return display;
    }
}
