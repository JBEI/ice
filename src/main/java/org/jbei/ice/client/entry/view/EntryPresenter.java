package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.IHasNavigableData;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.update.IEntryFormUpdateSubmit;
import org.jbei.ice.client.entry.view.view.AttachmentItem;
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
import org.jbei.ice.shared.dto.EntryInfo.EntryType;
import org.jbei.ice.shared.dto.SampleInfo;
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

public class EntryPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;
    private EntryInfo currentInfo;
    private EntryContext currentContext;
    private final HashMap<EntryType, SampleLocation> cache;

    public EntryPresenter(final RegistryServiceAsync service, final HandlerManager eventBus,
            EntryContext context) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = new EntryView();
        this.currentContext = context;
        this.cache = new HashMap<EntryType, SampleLocation>();

        addEntryViewHandler();
        MenuSelectionHandler handler = new MenuSelectionHandler(display.getDetailMenu());
        display.getDetailMenu().addClickHandler(handler);
        setContextNavHandlers();

        showCurrentEntryView();

        // SAMPLE
        // add sample button handler
        display.addSampleButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (currentInfo == null)
                    return; // TODO : show some error msg or wait till it is not null

                SampleLocation cacheLocation = cache.get(currentInfo.getType());
                if (cacheLocation != null) {
                    display.setSampleOptions(cacheLocation);
                    display.setSampleFormVisibility(!display.getSampleFormVisibility());
                    return;
                }

                service.retrieveStorageSchemes(AppController.sessionId, currentInfo.getType(),
                    new AsyncCallback<HashMap<SampleInfo, ArrayList<String>>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            eventBus.fireEvent(new FeedbackEvent(true,
                                    "Failed to retrieve the sample location data."));
                        }

                        @Override
                        public void onSuccess(HashMap<SampleInfo, ArrayList<String>> result) {
                            if (result == null)
                                return;

                            SampleLocation sampleLocation = new SampleLocation(result);
                            cache.put(currentInfo.getType(), sampleLocation);
                            display.setSampleOptions(sampleLocation);
                            display.setSampleFormVisibility(!display.getSampleFormVisibility());
                            display.addSampleSaveHandler(new SampleAddHandler());
                        }
                    });
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

        // PERMISSIONS
        retrievePermissionData();
        PermissionsPresenter pPresenter = display.getPermissionsWidget();

        // TODO :both of these can be combined
        pPresenter.setReadAddSelectionHandler(new ReadBoxSelectionHandler() {

            @Override
            void updatePermission(final PermissionInfo info, PermissionType type) {
                switch (type) {
                case WRITE_ACCOUNT:
                    info.setType(PermissionType.READ_ACCOUNT);
                    break;

                case WRITE_GROUP:
                    info.setType(PermissionType.READ_GROUP);
                    break;
                }

                final long id = currentInfo.getId();
                service.addPermission(AppController.sessionId, id, info,
                    new AsyncCallback<Boolean>() {

                        @Override
                        public void onFailure(Throwable caught) {
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            if (result)
                                display.getPermissionsWidget().addReadItem(info, service, id);
                        }
                    });
            }
        });

        pPresenter.setWriteAddSelectionHandler(new ReadBoxSelectionHandler() {

            @Override
            void updatePermission(final PermissionInfo info, PermissionType type) {

                final long id = currentInfo.getId();
                switch (type) {
                case READ_ACCOUNT:
                    info.setType(PermissionType.WRITE_ACCOUNT);
                    break;
                case READ_GROUP:
                    info.setType(PermissionType.WRITE_GROUP);
                    break;
                }

                service.addPermission(AppController.sessionId, id, info,
                    new AsyncCallback<Boolean>() {

                        @Override
                        public void onFailure(Throwable caught) {
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            if (result)
                                display.getPermissionsWidget().addWriteItem(info, service, id);
                        }
                    });
            }
        });
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
        retrieveAccountsAndGroups();
        setContextNavData();
        retrieveEntryDetails();
        retrievePermissionData();
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
                retrieveAccountsAndGroups();
                retrievePermissionData();
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
                retrieveAccountsAndGroups();
                retrievePermissionData();
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

    private void retrieveAccountsAndGroups() {
        //        final PermissionsPresenter pPresenter = display.getPermissionsWidget();

        //        service.retrieveAllAccounts(AppController.sessionId,
        //            new AsyncCallback<LinkedHashMap<String, String>>() {
        //
        //                @Override
        //                public void onSuccess(LinkedHashMap<String, String> result) {
        //                    pPresenter.setAccountData(result);
        //                }
        //
        //                @Override
        //                public void onFailure(Throwable caught) {
        //                    Window.alert(caught.getMessage());
        //                }
        //            });
        //
        //        service.retrieveAllGroups(AppController.sessionId,
        //            new AsyncCallback<LinkedHashMap<Long, String>>() {
        //
        //                @Override
        //                public void onFailure(Throwable caught) {
        //                    Window.alert(caught.getMessage());
        //                }
        //
        //                @Override
        //                public void onSuccess(LinkedHashMap<Long, String> result) {
        //                    pPresenter.setGroupData(result);
        //                }
        //            });

        // handler for updating permissions
        //        addUpdatePermissionsHandler();
    }

    private void retrieveEntryDetails() {

        final long entryId = currentContext.getCurrent();
        service.retrieveEntryDetails(AppController.sessionId, entryId,
            new AsyncCallback<EntryInfo>() {

                @Override
                public void onFailure(Throwable caught) {
                    FeedbackEvent event = new FeedbackEvent(true,
                            "There was an error retrieving the entry. Please try again later.");
                    eventBus.fireEvent(event);
                }

                @Override
                public void onSuccess(EntryInfo result) {

                    if (result == null) {
                        FeedbackEvent event = new FeedbackEvent(true, "System returned null entry.");
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
                    display.setSampleData(result.getSampleStorage());
                    display.setSequenceData(result.getSequenceAnalysis(), entryId);

                    // menu 
                    ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                    menuItems.add(new MenuItem(Menu.GENERAL, -1));
                    menuItems.add(new MenuItem(Menu.SEQ_ANALYSIS, result.getSequenceAnalysis()
                            .size()));
                    menuItems.add(new MenuItem(Menu.SAMPLES, result.getSampleStorage().size()));
                    display.setMenuItems(menuItems); // TODO : set menu loading indicator?
                    display.showEntryDetailView(currentInfo, canEdit);
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
    public class MenuSelectionHandler implements ClickHandler {

        private final EntryDetailViewMenu menu;
        private MenuItem selection;

        public MenuSelectionHandler(EntryDetailViewMenu menu) {
            this.menu = menu;
        }

        @Override
        public void onClick(ClickEvent event) {
            if (selection == menu.getCurrentSelection())
                return;

            selection = menu.getCurrentSelection();
            menu.setSelection(selection.getMenu());

            switch (menu.getCurrentSelection().getMenu()) {

            case GENERAL:
                boolean canEdit = (AppController.accountInfo.isModerator() || currentInfo
                        .isCanEdit());
                display.showEntryDetailView(currentInfo, canEdit);
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

    private class SampleAddHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            SampleStorage sample = display.getSampleAddFormValues();
            if (sample == null)
                return;

            service.createSample(AppController.sessionId, sample, currentInfo.getId(),
                new AsyncCallback<SampleStorage>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        FeedbackEvent feedback = new FeedbackEvent(true, "Could not save sample");
                        eventBus.fireEvent(feedback);
                    }

                    @Override
                    public void onSuccess(SampleStorage result) {
                        if (result == null) {
                            FeedbackEvent feedback = new FeedbackEvent(true,
                                    "Could not save sample");
                            eventBus.fireEvent(feedback);
                            return;
                        }
                        display.setSampleFormVisibility(false);
                        currentInfo.getSampleStorage().add(result);
                        display.setSampleData(currentInfo.getSampleStorage());
                        // TODO : update counts and show the loading indicator when the sample is being created
                        // TODO : on click.
                    }
                });
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
                                "Your entry could not be updated. Please try again.");
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

}
