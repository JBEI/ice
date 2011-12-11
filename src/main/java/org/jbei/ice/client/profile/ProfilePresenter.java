package org.jbei.ice.client.profile;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.SamplesDataProvider;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.EntryDataTable;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.ProfileInfo;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class ProfilePresenter extends AbstractPresenter {

    private final String sid = AppController.sessionId;
    private final EntryDataViewDataProvider provider; // entries tab view data provider
    private final SamplesDataProvider samplesDataProvider;

    public interface Display {

        Widget asWidget();

        void setContents(Widget widget);

        CellList<CellEntry> getMenu();

        void setHeaderText(String text);

        HasEntryDataTable<SampleInfo> getSamplesTable();

        EntryDataTable<EntryInfo> getEntryDataTable();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;
    private final SingleSelectionModel<CellEntry> selectionModel;
    private Widget accountWidget;

    public ProfilePresenter(final RegistryServiceAsync service, HandlerManager eventBus,
            final Display display, final String userId) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        // selection model
        selectionModel = new SingleSelectionModel<CellEntry>();
        selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                CellEntry selected = selectionModel.getSelectedObject();
                switch (selected.getType()) {
                default:
                case ABOUT:
                    display.setContents(accountWidget);
                    break;
                case ENTRIES:
                    display.setContents(display.getEntryDataTable());
                    break;
                case SAMPLES:
                    display.setContents(display.getSamplesTable());
                    break;
                }
            }
        });

        this.display.getMenu().setSelectionModel(selectionModel);
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
                accountWidget = new AboutWidget(info);

                // set menu
                ArrayList<CellEntry> menu = new ArrayList<CellEntry>();
                CellEntry about = new CellEntry(MenuType.ABOUT, -1);
                menu.add(about);
                menu.add(new CellEntry(MenuType.ENTRIES, info.getUserEntryCount()));
                menu.add(new CellEntry(MenuType.SAMPLES, info.getUserSampleCount()));
                display.getMenu().setRowData(menu);
                selectionModel.setSelected(about, true);

                // set data
                provider.setValues(profileInfo.getUserEntries());
                samplesDataProvider.setValues(profileInfo.getUserSamples());
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve account info for user : " + userId);
            }
        });

        provider = new EntryDataViewDataProvider(display.getEntryDataTable(), service);
        samplesDataProvider = new SamplesDataProvider(display.getSamplesTable(), service);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
