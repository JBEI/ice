package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.view.AttachmentItem;
import org.jbei.ice.client.entry.view.view.EntryDetailViewMenu;
import org.jbei.ice.client.entry.view.view.EntryView;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem;
import org.jbei.ice.client.entry.view.view.MenuItem.Menu;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class EntryPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;
    private EntryInfo currentInfo;
    private List<Long> contextList;
    private long currentId;

    public EntryPresenter(final RegistryServiceAsync service, HandlerManager eventBus,
            EntryContext context) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = new EntryView();

        // add handler for the permission link
        //        display.getDetailMenu().getPermissionLink().addClickHandler(new ClickHandler() {
        //
        //            @Override
        //            public void onClick(ClickEvent event) {
        //                retrievePermissionData(currentId);
        //                display.showPermissionsWidget();
        //            }
        //        });

        addEntryViewHandler();
        MenuSelectionHandler handler = new MenuSelectionHandler(display.getDetailMenu());
        display.getDetailMenu().addClickHandler(handler);
        setContextNavHandlers();

        showEntryView(context);

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
                display.showUpdateForm(currentInfo);
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

    private void addEntryViewHandler() {
        eventBus.addHandler(EntryViewEvent.TYPE, new EntryViewEventHandler() {

            @Override
            public void onEntryView(EntryViewEvent event) {
                showEntryView(event.getContext());
            }
        });
    }

    private void showEntryView(EntryContext context) {
        this.contextList = context.getList();
        if (contextList != null)
            Collections.reverse(this.contextList); // TODO : order matters. make sure this is the case in all

        currentId = context.getCurrent();
        retrieveAccountsAndGroups();
        setContextNavData();
        retrieveEntryDetails(currentId);
    }

    protected void setContextNavData() {
        boolean show = (contextList != null);
        display.showContextNav(show);
        if (!show) {
            display.enableNext(false);
            display.enablePrev(false);
            return;
        }

        if (contextList.isEmpty() || contextList.size() == 1) {
            display.enableNext(false);
            display.enablePrev(false);
        }

        int idx = contextList.indexOf(currentId);
        display.enablePrev(!(idx == 0));
        boolean atEnd = ((idx + 1) == contextList.size());
        display.enableNext(!atEnd);

        String text = (idx + 1) + " of " + contextList.size();
        display.setNavText(text);
    }

    private void setContextNavHandlers() {
        display.setNextHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int idx = contextList.indexOf(currentId);
                if (idx == -1) {
                    display.enableNext(false);
                    display.enablePrev(false);
                    return; // we have a problem. most likely means we did not disable next at the right time
                }

                int next = idx + 1;

                if (contextList.size() == next + 1) {
                    display.enableNext(false);
                }

                // TODO :this needs to be folded into a single "Retrieve"
                currentId = contextList.get(next);
                retrieveEntryDetails(currentId);
                retrieveAccountsAndGroups();
                //                retrievePermissionData(contextList.get(idx + 1));
                display.enablePrev(true);
                String text = (next + 1) + " of " + contextList.size();
                display.setNavText(text);
            }
        });

        // add previous handler
        display.setPrevHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int idx = contextList.indexOf(currentId);
                if (idx == -1) {
                    display.enableNext(false);
                    display.enablePrev(false);
                    return; // we have a problem. most likely means we did not disable prev at the right time
                }

                int prev = idx - 1;
                if (prev <= 0) { // at the first position {
                    display.enablePrev(false);
                }

                currentId = contextList.get(prev);
                retrieveEntryDetails(currentId);
                retrieveAccountsAndGroups();
                //                retrievePermissionData(contextList.get(idx - 1));
                display.enableNext(true);
                String text = (prev + 1) + " of " + contextList.size();
                display.setNavText(text);
            }
        });

        // add go back handler
        // TODO : this can be improved to show the current position of the viewed entry in the list
        display.setGoBackHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                History.back();
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

    private void retrieveEntryDetails(long entryId) {
        currentId = entryId;
        service.retrieveEntryDetails(AppController.sessionId, entryId,
            new AsyncCallback<EntryInfo>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Failed to retrieve entry details: " + caught.getMessage());
                }

                @Override
                public void onSuccess(EntryInfo result) {

                    if (result == null) {
                        // TODO : how to deal with error messages
                        Window.alert("There was an error retrieving the entry. Please try again later");
                        return;
                    }

                    currentInfo = result;
                    String name = result.getType().getDisplay().toUpperCase() + ": "
                            + result.getName();
                    display.setEntryName(name);

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

                    display.setAttachments(items, currentId);
                    // menu views
                    ArrayList<SampleStorage> data = new ArrayList<SampleStorage>();
                    for (SampleInfo sampleInfo : result.getSampleMap().keySet()) {
                        SampleStorage datum = new SampleStorage(sampleInfo, result.getSampleMap()
                                .get(sampleInfo));
                        data.add(datum);
                    }

                    display.setSampleData(data);
                    display.setSequenceData(result.getSequenceAnalysis());

                    // menu 
                    ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                    menuItems.add(new MenuItem(Menu.GENERAL, -1));
                    menuItems.add(new MenuItem(Menu.SEQ_ANALYSIS, result.getSequenceAnalysis()
                            .size()));
                    menuItems.add(new MenuItem(Menu.SAMPLES, result.getSampleMap().size()));
                    display.setMenuItems(menuItems); // TODO : set menu loading indicator?
                    display.showEntryDetailView(currentInfo);
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
                display.showEntryDetailView(currentInfo);
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

    public Widget getView() {
        return this.display.asWidget();
    }
}
