package org.jbei.ice.client.entry.view;

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
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter.DeleteEntryHandler;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.handler.HasAttachmentDeleteHandler;
import org.jbei.ice.client.entry.view.handler.ReadBoxSelectionHandler;
import org.jbei.ice.client.entry.view.handler.UploadPasteSequenceHandler;
import org.jbei.ice.client.entry.view.view.AttachmentItem;
import org.jbei.ice.client.entry.view.view.DeleteSequenceHandler;
import org.jbei.ice.client.entry.view.view.EntryView;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem.Menu;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter;
import org.jbei.ice.client.entry.view.view.SequenceFileUploadHandler;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

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
 * Presenter for entry view
 *
 * @author Hector Plahar
 */
public class EntryPresenter extends AbstractPresenter {

    private final IEntryView display;
    private final EntryModel model;
    private EntryInfo currentInfo;
    private EntryContext currentContext;
    private EntryAddPresenter entryAddPresenter;
    private IEntryFormSubmit formSubmit;

    public EntryPresenter(final RegistryServiceAsync service, CollectionsPresenter collectionsPresenter,
            final HandlerManager eventBus, EntryContext context) {
        super(service, eventBus);
        this.display = new EntryView(retrieveEntryTraceSequenceDetailsDelegate());
        this.currentContext = context;
        this.model = new EntryModel(service, this.display, eventBus);
        display.getMenu().setSelectionHandler(new MenuSelectionHandler());
        entryAddPresenter = new EntryAddPresenter(collectionsPresenter, EntryPresenter.this, service, eventBus);

        setContextNavHandlers();
        showCurrentEntryView();

        // add sample button handler
        display.addSampleButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                model.retrieveStorageSchemes(currentInfo);
            }
        });

        // GENERAL
        display.addGeneralEditButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                IEntryFormSubmit formUpdate = display.showUpdateForm(currentInfo);
                if (formUpdate == null)
                    return;

                formUpdate.addCancelHandler(new UpdateFormCancelHandler());
                formUpdate.addSubmitHandler(new UpdateFormSubmitHandler(formUpdate));
                formUpdate.setPreferences(entryAddPresenter.getPreferences());
            }
        });

        // PERMISSIONS (handlers for adding read/write)
        final PermissionsPresenter pPresenter = display.getPermissionsWidget();
        pPresenter.setReadAddSelectionHandler(new PermissionReadBoxHandler(false));
        pPresenter.setWriteAddSelectionHandler(new PermissionReadBoxHandler(true));

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
    }

    public void setCurrentContext(EntryContext context) {
        this.currentContext = context;
        // todo : clear all data that is currently being displayed
    }

    public void showCurrentEntryView() {
        setContextNavData();
        retrieveEntryDetails();
    }

    public void showCreateEntry(EntryAddType type) {
        IEntryFormSubmit newForm = entryAddPresenter.getEntryForm(type, new NewFormCancelHandler());
        this.formSubmit = newForm;
        display.showNewForm(newForm);
        display.setEntryHeader(newForm.getHeaderDisplay(), "", ClientController.account.getFullName(),
                               ClientController.account.getEmail(), (new Date(System.currentTimeMillis())));
        display.getPermissionsWidget().setCanEdit(true);
        currentInfo = newForm.getEntry();
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

        EntryInfo info = nav.getCachedData(this.currentContext.getId(), this.currentContext.getRecordId());
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
                EntryInfo currentInfo = nav.getCachedData(EntryPresenter.this.currentInfo.getId(),
                                                          EntryPresenter.this.currentInfo.getRecordId());
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

                EntryInfo nextInfo = nav.getNext(currentInfo);
                long currentId = nextInfo.getId();
                History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + currentId, false);
//                EntryPresenter.this.currentInfo = nextInfo;
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
                EntryInfo currentInfo = nav.getCachedData(currentContext.getId(), currentContext.getRecordId());
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

                EntryInfo prevInfo = nav.getPrev(currentInfo);

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

    private Delegate<Long> retrieveEntryTraceSequenceDetailsDelegate() {
        return new Delegate<Long>() {
            @Override
            public void execute(final Long aLong) {
                if (currentInfo.getId() == 0) {
//                    display.setSequenceData(result, currentInfo);
//                    display.getMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                    // TODO :
                    return;
                }

                //To change body of implemented methods use File | Settings | File Templates.
                new IceAsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

                    @Override
                    protected void callService(AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback)
                            throws AuthenticationException {
                        service.retrieveEntryTraceSequences(ClientController.sessionId, aLong.longValue(), callback);
                    }

                    @Override
                    public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                        display.setSequenceData(result, currentInfo);
                        display.getMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                    }
                }.go(eventBus);
            }
        };
    }

    private void retrieveEntryDetails() {
        if (currentContext == null)
            return;

        display.showLoadingIndicator(false);

        new IceAsyncCallback<EntryInfo>() {

            @Override
            protected void callService(AsyncCallback<EntryInfo> callback) throws AuthenticationException {
                service.retrieveEntryDetails(ClientController.sessionId, currentContext.getId(),
                                             currentContext.getRecordId(), currentContext.getPartnerUrl(), callback);
            }

            @Override
            public void onSuccess(EntryInfo result) {
                if (result == null) {
                    FeedbackEvent event = new FeedbackEvent(true, "Error retrieving entry with id \""
                            + currentContext.getId() + "\"");
                    eventBus.fireEvent(event);
                    return;
                }

                currentInfo = result;
                currentContext.setId(currentInfo.getId());
                currentContext.setRecordId(currentInfo.getRecordId());

                // permission (order is important here)
                ServiceDelegate<SampleInfo> delegate = model.createDeleteSampleHandler();
                SequenceViewPanelPresenter sequencePresenter = display.setEntryInfoForView(currentInfo, delegate);
                display.getPermissionsWidget().setPermissionData(result.getPermissions(), new DeletePermission());
                UploadPasteSequenceHandler handler = new UploadPasteSequenceHandler(service,
                                                                                    eventBus, sequencePresenter);
                sequencePresenter.addSequencePasteHandler(handler);

                SequenceFileUploadHandler uploadHandler = new SequenceFileUploadHandler(sequencePresenter);
                sequencePresenter.addSequenceFileUploadHandler(uploadHandler);
                Menu menu = display.getMenu().getCurrentSelection();
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

    public EntryInfo getCurrentInfo() {
        return currentInfo;
    }

    public void setDeleteHandler(final DeleteEntryHandler deleteEntryHandler) {
        display.addDeleteEntryHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (!Window.confirm("Confirm deletion of entry " + currentInfo.getPartId()))
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
                if (currentInfo.getId() == 0 && (currentContext == null || currentContext.getPartnerUrl() == null)) {
                    display.showNewForm(formSubmit);
                    return;
                }
                display.showEntryDetailView();
                break;

            case SEQ_ANALYSIS:
                display.showSequenceView(currentInfo);
                break;

            case SAMPLES:
                display.showSampleView();
                break;
        }
    }

    //
    // inner classes
    //
    private class PermissionReadBoxHandler extends ReadBoxSelectionHandler {

        private final boolean isWrite;

        public PermissionReadBoxHandler(boolean isWrite) {
            this.isWrite = isWrite;
        }

        @Override
        public void updatePermission(final PermissionInfo info) {
            if (isWrite) {
                info.setType(PermissionInfo.Type.WRITE_ENTRY);
            } else
                info.setType(PermissionInfo.Type.READ_ENTRY);

            if (currentInfo.getId() == 0) {
                currentInfo.getPermissions().add(info);
                displayPermission(info);
                return;
            }

            info.setTypeId(currentInfo.getId());
            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.addPermission(ClientController.sessionId, info, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (result.booleanValue())
                        displayPermission(info);
                }
            }.go(eventBus);
        }

        protected void displayPermission(PermissionInfo info) {
            DeletePermission deletePermission = new DeletePermission();
            if (isWrite) {
                display.getPermissionsWidget().addWriteItem(info, deletePermission);
            } else {
                display.getPermissionsWidget().addReadItem(info, deletePermission);
            }
        }
    }

    private class DeletePermission implements Delegate<PermissionInfo> {

        @Override
        public void execute(final PermissionInfo info) {
            if (info.getTypeId() == 0) {
                currentInfo.getPermissions().remove(info);
                display.getPermissionsWidget().removeItem(info);
                return;
            }

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.removePermission(ClientController.sessionId, info, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    display.getPermissionsWidget().removeItem(info);
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

    private class NewFormCancelHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            History.fireCurrentHistoryState();
        }
    }

    private class UpdateFormCancelHandler implements ClickHandler {

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
        protected void update(final EntryInfo info) {
            if (info == null)
                return;

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.updateEntry(ClientController.sessionId, info, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (!result) {
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

    public class DeleteSequenceTraceHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final long entryId = currentInfo.getId();
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
                    display.setSequenceData(result, currentInfo);
                    display.getMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                }
            }.go(eventBus);
        }
    }
}
