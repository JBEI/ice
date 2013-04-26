package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEventHandler;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEventHandler;
import org.jbei.ice.client.bulkupload.model.BulkUploadModel;
import org.jbei.ice.client.bulkupload.model.NewBulkInput;
import org.jbei.ice.client.bulkupload.sheet.Sheet;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.user.PreferenceKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Presenter for the bulk import page
 *
 * @author Hector Plahar
 */
public class BulkUploadPresenter extends AbstractPresenter {

    private final IBulkUploadView view;
    private final HashMap<EntryAddType, NewBulkInput> sheetCache;
    private final BulkUploadModel model;
    private NewBulkInput currentInput;
    private final ArrayList<BulkUploadMenuItem> savedDrafts = new ArrayList<BulkUploadMenuItem>();
    private ServiceDelegate<BulkUploadAutoUpdate> autoUpdateDelegate;
    private ServiceDelegate<PreferenceInfo> updatePreferenceDelegate;
    private ServiceDelegate<Set<OptionSelect>> updatePermissionDelegate;

    public BulkUploadPresenter(RegistryServiceAsync service, HandlerManager eventBus, final IBulkUploadView display) {
        super(service, eventBus);
        this.view = display;
        this.model = new BulkUploadModel(service, eventBus);
        sheetCache = new HashMap<EntryAddType, NewBulkInput>();

        setClickHandlers();

        // selection model handlers
        setMenuSelectionModel();
        setCreateBulkUploadSelectionModel();

        // toggle menu
        addToggleMenuHandler();

        // retrieveData
        retrieveSavedDrafts();
        retrievePendingIfAdmin();
        retrieveGroups();

        model.getEventBus().addHandler(
                FeedbackEvent.TYPE,
                new FeedbackEvent.IFeedbackEventHandler() {
                    @Override
                    public void onFeedbackAvailable(FeedbackEvent event) {
                        display.showFeedback(event.getMessage(), event.isError());
                    }
                });

        enableAutoUpdate();
        createPreferenceDelegate();
        createPermissionDelegate();

        view.setPermissionDelegate(updatePermissionDelegate);
    }

    /**
     * Initializes delegate for updating bulk upload permissions
     */
    protected void createPermissionDelegate() {
        updatePermissionDelegate = new ServiceDelegate<Set<OptionSelect>>() {
            @Override
            public void execute(final Set<OptionSelect> selected) {
                new IceAsyncCallback<Long>() {

                    @Override
                    protected void callService(AsyncCallback<Long> callback) throws AuthenticationException {
                        ArrayList<PermissionInfo> infos = new ArrayList<PermissionInfo>();
                        for (OptionSelect select : selected) {
                            if (select.getId() == 0) {
                                continue;
                            }
                            PermissionInfo permissionInfo = new PermissionInfo();
                            permissionInfo.setType(PermissionInfo.Type.READ_ENTRY);
                            permissionInfo.setArticle(PermissionInfo.Article.GROUP);
                            permissionInfo.setArticleId(select.getId());
                            infos.add(permissionInfo);
                        }
                        service.updateBulkUploadPermissions(ClientController.sessionId, currentInput.getId(),
                                                            currentInput.getImportType(), infos, callback);
                    }

                    @Override
                    public void onSuccess(Long result) {
                        currentInput.setId(result);
                    }
                }.go(eventBus);
            }
        };
    }

    protected void createPreferenceDelegate() {
        updatePreferenceDelegate = new ServiceDelegate<PreferenceInfo>() {
            @Override
            public void execute(final PreferenceInfo preferenceInfo) {
                new IceAsyncCallback<Long>() {

                    @Override
                    protected void callService(AsyncCallback<Long> callback) throws AuthenticationException {
                        service.updateBulkUploadPreference(ClientController.sessionId, currentInput.getId(),
                                                           currentInput.getImportType(), preferenceInfo, callback);
                    }

                    @Override
                    public void onSuccess(Long result) {
                        BulkUploadInfo info = currentInput.getSheet().setUpdateBulkUploadId(result);
                        currentInput.setId(result);
                        view.updateBulkUploadDraftInfo(info);
                    }
                }.go(eventBus);
            }
        };
    }

    /**
     * Enables auto save as user enters data into each cell
     */
    protected void enableAutoUpdate() {
        autoUpdateDelegate = new ServiceDelegate<BulkUploadAutoUpdate>() {

            @Override
            public void execute(final BulkUploadAutoUpdate wrapper) {
                view.setUpdatingVisibility(true);

                new IceAsyncCallback<BulkUploadAutoUpdate>() {

                    @Override
                    protected void callService(AsyncCallback<BulkUploadAutoUpdate> callback)
                            throws AuthenticationException {
                        service.autoUpdateBulkUpload(ClientController.sessionId, wrapper, currentInput.getImportType(),
                                                     callback);
                    }

                    @Override
                    public void onSuccess(BulkUploadAutoUpdate result) {
                        BulkUploadInfo info = currentInput.getSheet().setUpdatedEntry(result);
                        currentInput.setId(result.getBulkUploadId());
                        view.updateBulkUploadDraftInfo(info);
                        view.setLastUpdated(result.getLastUpdate());
                        view.setUpdatingVisibility(false);
                    }

                    @Override
                    public void serverFailure() {
                        Window.alert("There was an error saving your work");
                    }
                }.go(eventBus);
            }
        };
    }

    private void setClickHandlers() {
        // submit
        SheetSubmitHandler submitHandler = new SheetSubmitHandler();
        view.setSubmitHandler(submitHandler);

        // reset
        SheetResetHandler resetHandler = new SheetResetHandler();
        view.setResetHandler(resetHandler);

        // draft save
        BulkUploadRenameHandler renameHandler = new BulkUploadRenameHandler();
        view.setDraftNameSetHandler(renameHandler);

        // approve
        BulkUploadApproveHandler approveHandler = new BulkUploadApproveHandler();
        view.setApproveHandler(approveHandler);
    }

    private void addToggleMenuHandler() {
        view.addToggleMenuHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                view.setDraftMenuVisibility(!view.getMenuVisibility(), true);
            }
        });
    }

    /**
     * Sets selection model handler for draft menu. Obtains user selection, retrieves information
     * about it from the server and then displays the data to the user
     */
    private void setMenuSelectionModel() {
        view.getDraftMenuModel().addSelectionChangeHandler(new MenuSelectionHandler(view.getDraftMenuModel(), false));
        view.getPendingMenuModel().addSelectionChangeHandler(
                new MenuSelectionHandler(view.getPendingMenuModel(), true));
    }

    // for new sheet selections
    private void setCreateBulkUploadSelectionModel() {
        final SingleSelectionModel<EntryAddType> createSelection = view.getImportCreateModel();
        createSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                EntryAddType selection = createSelection.getSelectedObject();
                if (selection == null)
                    return;

                // check if an existing sheet has been cached
                if (sheetCache.containsKey(selection))
                    currentInput = sheetCache.get(selection);
                else {
                    // otherwise create a new sheet and retrieve associated data
                    Sheet sheet = new Sheet(selection, updatePreferenceDelegate);
                    sheet.setAutoUpdateDelegate(autoUpdateDelegate);
                    currentInput = new NewBulkInput(selection, sheet);
                    sheetCache.put(selection, currentInput);
                    model.retrieveStorageSchemes(selection, currentInput, null);
                }

                view.setSheet(currentInput, true, false);
                view.setDraftMenuVisibility(false, false);
                currentInput.getSheet().resetWidth();
                createSelection.setSelected(selection, false);
                retrievePreferences(currentInput.getSheet());
            }
        });
    }

    /**
     * Retrieves user preferences and sets in the sheet passed in the parameter.
     *
     * @param sheet Bulk upload sheet being displayed to the user
     */
    private void retrievePreferences(final Sheet sheet) {
        new IceAsyncCallback<HashMap<PreferenceKey, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<PreferenceKey, String>> callback)
                    throws AuthenticationException {
                ArrayList<PreferenceKey> keys = new ArrayList<PreferenceKey>();
                keys.add(PreferenceKey.FUNDING_SOURCE);
                keys.add(PreferenceKey.PRINCIPAL_INVESTIGATOR);
                model.getService().retrieveUserPreferences(ClientController.sessionId, keys, callback);
            }

            @Override
            public void onSuccess(HashMap<PreferenceKey, String> result) {
                if (result == null || result.isEmpty())
                    return;

                // remove underscores
                HashMap<String, PreferenceInfo> preferences = new HashMap<String, PreferenceInfo>();
                for (Map.Entry<PreferenceKey, String> entry : result.entrySet()) {
                    String key = entry.getKey().toString();
                    PreferenceInfo preference = new PreferenceInfo(true, key, entry.getValue());
                    preferences.put(key, preference);
                }
                sheet.getPresenter().setPreferences(preferences);
            }
        }.go(eventBus);
    }

    private void retrieveSavedDrafts() {
        this.model.retrieveDraftMenuData(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                savedDrafts.clear();
                Date date = new Date(0);

                for (BulkUploadInfo info : event.getData()) {
                    String name = info.getName();
                    String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                    BulkUploadMenuItem item = new BulkUploadMenuItem(info.getId(), name, info.getCount(), dateTime,
                                                                     info.getType().toString(),
                                                                     info.getAccount().getEmail());
                    savedDrafts.add(item);
                    if (date.before(info.getLastUpdate()))
                        date = info.getLastUpdate();
                }

                if (!savedDrafts.isEmpty()) {
                    view.setSavedDraftsData(savedDrafts, DateUtilities.formatShorterDate(date),
                                            new DeleteBulkUploadHandler(model.getService(), model.getEventBus()));
                } else
                    view.setToggleMenuVisibility(false);
            }
        });
    }

    private void retrievePendingIfAdmin() {
        if (!ClientController.account.isAdmin())
            return;

        this.model.retrieveDraftsPendingVerification(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                ArrayList<BulkUploadMenuItem> data = new ArrayList<BulkUploadMenuItem>();
                for (BulkUploadInfo info : event.getData()) {
                    String name = info.getName();
                    String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                    BulkUploadMenuItem item = new BulkUploadMenuItem(info.getId(), name, info.getCount(), dateTime,
                                                                     info.getType().toString(),
                                                                     info.getAccount().getEmail());
                    data.add(item);
                }

                if (!data.isEmpty()) {
                    view.setPendingDraftsData(data, new RevertPendingBulkUploadHandler(model.getService(),
                                                                                       model.getEventBus()));
                }
            }
        });
    }

    /**
     * Retrieves groups for assigning read access to the bulk upload
     */
    private void retrieveGroups() {
        new IceAsyncCallback<ArrayList<GroupInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<GroupInfo>> callback) throws AuthenticationException {
                service.retrieveUserGroups(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<GroupInfo> result) {
                ArrayList<OptionSelect> groups = new ArrayList<OptionSelect>();
                if (result != null) {
                    for (GroupInfo info : result) {
                        groups.add(new OptionSelect(info.getId(), info.getLabel()));
                    }
                }
                view.setPermissionGroups(groups);
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }

    //
    // inner classes
    //
    private class SheetSubmitHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            currentInput.getSheet().closeOpenCells();

            boolean isValid = currentInput.getSheet().validate();
            if (!isValid) {
                view.showFeedback("Please correct validation errors", true);
                return;
            }

            BulkUploadSubmitEventHandler eventHandler = new BulkUploadSubmitEventHandler() {

                @Override
                public void onSubmit(BulkUploadSubmitEvent event) {
                    if (event.isSuccess()) {
                        view.showFeedback("Entries submitted successfully for verification.", false);
                        History.newItem(Page.COLLECTIONS.getLink());
                    } else {
                        view.showFeedback("Error saving entries.", true);
                    }
                }
            };

            model.submitBulkImportDraft(currentInput.getId(), eventHandler);
        }
    }

    private class BulkUploadApproveHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            boolean isValid = currentInput.getSheet().validate();
            if (!isValid) {
                view.showFeedback("Please correct validation errors", true);
                return;
            }

            // for approval we do not want to change the owner
            model.approvePendingBulkImport(currentInput.getId(), new BulkUploadSubmitEventHandler() {

                @Override
                public void onSubmit(BulkUploadSubmitEvent event) {
                    if (event.isSuccess()) {
                        view.showFeedback("Entries approved successfully", false);
                        History.newItem(Page.COLLECTIONS.getLink());
                    } else {
                        view.showFeedback("Error approve bulk import.", true);
                    }
                }
            });
        }
    }

    private class SheetResetHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            currentInput.getSheet().clear();
        }
    }

    private class BulkUploadRenameHandler implements Delegate<String> {

        @Override
        public void execute(final String name) {
            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.setBulkUploadDraftName(ClientController.sessionId, currentInput.getId(), name, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    view.setDraftName(name);
                    currentInput.setName(name);
                }
            }.go(eventBus);
        }
    }

    /**
     * Selection handler for existing bulk upload.
     */
    private class MenuSelectionHandler implements SelectionChangeEvent.Handler {

        private final SingleSelectionModel<BulkUploadMenuItem> selection;
        private final boolean isValidation;

        public MenuSelectionHandler(SingleSelectionModel<BulkUploadMenuItem> selection, boolean isValidation) {
            this.selection = selection;
            this.isValidation = isValidation;
        }

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
            final BulkUploadMenuItem item = selection.getSelectedObject();
            view.setLoading(true);
            model.retrieveBulkImport(item.getId(), 0, 1000, new SavedDraftsEventHandler() {

                @Override
                public void onDataRetrieval(SavedDraftsEvent event) {
                    if (event == null) {
                        view.showFeedback("Could not retrieve saved draft", true);
                        return;
                    }

                    BulkUploadInfo info = event.getData().get(0);
                    EntryInfo firstEntry = info.getEntryList().isEmpty() ? null : info.getEntryList().get(0);
                    Sheet sheet = new Sheet(info.getType(), updatePreferenceDelegate, info);
                    sheet.setAutoUpdateDelegate(autoUpdateDelegate);
                    currentInput = new NewBulkInput(info.getType(), sheet);
                    currentInput.setId(info.getId());
                    if (firstEntry != null) {
                        model.retrieveStorageSchemes(info.getType(), currentInput, firstEntry.getOneSampleStorage());
                    }

                    // bulk upload permissions
                    ArrayList<OptionSelect> groups = new ArrayList<OptionSelect>();
                    for (PermissionInfo permissionInfo : info.getPermissions()) {
                        if (permissionInfo.getArticle() != PermissionInfo.Article.GROUP)
                            continue;

                        groups.add(new OptionSelect(permissionInfo.getArticleId(), permissionInfo.getDisplay()));
                    }
                    view.setSelectedPermissionGroups(groups);

                    String name = info.getName();

                    // setting name to creation date is none exist
                    if (name == null) {
                        name = DateUtilities.formatDate(info.getCreated());
                        info.setName(name);
                    }
                    currentInput.setName(name);

                    view.setSheet(currentInput, false, isValidation);
                    view.setDraftMenuVisibility(false, false);

                    // assume uniform values for all entries in regards to creator (same for permissions)
                    String creator = ClientController.account.getFullName();
                    String creatorEmail = ClientController.account.getEmail();
                    if (firstEntry != null) {
                        if (firstEntry.getCreatorEmail() != null && !firstEntry.getCreatorEmail().isEmpty()
                                && firstEntry.getCreator() != null && !firstEntry.getCreator().isEmpty()) {
                            creator = firstEntry.getCreator();
                            creatorEmail = firstEntry.getCreatorEmail();
                        }
                    }
                    view.setCreatorInformation(creator, creatorEmail);
                }
            });
        }
    }
}
