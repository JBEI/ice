package org.jbei.ice.client.profile;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.SamplesDataProvider;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.ProfileInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ProfilePresenter extends AbstractPresenter {

    private final String sid = AppController.sessionId;
    private final EntryDataViewDataProvider provider; // entries tab view data provider
    private final SamplesDataProvider samplesDataProvider;

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IProfileView display;
    private AboutWidget accountWidget;
    private CollectionDataTable table;
    private final VerticalPanel panel;

    public ProfilePresenter(final RegistryServiceAsync service, final HandlerManager eventBus,
            final IProfileView display, final String userId) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        accountWidget = new AboutWidget();

        this.display.getMenu().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                CellEntry selected = display.getMenu().getSelection();
                switch (selected.getType()) {
                default:
                case ABOUT:
                    display.setContents(accountWidget);
                    break;
                case ENTRIES:

                    display.setContents(panel);
                    break;
                case SAMPLES:
                    display.setContents(display.getSamplesTable());
                    break;
                }
            }
        });

        //        this.display.getMenu().setSelectionModel(selectionModel);
        this.service.retrieveProfileInfo(sid, userId, new AsyncCallback<ProfileInfo>() {

            @Override
            public void onSuccess(ProfileInfo profileInfo) {
                // TODO : some accounts do not have registered accounts and so need to check for that and disable link
                // TODO : e.g. filemaker 

                if (profileInfo == null) {
                    Label label = new Label(
                            "Could not retrieve user account information. Please try again.");
                    display.setContents(label);
                    return;
                }

                AccountInfo info = profileInfo.getAccountInfo();
                display.setHeaderText(info.getFirstName() + " " + info.getLastName());
                accountWidget.setAccountInfo(info);
                display.setContents(accountWidget);

                // set menu
                ArrayList<CellEntry> menu = new ArrayList<CellEntry>();
                CellEntry about = new CellEntry(MenuType.ABOUT, -1);
                menu.add(about);
                menu.add(new CellEntry(MenuType.ENTRIES, info.getUserEntryCount()));
                menu.add(new CellEntry(MenuType.SAMPLES, info.getUserSampleCount()));
                display.getMenu().setRowData(menu);

                // set data
                provider.setValues(profileInfo.getUserEntries());
                samplesDataProvider.setValues(profileInfo.getUserSamples());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve account info for user : " + userId);
            }
        });

        this.table = new CollectionDataTable(new EntryTablePager()) {

            @Override
            protected EntryViewEventHandler getHandler() {
                return new EntryViewEventHandler() {
                    @Override
                    public void onEntryView(EntryViewEvent event) {
                        event.setNavigable(provider);
                        eventBus.fireEvent(event);
                    }
                };
            }
        };
        panel = new VerticalPanel();
        panel.setWidth("100%");
        //                    entriesTable.addStyleName("gray_border");
        panel.add(table);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(table);
        panel.add(tablePager);

        provider = new EntryDataViewDataProvider(this.table, service);
        samplesDataProvider = new SamplesDataProvider(display.getSamplesTable(), service);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
