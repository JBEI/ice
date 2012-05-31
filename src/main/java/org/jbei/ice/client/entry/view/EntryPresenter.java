package org.jbei.ice.client.entry.view;

import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.entry.view.detail.SequenceViewPanelPresenter;
import org.jbei.ice.client.entry.view.update.IEntryFormUpdateSubmit;
import org.jbei.ice.client.entry.view.view.AttachmentItem;
import org.jbei.ice.client.entry.view.view.DeleteSequenceHandler;
import org.jbei.ice.client.entry.view.view.EntryDetailViewMenu;
import org.jbei.ice.client.entry.view.view.EntryView;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem;
import org.jbei.ice.client.entry.view.view.MenuItem.Menu;
import org.jbei.ice.client.entry.view.view.PermissionsPresenter;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;
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

    public EntryPresenter(final RegistryServiceAsync service, final HandlerManager eventBus,
            EntryContext context) {
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
                IEntryFormUpdateSubmit formUpdate = display.showUpdateForm(currentInfo);
                if (formUpdate == null)
                    return;

                if (!formUpdate.hasCancelHandler())
                    formUpdate.addCancelHandler(new UpdateFormCancelHandler());

                if (!formUpdate.hasSubmitHandler())
                    formUpdate.addSubmitHandler(new UpdateFormSubmitHandler(formUpdate));
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
        display.setSequenceFinishUploadHandler(new SequenceUploaderFinishHandler());
    }

    /**
     * retrieves the existing permission for the current entry
     */
    private void retrievePermissionData() {
        service.retrievePermissionData(AppController.sessionId, this.currentContext.getCurrent(),
            new AsyncCallback<ArrayList<PermissionInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    display.getPermissionsWidget().onErrRetrievingExistingPermissions();
                }

                @Override
                public void onSuccess(ArrayList<PermissionInfo> result) {
                    if (result == null)
                        return;

                    display.getPermissionsWidget().setPermissionData(result, service,
                        currentContext.getCurrent());
                }
            });
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
                EntryInfo currentInfo = nav.getCachedData(currentContext.getCurrent()); // TODO : how is this current info different from EntryPresenter.this.currentInfo
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

                EntryInfo nextInfo = nav.getNext(currentInfo); // TODO : nextInfo can be null. look at the implementation of getNext for more info

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

    private void retrieveEntrySequenceDetails() {

        final long entryId = currentContext.getCurrent();
        service.retrieveEntryTraceSequences(AppController.sessionId, entryId,
            new AsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    FeedbackEvent event = new FeedbackEvent(true, "Error connecting to the server");
                    eventBus.fireEvent(event);
                }

                @Override
                public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                    if (result == null) {
                        FeedbackEvent event = new FeedbackEvent(true,
                                "Could not retrieve sequence trace files");
                        eventBus.fireEvent(event);
                        return;
                    }

                    display.setSequenceData(result, currentInfo);
                    display.getDetailMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                }
            });
    }

    private void retrieveEntryDetails() {

        display.showLoadingIndicator();

        final long entryId = currentContext.getCurrent();
        service.retrieveEntryDetails(AppController.sessionId, entryId,
            new AsyncCallback<EntryInfo>() {

                @Override
                public void onFailure(Throwable caught) {
                    FeedbackEvent event = new FeedbackEvent(true,
                            "There was an error retrieving the entry. Please try again later.");
                    eventBus.fireEvent(event);
                    // TODO : cancel loading indicator show
                }

                @Override
                public void onSuccess(EntryInfo result) {

                    if (result == null) {
                        FeedbackEvent event = new FeedbackEvent(true,
                                "Could not retrieve entry with id \"" + entryId + "\"");
                        eventBus.fireEvent(event);
                        return;
                    }

                    currentInfo = result;
                    String name = result.getType().getDisplay().toUpperCase() + ": "
                            + result.getName();
                    display.setEntryName(name);

                    // can user edit ?
                    boolean canEdit = (AppController.accountInfo.isModerator() || result
                            .isCanEdit());
                    display.getPermissionsWidget().setCanEdit(canEdit);
                    if (canEdit) {
                        display.setSequenceDeleteHandler(new DeleteSequenceTraceHandler());
                    }

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

                    display.setAttachments(items, entryId);
                    // menu views
                    display.getDetailMenu().updateMenuCount(Menu.SEQ_ANALYSIS,
                        result.getSequenceAnalysis().size());
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
                        sequencePresenter = display.showEntryDetailView(currentInfo, canEdit,
                            new DeleteSequenceHandler(service, entryId));
                        sequencePresenter.addFileUploadHandler(new UploadPasteSequenceHandler(
                                service, sequencePresenter));
                        break;

                    case SEQ_ANALYSIS:
                        boolean showFlash = (selection.getCount() > 0);
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
            service.addPermission(AppController.sessionId, id, info, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable caught) {
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (!result)
                        return;

                    if (isWrite) {
                        display.getPermissionsWidget().addWriteItem(info, service, id,
                            currentInfo.isCanEdit());
                    } else {
                        display.getPermissionsWidget().addReadItem(info, service, id,
                            currentInfo.isCanEdit());
                    }
                }
            });
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
                boolean canEdit = (AppController.accountInfo.isModerator() || currentInfo
                        .isCanEdit());
                sequencePresenter = display.showEntryDetailView(currentInfo, canEdit,
                    new DeleteSequenceHandler(service, currentInfo.getId()));
                sequencePresenter.addFileUploadHandler(new UploadPasteSequenceHandler(service,
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

        private final IEntryFormUpdateSubmit formSubmit;

        public UpdateFormSubmitHandler(IEntryFormUpdateSubmit formSubmit) {
            this.formSubmit = formSubmit;
        }

        @Override
        public void onClick(ClickEvent event) {
            FocusWidget focus = formSubmit.validateForm();
            if (focus != null) {
                focus.setFocus(true);
                FeedbackEvent feedback = new FeedbackEvent(true,
                        "Please fill out all required fields");
                eventBus.fireEvent(feedback);
                return;
            }

            formSubmit.populateEntry();
            update(formSubmit.getEntry());
        }

        /**
         * Makes an rpc to save the set of entrys
         * 
         * @param hasEntry
         *            set of entrys to be saved.
         */
        protected void update(final EntryInfo info) {
            if (info == null)
                return;

            service.updateEntry(AppController.sessionId, info, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable caught) {
                    eventBus.fireEvent(new FeedbackEvent(true, "Server error. Please try again."));
                }

                @Override
                public void onSuccess(Boolean success) {
                    if (!success) {
                        FeedbackEvent event = new FeedbackEvent(true,
                                "Your entry could not be updated.");
                        eventBus.fireEvent(event);
                    } else {
                        showCurrentEntryView();
                        FeedbackEvent event = new FeedbackEvent(false,
                                "Entry successfully updated.");
                        eventBus.fireEvent(event);
                        Window.scrollTo(0, 0);
                    }
                }
            });
        }
    }

    private class SequenceUploaderFinishHandler implements OnFinishUploaderHandler {

        @Override
        public void onFinish(IUploader uploader) {
            if (uploader.getStatus() == Status.SUCCESS) {
                UploadedInfo info = uploader.getServerInfo();
                //                    uploader.reset();
                //                    uploadPanel.setVisible(false);
                retrieveEntrySequenceDetails();
            } else {
                UploadedInfo info = uploader.getServerInfo();
                if (uploader.getStatus() == Status.ERROR) {
                    Window.alert("There was a problem uploading your file.\n\nPlease contact your administrator if this problem persists");
                }
            }
            uploader.reset();
            display.setSequenceFormVisibility(false);
        }
    }

    // TODO : this can be moved to external file
    public class DeleteSequenceTraceHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final long entryId = currentInfo.getId();
            Set<SequenceAnalysisInfo> selected = display.getSequenceTableSelectionModel()
                    .getSelectedSet();
            if (selected == null || selected.isEmpty())
                return;

            ArrayList<String> fileIds = new ArrayList<String>();
            for (SequenceAnalysisInfo info : selected) {
                fileIds.add(info.getFileId());
            }

            service.deleteEntryTraceSequences(AppController.sessionId, entryId, fileIds,
                new AsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

                    @Override
                    public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                        if (result == null) {
                            Window.alert("There was a problem deleting the sequence file(s). \n\nPlease contact your administrator if this problem persists");
                            return;
                        }

                        display.setSequenceData(result, currentInfo);
                        display.getDetailMenu().updateMenuCount(Menu.SEQ_ANALYSIS, result.size());
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not delete trace sequence file");
                    }
                });
        }
    }
}
