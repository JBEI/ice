package org.jbei.ice.client.entry.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.SequenceTable;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.client.entry.view.view.EntryDetailViewMenu;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem;
import org.jbei.ice.client.entry.view.view.MenuItem.Menu;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;

public class EntryPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;
    private EntryDetailView<? extends EntryInfo> view;
    private final EntrySampleTable sampleTable;
    private final SequenceTable sequenceTable;

    public EntryPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final IEntryView display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        sequenceTable = new SequenceTable();
        sampleTable = new EntrySampleTable();
    }

    public EntryPresenter(final RegistryServiceAsync service, HandlerManager eventBus,
            final IEntryView display, String entryId) {
        this(service, eventBus, display);

        final long id = Long.decode(entryId); // TODO : catch potential NFE
        retrieveEntryDetails(id);

        // add handler for the permission link
        display.getDetailMenu().getPermissionLink().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                //                retrievePermissionData(id); TODO : RequestCycle.get() causes a failure on the server
                display.showPermissionsWidget();
            }
        });

        MenuSelectionHandler handler = new MenuSelectionHandler(display.getDetailMenu());
        display.getDetailMenu().addClickHandler(handler);

        retrieveAccountsAndGroups();
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

                    view = ViewFactory.createDetailView(result);
                    String name = result.getType().getDisplay().toUpperCase() + ": "
                            + result.getName();
                    display.setEntryName(name);

                    // attachments
                    ArrayList<AttachmentInfo> attachments = result.getAttachments();
                    if (attachments != null) {
                        display.getAttachmentList().setRowData(attachments);
                    }

                    // menu views
                    ArrayList<SampleStorage> data = new ArrayList<SampleStorage>();
                    for (SampleInfo sampleInfo : result.getSampleMap().keySet()) {
                        SampleStorage datum = new SampleStorage(sampleInfo, result.getSampleMap()
                                .get(sampleInfo));
                        data.add(datum);
                    }

                    sampleTable.setData(data);
                    sequenceTable.setData(result.getSequenceAnalysis());

                    // menu 
                    ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                    menuItems.add(new MenuItem(Menu.GENERAL, -1));
                    menuItems.add(new MenuItem(Menu.SEQ_ANALYSIS, result.getSequenceAnalysis()
                            .size()));
                    menuItems.add(new MenuItem(Menu.SAMPLES, result.getSampleMap().size()));
                    display.setMenuItems(menuItems); // TODO : set menu loading indicator?

                    Button editButton = display.showEntryDetailView(view);
                    editButton.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            UpdateEntryForm<? extends EntryInfo> form = ViewFactory
                                    .getUpdateForm(view.getInfo());
                            if (form == null)
                                return;

                            display.showUpdateForm(form);
                        }
                    });
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
            switch (menu.getCurrentSelection().getMenu()) {

            case GENERAL: // TODO : need to add this only once not every time the selection changes
                Button editButton = display.showEntryDetailView(view);
                editButton.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        UpdateEntryForm<? extends EntryInfo> form = ViewFactory.getUpdateForm(view
                                .getInfo());
                        if (form == null)
                            return;

                        display.showUpdateForm(form);
                    }
                });
                break;

            case SEQ_ANALYSIS:
                Flash.Parameters params = new Flash.Parameters();
                params.setSwfPath("static/sc/SequenceChecker.swf");
                params.setSessiondId(AppController.sessionId);
                params.setEntryId(view.getInfo().getRecordId());

                Button button = display.showSequenceView(sequenceTable, new Flash(params));
                if (button != null) {
                    button.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            //                                view.switchToEditMode();
                        }
                    });
                }
                break;

            case SAMPLES:
                Button addSample = display.showSampleView(sampleTable);
                addSample.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        display.getSampleForm().setVisible(!display.getSampleForm().isVisible());
                    }
                });
                break;
            }
        }
    }
}
