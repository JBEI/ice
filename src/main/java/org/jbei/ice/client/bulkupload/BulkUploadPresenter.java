package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.bulkupload.events.BulkUploadDraftSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEvent;
import org.jbei.ice.client.bulkupload.events.BulkUploadSubmitEventHandler;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkupload.events.SavedDraftsEventHandler;
import org.jbei.ice.client.bulkupload.model.BulkUploadModel;
import org.jbei.ice.client.bulkupload.model.NewBulkInput;
import org.jbei.ice.client.bulkupload.sheet.Sheet;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
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
    private final ArrayList<BulkUploadMenuItem> savedDrafts = new ArrayList<BulkUploadMenuItem>(); // list of saved
    // drafts

    public BulkUploadPresenter(BulkUploadModel model, final IBulkUploadView display) {
        this.view = display;
        this.model = model;
        sheetCache = new HashMap<EntryAddType, NewBulkInput>();

        setClickHandlers();

        // selection model handlers
        setMenuSelectionModel();
        setCreateSelectionModel();

        // toggle menu
        addToggleMenuHandler();

        // retrieveData
        retrieveSavedDrafts();
        retrievePendingIfAdmin();

        model.getEventBus().addHandler(FeedbackEvent.TYPE,
                                       new FeedbackEvent.IFeedbackEventHandler() {
                                           @Override
                                           public void onFeedbackAvailable(FeedbackEvent event) {
                                               display.showFeedback(event.getMessage(), event.isError());
                                           }
                                       });

        // retrieve groups
        retrieveUserGroups();
    }

    public void retrieveUserGroups() {
        new IceAsyncCallback<ArrayList<GroupInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<GroupInfo>> callback) throws AuthenticationException {
                // TODO : when the group management system is fleshed out, this call should be to retrieve
                // TODO : groups of users not all of them
                model.getService().retrieveAllGroups(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<GroupInfo> result) {
                view.setGroupPermissions(result);
            }
        }.go(model.getEventBus());
    }

    private void setClickHandlers() {

        // draft update
        SheetDraftUpdateHandler handler = new SheetDraftUpdateHandler();
        view.setDraftUpdateHandler(handler);

        // submit
        SheetSubmitHandler submitHandler = new SheetSubmitHandler(false);
        view.setSubmitHandler(submitHandler);

        // draft submit
        SheetSubmitHandler draftSubmitHandler = new SheetSubmitHandler(true);
        view.setDraftSubmitHandler(draftSubmitHandler);

        // reset
        SheetResetHandler resetHandler = new SheetResetHandler();
        view.setResetHandler(resetHandler);

        // draft save
        SheetDraftSaveHandler draftSaveHandler = new SheetDraftSaveHandler();
        view.setDraftSaveHandler(draftSaveHandler);

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
        view.getDraftMenuModel().addSelectionChangeHandler(
                new MenuSelectionHandler(view.getDraftMenuModel(), false));
        view.getPendingMenuModel().addSelectionChangeHandler(
                new MenuSelectionHandler(view.getPendingMenuModel(), true));
    }

    private void setCreateSelectionModel() {
        final SingleSelectionModel<EntryAddType> createSelection = view.getImportCreateModel();
        createSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                EntryAddType selection = createSelection.getSelectedObject();
                if (selection == null)
                    return;

                // check if an existing sheet has been cached and return it if so
                if (sheetCache.containsKey(selection))
                    currentInput = sheetCache.get(selection);
                else {
                    // otherwise create a new sheet and retrieve associated data
                    Sheet sheet = new Sheet(selection);
                    currentInput = new NewBulkInput(selection, sheet);
                    sheetCache.put(selection, currentInput);
                    model.retrieveStorageSchemes(EntryAddType.addTypeToType(selection), currentInput);
                }

                view.setSheet(currentInput, true, false);
                view.setHeader(selection.getDisplay() + " Bulk Import");
                view.setDraftMenuVisibility(false, false);
                createSelection.setSelected(selection, false);
            }
        });
    }

    private void retrieveSavedDrafts() {
        this.model.retrieveDraftMenuData(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                savedDrafts.clear();
                for (BulkUploadInfo info : event.getData()) {
                    String name = info.getName();
                    String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                    BulkUploadMenuItem item = new BulkUploadMenuItem(info.getId(), name, info
                            .getCount(), dateTime, info.getType().toString(), info.getAccount().getEmail());
                    savedDrafts.add(item);
                }

                if (!savedDrafts.isEmpty()) {
                    view.setSavedDraftsData(savedDrafts, new DeleteBulkUploadHandler(model.getService(),
                                                                                     model.getEventBus()));
                } else
                    view.setToggleMenuVisibility(false);
            }
        });
    }

    private void retrievePendingIfAdmin() {
        if (!AppController.accountInfo.isModerator())
            return;

        this.model.retrieveDraftsPendingVerification(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                ArrayList<BulkUploadMenuItem> data = new ArrayList<BulkUploadMenuItem>();
                for (BulkUploadInfo info : event.getData()) {
                    String name = info.getName();
                    String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                    BulkUploadMenuItem item = new BulkUploadMenuItem(info.getId(), name, info
                            .getCount(), dateTime, info.getType().toString(), info.getAccount().getEmail());
                    data.add(item);
                }

                if (!data.isEmpty())
                    view.setPendingDraftsData(data, new DeleteBulkUploadHandler(model.getService(),
                                                                                model.getEventBus()));
            }
        });
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

        private final boolean isDraftSubmit;

        public SheetSubmitHandler(boolean isDraftSubmit) {
            this.isDraftSubmit = isDraftSubmit;
        }

        @Override
        public void onClick(ClickEvent event) {
            boolean isValid = currentInput.getSheet().validate();
            if (!isValid) {
                view.showFeedback("Please correct validation errors", true);
                return;
            }

            ArrayList<EntryInfo> cellData = currentInput.getSheet().getCellData(
                    AppController.accountInfo.getEmail(), AppController.accountInfo.getFullName(), view.getCreator(),
                    view.getCreatorEmail());
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data into the sheet before submitting", true);
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

            if (isDraftSubmit)
                model.submitBulkImportDraft(currentInput.getId(), cellData, view.getPermissionSelection(),
                                            eventHandler);
            else
                model.submitBulkImport(currentInput.getImportType(), cellData, view.getPermissionSelection(),
                                       eventHandler);
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
            ArrayList<EntryInfo> cellData = currentInput.getSheet().getCellData(null, null, view.getCreator(),
                                                                                view.getCreatorEmail());
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data into the sheet before submitting", true);
                return;
            }

            model.approvePendingBulkImport(currentInput.getId(), cellData, view.getPermissionSelection(),
                                           new BulkUploadSubmitEventHandler() {

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

    private class SheetDraftSaveHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String name = view.getDraftName();
            currentInput.setName(name);

            ArrayList<EntryInfo> cellData = currentInput.getSheet().getCellData(
                    AppController.accountInfo.getEmail(), AppController.accountInfo.getFullName(), view.getCreator(),
                    view.getCreatorEmail());
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data before saving draft", true);
                return;
            }

            BulkUploadSaveHandler handler = new BulkUploadSaveHandler();
            String uuid = view.getPermissionSelection();
            model.saveBulkImportDraftData(currentInput.getImportType(), name, cellData, uuid, handler);
        }
    }

    private class BulkUploadSaveHandler implements BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler {

        @Override
        public void onSubmit(BulkUploadDraftSubmitEvent event) {
            if (event == null || event.getDraftInfo() == null)
                view.showFeedback("Error saving draft", true);
            else {
                BulkUploadInfo info = event.getDraftInfo();
                view.showFeedback("Draft successfully saved", false);
                String dateTime = DateUtilities.formatShorterDate(
                        info.getCreated());
                BulkUploadMenuItem item = new BulkUploadMenuItem(info.getId(),
                                                                 info.getName(),
                                                                 info.getCount(),
                                                                 dateTime,
                                                                 info.getType().toString(),
                                                                 info.getAccount().getEmail());

                savedDrafts.add(item);
                view.setSavedDraftsData(savedDrafts,
                                        new DeleteBulkUploadHandler(model.getService(), model.getEventBus()));

                currentInput.setName(info.getName());
                currentInput.setId(info.getId());
                currentInput.getSheet().setCurrentInfo(info);

                view.setSheet(currentInput, false, false);
                view.setHeader(currentInput.getImportType().getDisplay() + " Bulk Import");
                // check if menu panel is visible
                if (view.getMenuVisibility() == false)
                    view.setDraftMenuVisibility(true, true);
            }
        }
    }

    private class SheetDraftUpdateHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (currentInput == null)
                return;

            long id = currentInput.getId();
            final EntryAddType type = currentInput.getImportType();
            ArrayList<EntryInfo> cellData = currentInput.getSheet().getCellData(
                    AppController.accountInfo.getEmail(), AppController.accountInfo.getFullName(), view.getCreator(),
                    view.getCreatorEmail());

            BulkUploadUpdateHandler handler = new BulkUploadUpdateHandler(type);
            String uuid = view.getPermissionSelection();
            model.updateBulkImportDraft(id, cellData, uuid, handler);
        }
    }

    private class BulkUploadUpdateHandler implements BulkUploadDraftSubmitEvent.BulkUploadDraftSubmitEventHandler {

        private final EntryAddType type;

        public BulkUploadUpdateHandler(EntryAddType type) {
            this.type = type;
        }

        @Override
        public void onSubmit(BulkUploadDraftSubmitEvent event) {
            if (event == null || event.getDraftInfo() == null)
                view.showFeedback("Error updating draft", true);
            else {
                BulkUploadInfo info = event.getDraftInfo();
                view.showFeedback("Update successful", false);
                String time = DateUtilities.formatShorterDate(info.getCreated());
                BulkUploadMenuItem menuItem = new BulkUploadMenuItem(info.getId(),
                                                                     info.getName(),
                                                                     info.getCount(),
                                                                     time,
                                                                     type.getDisplay(),
                                                                     AppController.accountInfo.getEmail());
                view.updateSavedDraftsMenu(menuItem);
                sheetCache.clear();
            }
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
            model.retrieveBulkImport(item.getId(), new SavedDraftsEventHandler() {

                @Override
                public void onDataRetrieval(SavedDraftsEvent event) {
                    if (event == null) {
                        view.showFeedback("Could not retrieve saved draft", true);
                        return;
                    }

                    BulkUploadInfo info = event.getData().get(0);
                    Sheet sheet = new Sheet(info.getType(), info);
                    currentInput = new NewBulkInput(info.getType(), sheet);
                    currentInput.setId(info.getId());
                    String name = info.getName();

                    // setting name to creation date is none exist
                    if (name == null) {
                        name = DateUtilities.formatDate(info.getCreated());
                        info.setName(name);
                    }
                    currentInput.setName(name);

                    view.setSheet(currentInput, false, isValidation);
                    view.setHeader(info.getType().getDisplay() + " Bulk Import");
                    view.setDraftMenuVisibility(false, false);
                    view.setSelectedGroupPermission(info.getGroupInfo());

                    // assume uniform values for all entries in regards to creator
                    if (!info.getEntryList().isEmpty()) {
                        EntryInfo entryInfo = info.getEntryList().get(0);
                        view.setCreatorInformation(entryInfo.getCreator(), entryInfo.getCreatorEmail());
                    }
                }
            });
        }
    }
}
