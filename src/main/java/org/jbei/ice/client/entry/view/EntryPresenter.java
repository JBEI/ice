package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
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
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.event.FeedbackEvent;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
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

    public EntryPresenter(final RegistryServiceAsync service, HandlerManager eventBus,
            EntryContext context) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = new EntryView();
        this.currentContext = context;

        // add handler for the permission link
        //                display.getDetailMenu().getPermissionLink().addClickHandler(new ClickHandler() {
        //        
        //                    @Override
        //                    public void onClick(ClickEvent event) {
        //                        retrievePermissionData(currentId);
        //                        display.showPermissionsWidget();
        //                    }
        //                });

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
                boolean visible = display.getSampleFormVisibility();
                display.setSampleFormVisibility(!visible);
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
    }

    private void showCurrentEntryView() {
        retrieveAccountsAndGroups();
        setContextNavData();
        retrieveEntryDetails();
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
                EntryInfo currentInfo = nav.getCachedData(currentContext.getCurrent());
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

                // TODO :this needs to be folded into a single "Retrieve"
                long currentId = nextInfo.getId();
                currentContext.setCurrent(currentId);
                retrieveEntryDetails();
                retrieveAccountsAndGroups();
                //                retrievePermissionData(contextList.get(idx + 1));
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
                //                retrievePermissionData(contextList.get(idx - 1));
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
        service.retrieveAllAccounts(AppController.sessionId,
            new AsyncCallback<LinkedHashMap<Long, String>>() {

                @Override
                public void onSuccess(LinkedHashMap<Long, String> result) {
                    display.getPermissionsWidget().setAccountData(result);
                }

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            });

        service.retrieveAllGroups(AppController.sessionId,
            new AsyncCallback<LinkedHashMap<Long, String>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }

                @Override
                public void onSuccess(LinkedHashMap<Long, String> result) {
                    display.getPermissionsWidget().setGroupData(result);
                }
            });
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
                        FeedbackEvent event = new FeedbackEvent(true,
                                "System returned null entry. Please try again later.");
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
                    ArrayList<SampleStorage> data = new ArrayList<SampleStorage>();
                    for (SampleInfo sampleInfo : result.getSampleMap().keySet()) {
                        SampleStorage datum = new SampleStorage(sampleInfo, result.getSampleMap()
                                .get(sampleInfo));
                        data.add(datum);
                    }

                    display.setSampleData(data);
                    display.setSequenceData(result.getSequenceAnalysis(), entryId);

                    // menu 
                    ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                    menuItems.add(new MenuItem(Menu.GENERAL, -1));
                    menuItems.add(new MenuItem(Menu.SEQ_ANALYSIS, result.getSequenceAnalysis()
                            .size()));
                    menuItems.add(new MenuItem(Menu.SAMPLES, result.getSampleMap().size()));
                    display.setMenuItems(menuItems); // TODO : set menu loading indicator?
                    display.showEntryDetailView(currentInfo, canEdit);
                }
            });
    }

    private void retrievePermissionData(final long entryId) {

        // retrieve data for permission
        service.retrievePermissionData(AppController.sessionId, entryId,
            new AsyncCallback<ArrayList<PermissionInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error occured retrieving permissions: " + caught.getMessage()); // TODO 
                }

                @Override
                public void onSuccess(ArrayList<PermissionInfo> result) {
                    display.getPermissionsWidget().setExistingPermissions(result);
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
