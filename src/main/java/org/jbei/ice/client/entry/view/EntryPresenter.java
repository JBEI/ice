package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter.DeleteEntryHandler;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.view.AttachmentItem;
import org.jbei.ice.client.entry.view.view.DeleteSequenceHandler;
import org.jbei.ice.client.entry.view.view.EntryDetailViewMenu;
import org.jbei.ice.client.entry.view.view.EntryView;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem;
import org.jbei.ice.client.entry.view.view.MenuItem.Menu;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter;
import org.jbei.ice.client.entry.view.view.SequenceFileUploadHandler;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
import org.jbei.ice.shared.dto.Visibility;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;

/**
 * Presenter for entry view
 *
 * @author Hector Plahar
 */
public class EntryPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;
    private EntryInfo currentInfo;
    private EntryContext currentContext;
    private SequenceViewPanelPresenter sequencePresenter;

    private final EntryModel model;

    public EntryPresenter(final RegistryServiceAsync service, final HandlerManager eventBus, EntryContext context) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = new EntryView();
        this.currentContext = context;

        this.model = new EntryModel(service, this.display, eventBus);

        addEntryViewHandler();
        display.getDetailMenu().addSelectionChangeHandler(
                new MenuSelectionHandler(display.getDetailMenu()));

        setContextNavHandlers();
        showCurrentEntryView();

        // trace sequence upload handler
        display.setTraceSequenceStartUploader(new TraceSequenceStartUploaderHandler());

        // SAMPLE
        // add sample button handler
        display.addSampleButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (currentInfo == null)
                    return; // TODO : show some error msg or wait till it is not null

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

                formUpdate.getCancel().addClickHandler(new UpdateFormCancelHandler());
                formUpdate.getSubmit().addClickHandler(new UpdateFormSubmitHandler(formUpdate));
            }
        });

        // SEQUENCE
        display.addSequenceAddButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                boolean visible = display.getSequenceFormVisibility();
                display.setSequenceFormVisibility(!visible);
            }
        });

        // PERMISSIONS (handlers for adding read/write)
        final PermissionsPresenter pPresenter = display.getPermissionsWidget();
        pPresenter.setReadAddSelectionHandler(new PermissionReadBoxHandler(false));
        pPresenter.setWriteAddSelectionHandler(new PermissionReadBoxHandler(true));

        // sequence upload handler
        display.setSequenceFinishUploadHandler(new TraceSequenceUploaderFinishHandler());

        // attachment delete handler
        display.setAttachmentDeleteHandler(new HasAttachmentDeleteHandler() {

            @Override
            public void deleteAttachment(final AttachmentItem item) {
                new IceAsyncCallback<Boolean>() {

                    @Override
                    protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                        service.deleteEntryAttachment(AppController.sessionId, item.getFileId(), callback);
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
    }

    //    public void setDeleteEntryHandler(new  DeleteEntryHandler() ) {
    //     // delete handler
    //        display.addDeleteEntryHandler(new ClickHandler() {
    //
    //            @Override
    //            public void onClick(ClickEvent event) {
    //                if (!Window.confirm("Confirm deletion of entry " + currentInfo.getPartId()))
    //                    return;
    //
    //                deleteCurrentEntry();
    //            }
    //        });
    //       
    //    }

    /**
     * retrieves the existing permission for the current entry
     */
    private void retrievePermissionData() {
        new IceAsyncCallback<ArrayList<PermissionInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<PermissionInfo>> callback)
                    throws AuthenticationException {
                service.retrievePermissionData(AppController.sessionId, currentContext.getCurrent(), callback);
            }

            @Override
            public void onSuccess(ArrayList<PermissionInfo> result) {
                display.getPermissionsWidget().setPermissionData(result, service,
                                                                 currentContext.getCurrent());
            }
        }.go(eventBus);
    }

    private void showCurrentEntryView() {
        setContextNavData();
        retrieveEntryDetails();
    }

    public void setCurrentContext(EntryContext context) {
        this.currentContext = context;
        showCurrentEntryView();
    }

    private void addEntryViewHandler() {
        eventBus.addHandler(EntryViewEvent.TYPE, new EntryViewEventHandler() {

            @Override
            public void onEntryView(EntryViewEvent event) {
                if (event != null && event.getContext() != null)
                    currentContext = event.getContext();
                showCurrentEntryView();
            }
        });
    }

    protected void setContextNavData() {
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

        EntryInfo info = nav.getCachedData(this.currentContext.getCurrent());
        // TODO : info == null ?
        int idx = nav.indexOfCached(info);

        display.enablePrev(!(idx == 0));
        boolean atEnd = ((idx + 1) == size);
        display.enableNext(!atEnd);

        String text = (idx + 1) + " of " + size;
        display.setNavText(text);
    }

    private void setContextNavHandlers() {

        // menu 
        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(Menu.GENERAL, -1));
        menuItems.add(new MenuItem(Menu.SEQ_ANALYSIS, 0));
        menuItems.add(new MenuItem(Menu.SAMPLES, 0));
        display.setMenuItems(menuItems);
        display.getDetailMenu().setSelection(Menu.GENERAL);

        display.setNextHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                IHasNavigableData nav = currentContext.getNav();
                EntryInfo currentInfo = nav.getCachedData(currentContext
                                                                  .getCurrent()); // TODO : how is this current info
                // different from EntryPresenter.this.currentInfo
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

                EntryInfo nextInfo = nav.getNext(
                        currentInfo); // TODO : nextInfo can be null. look at the implementation of getNext for more
                // info

                // TODO :this needs to be folded into a single "Retrieve"
                long currentId = nextInfo.getId();
                History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + currentId, false);
                EntryPresenter.this.currentInfo = nextInfo;
                currentContext.setCurrent(currentId);
                retrieveEntryDetails();
                display.enablePrev(true);
                String text = (next + 1) + " of " + size;
                display.setNavText(text);
            }
        });

        // add previous handler
        display.setPrevHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                IHasNavigableData nav = currentContext.getNav();
                EntryInfo currentInfo = nav.getCachedData(currentContext.getCurrent());
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
                currentContext.setCurrent(currentId);
                retrieveEntryDetails();
                display.enableNext(true);
                String text = (prev + 1) + " of " + nav.getSize();
                display.setNavText(text);
            }
        });

        // add go back handler
        // TODO : this can be improved to show the current position of the viewed entry in the list
        display.setGoBackHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new ShowEntryListEvent(currentContext));
            }
        });
    }

    private void retrieveEntryTraceSequenceDetails() {

        final long entryId = currentContext.getCurrent();
        new IceAsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback)
                    throws AuthenticationException {
                service.retrieveEntryTraceSequences(AppController.sessionId, entryId, callback);
            }

            @Override
            public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                display.setSequenceData(result, currentInfo);
                display.getDetailMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
            }
        }.go(eventBus);
    }

    private void retrieveEntryDetails() {

        display.showLoadingIndicator();

        final long entryId = currentContext.getCurrent();
        service.retrieveEntryDetails(AppController.sessionId, entryId,
                                     new AsyncCallback<EntryInfo>() {

                                         @Override
                                         public void onFailure(Throwable caught) {
                                             FeedbackEvent event = new FeedbackEvent(true,
                                                                                     "There was an error retrieving " +
                                                                                             "the entry. Please try " +
                                                                                             "again later.");
                                             eventBus.fireEvent(event);
                                             // TODO : cancel loading indicator show
                                             // display.showLoadingIndicator(false);
                                         }

                                         @Override
                                         public void onSuccess(EntryInfo result) {

                                             if (result == null) {
                                                 FeedbackEvent event = new FeedbackEvent(true,
                                                                                         "Could not retrieve entry " +
                                                                                                 "with id \"" +
                                                                                                 entryId + "\"");
                                                 eventBus.fireEvent(event);
                                                 return;
                                             }

                                             currentInfo = result;
                                             currentContext.setCurrent(currentInfo.getId());
                                             String name = result.getType().getDisplay().toUpperCase() + ": "
                                                     + result.getName();
                                             display.setEntryName(name);

                                             // can user edit ?
                                             boolean canEdit = (AppController.accountInfo.isAdmin() || result
                                                     .isCanEdit());
                                             display.getPermissionsWidget().
                                                     setCanEdit(result.getVisibility() == Visibility.OK);
                                             if (canEdit) {
                                                 display.setSequenceDeleteHandler(new DeleteSequenceTraceHandler());
                                             }

                                             // visibility
                                             display.getVisibilityWidget().setVisibility(result.getVisibility());


                                             // attachments
                                             ArrayList<AttachmentInfo> attachments = result.getAttachments();
                                             ArrayList<AttachmentItem> items = new ArrayList<AttachmentItem>();
                                             if (attachments != null) {
                                                 for (AttachmentInfo info : attachments) {
                                                     AttachmentItem item = new AttachmentItem(info.getId(), info
                                                             .getFilename(), info.getDescription());
                                                     item.setFileId(info.getFileId());
                                                     items.add(item);
                                                 }
                                             }

                                             display.setAttachments(items, currentInfo.getId());

                                             // menu views
                                             display.getDetailMenu().updateMenuCount(Menu.SEQ_ANALYSIS,
                                                                                     result.getSequenceAnalysis()
                                                                                           .size());
                                             display.getDetailMenu().updateMenuCount(Menu.SAMPLES,
                                                                                     result.getSampleStorage().size());

                                             display.setSampleData(result.getSampleStorage());
                                             display.setSequenceData(result.getSequenceAnalysis(), result);

                                             MenuItem selection = display.getDetailMenu().getCurrentSelection();
                                             Menu menu;
                                             if (selection == null) {
                                                 menu = Menu.GENERAL;
                                             } else {
                                                 menu = selection.getMenu();
                                             }

                                             switch (menu) {

                                                 case GENERAL:
                                                     sequencePresenter = display.showEntryDetailView(
                                                             currentInfo,
                                                             canEdit,
                                                             new DeleteSequenceHandler(
                                                                     service,
                                                                     eventBus,
                                                                     entryId));
                                                     sequencePresenter.addSequencePasteHandler(
                                                             new UploadPasteSequenceHandler(
                                                                     service, sequencePresenter));
                                                     sequencePresenter
                                                             .addSequenceFileUploadHandler(
                                                                     new SequenceFileUploadHandler(
                                                                             sequencePresenter));
                                                     break;


                                                 case SEQ_ANALYSIS:
                                                     boolean showFlash = (selection != null && selection
                                                             .getCount() > 0);
                                                     display.showSequenceView(currentInfo, showFlash);
                                                     break;

                                                 case SAMPLES:
                                                     display.showSampleView();
                                                     break;
                                             }

                                             // retrieve associated permission
                                             retrievePermissionData();
                                         }
                                     });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    public Widget getView() {
        return this.display.asWidget();
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

    //
    // inner classes
    //

    private class PermissionReadBoxHandler extends ReadBoxSelectionHandler {

        private final boolean isWrite;

        public PermissionReadBoxHandler(boolean isWrite) {
            this.isWrite = isWrite;
        }

        @Override
        void updatePermission(final PermissionInfo info, PermissionType permissionType) {

            final long id = currentInfo.getId();
            if (isWrite) {
                switch (info.getType()) {
                    case READ_ACCOUNT:
                        info.setType(PermissionType.WRITE_ACCOUNT);
                        break;
                    case READ_GROUP:
                        info.setType(PermissionType.WRITE_GROUP);
                        break;
                }
            }

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.addPermission(AppController.sessionId, id, info, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (isWrite) {
                        display.getPermissionsWidget().addWriteItem(info, service, id, currentInfo.isCanEdit());
                    } else {
                        display.getPermissionsWidget().addReadItem(info, service, id, currentInfo.isCanEdit());
                    }
                }
            }.go(eventBus);
        }
    }

    /*Handler for the entry detail menu*/
    public class MenuSelectionHandler implements SelectionChangeEvent.Handler {

        private final EntryDetailViewMenu menu;
        private MenuItem selection;

        public MenuSelectionHandler(EntryDetailViewMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
            if (selection == menu.getCurrentSelection() || menu.getCurrentSelection() == null)
                return;

            selection = menu.getCurrentSelection();
            menu.setSelection(selection.getMenu());

            switch (selection.getMenu()) {

                case GENERAL:
                    boolean canEdit = (AppController.accountInfo.isAdmin() || currentInfo.isCanEdit());
                    sequencePresenter = display.showEntryDetailView(currentInfo, canEdit,
                                                                    new DeleteSequenceHandler(service, eventBus,
                                                                                              currentInfo.getId()));
                    sequencePresenter.addSequencePasteHandler(new UploadPasteSequenceHandler(service,
                                                                                             sequencePresenter));
                    sequencePresenter.addSequenceFileUploadHandler(new SequenceFileUploadHandler(
                            sequencePresenter));
                    break;

                case SEQ_ANALYSIS:
                    boolean showFlash = (menu.getCurrentSelection().getCount() > 0);
                    display.showSequenceView(currentInfo, showFlash);
                    break;

                case SAMPLES:
                    display.showSampleView();
                    break;
            }
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
                    service.updateEntry(AppController.sessionId, info, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (!result) {
                        FeedbackEvent event = new FeedbackEvent(true, "Your entry could not be updated.");
                        eventBus.fireEvent(event);
                    } else {
                        showCurrentEntryView();
                        FeedbackEvent event = new FeedbackEvent(false, "Entry successfully updated.");
                        eventBus.fireEvent(event);
                        Window.scrollTo(0, 0);
                    }
                }
            }.go(eventBus);
        }
    }

    public class TraceSequenceStartUploaderHandler implements IUploader.OnStartUploaderHandler {

        public void onStart(IUploader uploader) {
            String servletPath = "servlet.gupld?eid=" + currentInfo.getId()
                    + "&type=sequence&sid=" + AppController.sessionId;
            uploader.setServletPath(servletPath);
        }
    }

    private class TraceSequenceUploaderFinishHandler implements OnFinishUploaderHandler {

        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == Status.SUCCESS) {
                UploadedInfo info = uploader.getServerInfo();
                //                    uploader.reset();
                //                    uploadPanel.setVisibility(false);
                retrieveEntryTraceSequenceDetails();
            } else {
                UploadedInfo info = uploader.getServerInfo();
                if (uploader.getStatus() == Status.ERROR) {
                    Window.alert(
                            "There was a problem uploading your file.\n\nPlease contact your administrator if this " +
                                    "problem persists");
                }
            }
            uploader.reset();
            display.setSequenceFormVisibility(false);
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
                    service.deleteEntryTraceSequences(AppController.sessionId, entryId, fileIds, callback);
                }

                @Override
                public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                    display.setSequenceData(result, currentInfo);
                    display.getDetailMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                }
            }.go(eventBus);
        }
    }
}
