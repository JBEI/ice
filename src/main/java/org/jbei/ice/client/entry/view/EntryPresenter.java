package org.jbei.ice.client.entry.view;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.widget.Flash;
import org.jbei.ice.client.entry.view.detail.EntryDetailView;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.table.EntrySampleTable;
import org.jbei.ice.client.entry.view.table.SequenceTable;
import org.jbei.ice.client.entry.view.update.UpdateEntryForm;
import org.jbei.ice.client.entry.view.view.IEntryView;
import org.jbei.ice.client.entry.view.view.MenuItem;
import org.jbei.ice.client.entry.view.view.MenuItem.Menu;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class EntryPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;
    private EntryDetailView<? extends EntryInfo> view;
    private final EntrySampleTable sampleTable;
    private final SequenceTable sequenceTable;
    private final MenuItem generalMenuItem;

    //    private EntryInfo info;

    public EntryPresenter(RegistryServiceAsync service, HandlerManager eventBus, IEntryView display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        generalMenuItem = new MenuItem(Menu.GENERAL, -1);
        sequenceTable = new SequenceTable();
        sampleTable = new EntrySampleTable();

        //        addEditHandler();
    }

    public EntryPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final IEntryView display, String entryId) {
        this(service, eventBus, display);

        final long id = Long.decode(entryId); // TODO : catch NFE
        service.retrieveEntryDetails(AppController.sessionId, id, new AsyncCallback<EntryInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve entry details: " + caught.getMessage());
            }

            @Override
            public void onSuccess(EntryInfo result) {
                if (result == null) {
                    String errorMsg = "<p>Could not retrieve the entry requested. Please try again later.</p>";
                    // TODO : how to deal with error messages
                    //                    display.setEntryDetailView(new HTML(errorMsg));
                    return;
                }

                //                info = result;
                view = ViewFactory.createDetailView(result);
                String name = result.getType().getDisplay() + ": " + result.getName();
                display.setEntryName(name);

                // attachments
                ArrayList<AttachmentInfo> attachments = result.getAttachments();
                if (attachments != null) {
                    display.getAttachmentList().setRowData(attachments);
                }

                // menu views
                ArrayList<SampleStorage> data = new ArrayList<SampleStorage>();
                for (SampleInfo sampleInfo : result.getSampleMap().keySet()) {
                    SampleStorage datum = new SampleStorage(sampleInfo, result.getSampleMap().get(
                        sampleInfo));
                    data.add(datum);
                }
                sampleTable.setData(data);
                sequenceTable.setData(result.getSequenceAnalysis());

                // menu 
                ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
                menuItems.add(generalMenuItem);
                menuItems.add(new MenuItem(Menu.SEQ_ANALYSIS, result.getSequenceAnalysis().size()));
                menuItems.add(new MenuItem(Menu.SAMPLES, result.getSampleMap().size()));
                display.getMenu().setRowData(menuItems); // TODO : set menu loading indicator
                display.getMenu().setSelectionModel(new MenuSelectionModel());
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
    class MenuSelectionModel extends SingleSelectionModel<MenuItem> {
        public MenuSelectionModel() {
            super();

            this.setSelected(generalMenuItem, true);
            this.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    MenuItem selected = MenuSelectionModel.this.getSelectedObject();
                    switch (selected.getMenu()) {
                    case GENERAL: // TODO : need to add this only once not everytime the selection changes
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
                        display.showSampleView(sampleTable);
                        break;
                    }
                }
            });
        }
    }
}
