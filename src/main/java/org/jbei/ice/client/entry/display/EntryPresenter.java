package org.jbei.ice.client.entry.display;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.add.EntryAddPresenter;
import org.jbei.ice.client.collection.add.EntryFormFactory;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter.DeleteEntryHandler;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.entry.display.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.display.handler.HasAttachmentDeleteHandler;
import org.jbei.ice.client.entry.display.model.FlagEntry;
import org.jbei.ice.client.entry.display.view.AttachmentItem;
import org.jbei.ice.client.entry.display.view.DeleteSequenceHandler;
import org.jbei.ice.client.entry.display.view.EntryView;
import org.jbei.ice.client.entry.display.view.IEntryView;
import org.jbei.ice.client.entry.display.view.MenuItem;
import org.jbei.ice.client.entry.display.view.MenuItem.Menu;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.comment.UserComment;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Presenter for entry view that handles all the logic associated with retrieving data.
 *
 * @author Hector Plahar
 */
public class EntryPresenter extends AbstractPresenter {

    private final IEntryView display;
    private final EntryModel model;
    private PartData currentPart;
    private EntryContext currentContext;
    private EntryAddPresenter entryAddPresenter;
    private IEntryFormSubmit formSubmit;
    private CollectionsPresenter collectionsPresenter;

    public EntryPresenter(final RegistryServiceAsync service, CollectionsPresenter collectionsPresenter,
            final HandlerManager eventBus, EntryContext context) {
        super(service, eventBus);
        this.display = new EntryView(retrieveEntryTraceSequenceDetailsDelegate(), removeAddPublicAccessDelegate());
        this.currentContext = context;
        this.model = new EntryModel(service, this.display, eventBus);
        display.getMenu().setSelectionHandler(new MenuSelectionHandler());
        this.collectionsPresenter = collectionsPresenter;
        if (collectionsPresenter != null)
            entryAddPresenter = new EntryAddPresenter(collectionsPresenter, EntryPresenter.this, service, eventBus);

        setContextNavHandlers();
        showCurrentEntryView();

        // add sample button handler
        display.addSampleButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                model.retrieveStorageSchemes(currentPart);
            }
        });

        // GENERAL
        display.addGeneralEditButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                IEntryFormSubmit formUpdate = EntryFormFactory.updateForm(currentPart);
                if (formUpdate == null)
                    return;

                formUpdate.addCancelHandler(new FormCancelHandler());
                formUpdate.addSubmitHandler(new UpdateFormSubmitHandler(formUpdate));
                if (entryAddPresenter != null)
                    formUpdate.setPreferences(entryAddPresenter.getPreferences());

                display.showUpdateForm(formUpdate, currentPart);
            }
        });

        // PERMISSIONS (handlers for adding read/write)
        display.getPermissionsWidget().setPermissionAddSelectionHandler(new PermissionAddDelegate());

        // attachment delete handler
        display.setAttachmentDeleteHandler(new HasAttachmentDeleteHandler() {

            @Override
            public void deleteAttachment(final AttachmentItem item) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.deleteEntryAttachment(ClientController.sessionId, item.getFileId(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (result)
                            display.removeAttachment(item);
                        else
                            Window.alert("Failed to delete attachment");
                    }
                }.go(eventBus);
            }
        });

        DeleteSequenceHandler deleteHandler = new DeleteSequenceHandler(service, eventBus);
        display.setDeleteSequenceHandler(deleteHandler);
        display.setSequenceDeleteHandler(new DeleteSequenceTraceHandler());
        setCommentSubmitDelegate();
        setFlagDelegate();
    }

    /**
     * Sets delegate used by EntryCommentPanel to submit user comments for current entry
     */
    public void setCommentSubmitDelegate() {
        display.addSubmitCommentDelegate(new ServiceDelegate<UserComment>() {
            @Override
            public void execute(final UserComment comment) {
                new IceAsyncCallback<UserComment>() {

                    @Override
                    protected void callService(AsyncCallback<UserComment> callback) throws AuthenticationException {
                        comment.setEntryId(currentPart.getId());
                        service.sendComment(ClientController.sessionId, comment, callback);
                    }

                    @Override
                    public void onSuccess(UserComment result) {
                        display.addComment(result);
                    }
                }.go(eventBus);
            }
        });
    }

    public void setCurrentContext(EntryContext context) {
        this.currentContext = context;
    }

    public void showCurrentEntryView() {
        setContextNavData();
        retrieveEntryDetails();
    }

    public void showCreateEntry(EntryAddType addType) {
        IEntryFormSubmit newForm = entryAddPresenter.getEntryForm(addType, new FormCancelHandler());

        this.formSubmit = newForm;
        display.showNewForm(newForm);
        display.setEntryHeader("create new " + newForm.getHeaderDisplay(), "", ClientController.account.getFullName(),
                               ClientController.account.getEmail(), (new Date(System.currentTimeMillis())));
        display.getPermissionsWidget().setCanEdit(true);
        if (!entryAddPresenter.getDefaultPermissions().isEmpty()) {
            display.getPermissionsWidget().setPermissionData(entryAddPresenter.getDefaultPermissions(),
                                                             new DeletePermission());
        }
        display.getMenu().switchToEditMode(true);

        // sequence panel
        SequenceViewPanelPresenter sequencePresenter = newForm.getSequenceViewPresenter();
        new PasteSequenceDelegate(sequencePresenter);
        currentPart = newForm.getEntry();
    }

    public void setDefaultPermissions(ArrayList<AccessPermission> accessPermissions) {
        display.getPermissionsWidget().setPermissionData(accessPermissions, new DeletePermission());
    }

    protected void setContextNavData() {
        if (currentContext == null)
            return;

        IHasNavigableData nav = this.currentContext.getNav();
        boolean show = (nav != null);

        display.showContextNav(show);
        if (!show) {
            display.enableNext(false);
            display.enablePrev(false);
            return;
        }

        int size = nav.getSize();
        if (size == 0 || size == 1) {
            display.enableNext(false);
            display.enablePrev(false);
        }

        PartData info = nav.getCachedData(this.currentContext.getId(), this.currentContext.getRecordId());
        int idx = nav.indexOfCached(info);

        display.enablePrev(!(idx == 0));
        boolean atEnd = ((idx + 1) == size);
        display.enableNext(!atEnd);

        String text = formatNumber((idx + 1)) + " of " + formatNumber(size);
        display.setNavText(text);
    }

    private String formatNumber(long l) {
        if (l < 0)
            return "";

        NumberFormat format = NumberFormat.getFormat("##,###");
        return format.format(l);
    }

    private void setContextNavHandlers() {
        if (currentContext == null)
            return;

        display.setNextHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                IHasNavigableData nav = currentContext.getNav();
                PartData currentInfo = nav.getCachedData(EntryPresenter.this.currentPart.getId(),
                                                         EntryPresenter.this.currentPart.getRecordId());
                int idx = nav.indexOfCached(currentInfo);

                if (idx == -1) {
                    display.enableNext(false);
                    display.enablePrev(false);
                    return; // we have a problem. most likely means we did not disable next at the right time
                }

                int size = nav.getSize();
                int next = idx + 1;
                if (next + 1 == size)
                    display.enableNext(false);

                PartData nextInfo = nav.getNext(currentInfo);
                long currentId = nextInfo.getId();
                if (currentContext.getPartnerUrl() == null)
                    History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + currentId, false);
//                EntryPresenter.this.currentPart = nextInfo;
                currentContext.setId(currentId);
                currentContext.setRecordId(nextInfo.getRecordId());
                retrieveEntryDetails();
                display.enablePrev(true);
                String text = formatNumber((next + 1)) + " of " + formatNumber(size);
                display.setNavText(text);
            }
        });

        // add previous handler
        display.setPrevHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                IHasNavigableData nav = currentContext.getNav();
                PartData currentInfo = nav.getCachedData(currentContext.getId(), currentContext.getRecordId());
                int idx = nav.indexOfCached(currentInfo);

                if (idx == -1) {
                    display.enableNext(false);
                    display.enablePrev(false);
                    return; // we have a problem. most likely means we did not disable prev at the right time
                }

                int prev = idx - 1;
                if (prev <= 0) { // at the first position {
                    display.enablePrev(false);
                }

                PartData prevInfo = nav.getPrev(currentInfo);

                long currentId = prevInfo.getId();
                History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + currentId, false);
                currentContext.setId(currentId);
                currentContext.setRecordId(prevInfo.getRecordId());
                retrieveEntryDetails();
                display.enableNext(true);
                String text = formatNumber((prev + 1)) + " of " + formatNumber(nav.getSize());
                display.setNavText(text);
            }
        });

        // add go back handler
        display.setGoBackHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new ShowEntryListEvent(currentContext));
            }
        });
    }

    private void setFlagDelegate() {
        Delegate<FlagEntry> delegate = new Delegate<FlagEntry>() {
            @Override
            public void execute(final FlagEntry flagOption) {
                if (flagOption.getFlagOption() == FlagEntry.FlagOption.ALERT) {
                    new IceAsyncCallback<UserComment>() {

                        @Override
                        protected void callService(AsyncCallback<UserComment> callback) throws AuthenticationException {
                            service.alertToEntryProblem(ClientController.sessionId, currentPart.getId(),
                                                        flagOption.getMessage(), callback);
                        }

                        @Override
                        public void onSuccess(UserComment result) {
                            String msg;
                            if (result != null) {
                                msg = "Notification sent successfully";
                                display.addComment(result);
                            } else
                                msg = "Your notification could not be sent!";
                            eventBus.fireEvent(new FeedbackEvent(result == null, msg));
                        }
                    }.go(eventBus);
                } else if (flagOption.getFlagOption() == FlagEntry.FlagOption.REQUEST_SAMPLE) {
                    new IceAsyncCallback<Boolean>() {

                        @Override
                        protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                            service.requestSample(ClientController.sessionId, currentPart.getId(),
                                                  flagOption.getMessage(), callback);
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            String msg;
                            if (result) {
                                msg = "Notification sent successfully";
                            } else
                                msg = "Your notification could not be sent!";
                            eventBus.fireEvent(new FeedbackEvent(!result, msg));
                        }
                    }.go(eventBus);
                } else {
                    eventBus.fireEvent(new FeedbackEvent(true, "Invalid selection!"));
                }
            }
        };
        display.addFlagDelegate(delegate);
    }

    private Delegate<Long> retrieveEntryTraceSequenceDetailsDelegate() {
        return new Delegate<Long>() {
            @Override
            public void execute(final Long aLong) {
                if (currentPart.getId() == 0) {
//                    display.setSequenceData(result, currentPart);
//                    display.getMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                    // TODO :
                    return;
                }

                new IceAsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

                    @Override
                    protected void callService(AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback)
                            throws AuthenticationException {
                        service.retrieveEntryTraceSequences(ClientController.sessionId, aLong, callback);
                    }

                    @Override
                    public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                        display.setSequenceData(result, currentPart);
                        display.getMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                    }
                }.go(eventBus);
            }
        };
    }

    private ServiceDelegate<Boolean> removeAddPublicAccessDelegate() {
        return new ServiceDelegate<Boolean>() {
            @Override
            public void execute(final Boolean remove) {
                // handle case when user is only now creating entry
                if (currentPart == null || currentPart.getId() == 0) {
                    display.getPermissionsWidget().setPublicReadAccess(!remove);
                    return;
                }

                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        if (remove)
                            service.disablePublicReadAccess(ClientController.sessionId, currentPart.getId(), callback);
                        else
                            service.enablePublicReadAccess(ClientController.sessionId, currentPart.getId(), callback);
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (!result)
                            return;

                        display.getPermissionsWidget().setPublicReadAccess(!remove);
                    }
                }.go(eventBus);
            }
        };
    }

    private void retrieveEntryDetails() {
        if (currentContext == null)
            return;

        display.getPermissionsWidget().reset();
        display.showLoadingIndicator(false);

        new IceAsyncCallback<PartData>() {

            @Override
            protected void callService(AsyncCallback<PartData> callback) throws AuthenticationException {
                service.retrieveEntryDetails(ClientController.sessionId, currentContext.getId(),
                                             currentContext.getPartnerUrl(), callback);
            }

            @Override
            public void onSuccess(PartData result) {
                if (result == null) {
                    FeedbackEvent event = new FeedbackEvent(true, "Error retrieving entry with id \""
                            + currentContext.getId() + "\"");
                    eventBus.fireEvent(event);
                    return;
                }

                display.getMenu().switchToEditMode(false);
                currentPart = result;
                currentContext.setId(currentPart.getId());
                currentContext.setRecordId(currentPart.getRecordId());

                // permission (order is important here)
                ServiceDelegate<PartSample> delegate = model.createDeleteSampleHandler();
                boolean isLocal = currentContext.getPartnerUrl() == null || currentContext.getPartnerUrl().isEmpty();
                SequenceViewPanelPresenter sequencePresenter = display.setEntryInfoForView(currentPart, delegate,
                                                                                           isLocal);
                display.getPermissionsWidget().setPermissionData(result.getAccessPermissions(), new DeletePermission());
                new PasteSequenceDelegate(sequencePresenter);

                Menu menu = display.getMenu().getCurrentSelection();

                // menu views
                display.getMenu().updateMenuCount(MenuItem.Menu.SEQ_ANALYSIS, result.getSequenceAnalysis().size());
                display.getMenu().updateMenuCount(MenuItem.Menu.SAMPLES, result.getSampleStorage().size());
                display.getMenu().updateMenuCount(Menu.COMMENTS, result.getComments().size());

                // show/hide sample button
                display.setUserCanEdit(currentPart.isCanEdit());
                collectionsPresenter.getView().enableExportAs(currentContext.getPartnerUrl() == null);
                collectionsPresenter.getView().enableBulkEditVisibility(false);

                handleMenuSelection(menu);
            }

            @Override
            public void serverFailure() {
                FeedbackEvent event = new FeedbackEvent(true, "There was an error retrieving the entry");
                eventBus.fireEvent(event);
                display.showLoadingIndicator(true);
            }

            @Override
            public void onNullResult() {
                display.showLoadingIndicator(true);
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    public IEntryView getView() {
        return this.display;
    }

    public PartData getCurrentInfo() {
        return currentPart;
    }

    public void setDeleteHandler(final DeleteEntryHandler deleteEntryHandler) {
        display.addDeleteEntryHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!Window.confirm("Confirm deletion of entry " + currentPart.getPartId()))
                    return;

                deleteEntryHandler.onClick(event);
            }
        });
    }

    private void handleMenuSelection(Menu menu) {
        if (menu == null)
            menu = Menu.GENERAL;

        switch (menu) {
            case GENERAL:
                if (currentPart.getId() == 0 && (currentContext == null || currentContext.getPartnerUrl() == null)) {
                    display.showNewForm(formSubmit);
                    return;
                }
                display.showEntryDetailView();
                break;

            case SEQ_ANALYSIS:
                display.showSequenceView(currentPart);
                break;

            case COMMENTS:
                display.showCommentView(currentPart.getComments());
                break;

            case SAMPLES:
                display.showSampleView();
                break;
        }
    }

    //
    // inner classes
    //

    /**
     * Sequence delegate that adds handlers for uploading a sequence
     */
    private class PasteSequenceDelegate implements ServiceDelegate<String> {

        private SequenceViewPanelPresenter presenter;

        public PasteSequenceDelegate(final SequenceViewPanelPresenter presenter) {
            if (presenter == null)
                return;

            this.presenter = presenter;
            presenter.addSequencePasteHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final String sequence = presenter.getSequence();
                    callService(sequence);
                }
            });
        }

        @Override
        public void execute(final String sequence) {
            callService(sequence);
        }

        private void callService(final String sequence) {
            if (this.presenter == null)
                return;

            new IceAsyncCallback<PartData>() {

                @Override
                protected void callService(AsyncCallback<PartData> callback) throws AuthenticationException {
                    boolean isFile = !presenter.isPastedSequence();
                    service.saveSequence(ClientController.sessionId, currentPart, sequence, isFile, callback);
                }

                @Override
                public void onSuccess(PartData result) {
                    boolean hasSequence = result != null;
                    presenter.setHasSequence(hasSequence);
                    if (!hasSequence) {
                        Window.alert("Could not save sequence");
                        return;
                    }

                    presenter.getPartData().setHasSequence(true);
                    presenter.getPartData().setHasOriginalSequence(true);
                    presenter.getPartData().setId(result.getId());
                    presenter.getPartData().setRecordId(result.getRecordId());

                    currentPart = presenter.getPartData();

                    // display the flash widget for uploaded sequence
                    presenter.updateSequenceView();
                }
            }.go(eventBus);
        }
    }

    private class PermissionAddDelegate implements ServiceDelegate<AccessPermission> {

        @Override
        public void execute(final AccessPermission access) {
            if (currentPart.getId() == 0) {
                currentPart.getAccessPermissions().add(access);
                displayPermission(access);
                return;
            }

            access.setTypeId(currentPart.getId());
            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.addPermission(ClientController.sessionId, access, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (result)
                        displayPermission(access);
                }
            }.go(eventBus);
        }

        protected void displayPermission(AccessPermission access) {
            DeletePermission deletePermission = new DeletePermission();
            if (access.getType() == AccessPermission.Type.WRITE_ENTRY) {
                display.getPermissionsWidget().addWriteItem(access, deletePermission);
            } else {
                display.getPermissionsWidget().addReadItem(access, deletePermission);
            }
        }
    }

    private class DeletePermission implements Delegate<AccessPermission> {

        @Override
        public void execute(final AccessPermission access) {
            if (access.getTypeId() == 0) {
                currentPart.getAccessPermissions().remove(access);
                display.getPermissionsWidget().removeItem(access);
                return;
            }

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.removePermission(ClientController.sessionId, access, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    display.getPermissionsWidget().removeItem(access);
                }
            }.go(eventBus);
        }
    }

    /*Handler for the entry detail menu*/
    public class MenuSelectionHandler implements SelectionChangeEvent.Handler {

        private Menu currentSelection;

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
            if (currentSelection == display.getMenu().getCurrentSelection()
                    || display.getMenu().getCurrentSelection() == null) {
                return;
            }

            currentSelection = display.getMenu().getCurrentSelection();
            display.getMenu().setSelection(currentSelection);
            handleMenuSelection(currentSelection);
        }
    }

    private class FormCancelHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            showCurrentEntryView();
            Window.scrollTo(0, 0);
        }
    }

    private class UpdateFormSubmitHandler implements ClickHandler {

        private final IEntryFormSubmit formSubmit;

        public UpdateFormSubmitHandler(IEntryFormSubmit formSubmit) {
            this.formSubmit = formSubmit;
        }

        @Override
        public void onClick(ClickEvent event) {
            FocusWidget focus = formSubmit.validateForm();
            if (focus != null) {
                focus.setFocus(true);
                FeedbackEvent feedback = new FeedbackEvent(true, "Please fill out all required fields");
                eventBus.fireEvent(feedback);
                return;
            }

            formSubmit.populateEntries();
            update(formSubmit.getEntry());
        }

        /**
         * Makes an rpc to save the set of entrys
         */
        protected void update(final PartData info) {
            if (info == null)
                return;

            new IceAsyncCallback<Long>() {

                @Override
                protected void callService(AsyncCallback<Long> callback) throws AuthenticationException {
                    service.updateEntry(ClientController.sessionId, info, callback);
                }

                @Override
                public void onSuccess(Long result) {
                    if (result == null) {
                        eventBus.fireEvent(new FeedbackEvent(true, "Your entry could not be updated"));
                    } else {
                        showCurrentEntryView();
                        eventBus.fireEvent(new FeedbackEvent(false, "Entry successfully updated"));
                        Window.scrollTo(0, 0);
                    }
                }

                @Override
                public void serverFailure() {
                    eventBus.fireEvent(new FeedbackEvent(true, "Error updating record"));
                }
            }.go(eventBus);
        }
    }

    /**
     * Handler for deleting trace sequence files
     */
    public class DeleteSequenceTraceHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final long entryId = currentPart.getId();
            Set<SequenceAnalysisInfo> selected = display.getSequenceTableSelectionModel().getSelectedSet();
            if (selected == null || selected.isEmpty())
                return;

            final ArrayList<String> fileIds = new ArrayList<String>();
            for (SequenceAnalysisInfo info : selected) {
                fileIds.add(info.getFileId());
            }

            new IceAsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

                @Override
                protected void callService(AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback)
                        throws AuthenticationException {
                    service.deleteEntryTraceSequences(ClientController.sessionId, entryId, fileIds, callback);
                }

                @Override
                public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                    display.setSequenceData(result, currentPart);
                    display.getMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                }
            }.go(eventBus);
        }
    }
}
