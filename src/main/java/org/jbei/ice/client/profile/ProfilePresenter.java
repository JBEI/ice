package org.jbei.ice.client.profile;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.SamplesDataProvider;
import org.jbei.ice.client.collection.SamplesDataTable;
import org.jbei.ice.client.component.EntryDataViewDataProvider;
import org.jbei.ice.client.component.table.DataTable;
import org.jbei.ice.shared.EntryData;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class ProfilePresenter extends Presenter {

    private final String sid = AppController.sessionId;
    private final EntryDataViewDataProvider provider; // entries tab view data provider
    private final SamplesDataProvider samplesDataProvider;
    private AccountInfo info;

    public interface Display {

        Widget asWidget();

        void setData(String name, String email, String since, String institution, String description);

        DataTable<EntryData> getEntriesDataView();

        SamplesDataTable getSamplesDataTable();

        void addEntryClickHandler(ClickHandler handler);

        void addSamplesClickHandler(ClickHandler handler);
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public ProfilePresenter(final RegistryServiceAsync service, HandlerManager eventBus,
            final Display display, final String userId) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        this.service.retrieveAccountInfo(sid, userId, new AsyncCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo info) {
                // TODO : some accounts do not have registered accounts and so need to check for that and disable link
                // TODO : e.g. filemaker 
                String fullName = info.getFirstName() + " " + info.getLastName();
                display.setData(fullName, info.getEmail(), info.getSince(), info.getInstitution(),
                    info.getDescription());
                ProfilePresenter.this.info = info;
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed to retrieve account info for user : " + userId);
            }
        });

        provider = new EntryDataViewDataProvider(display.getEntriesDataView(), service);

        // click handlers
        display.addEntryClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                service.retrieveUserEntries(sid, ProfilePresenter.this.info.getEmail(),
                    new AsyncCallback<ArrayList<Long>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(ArrayList<Long> result) {
                            if (result == null)
                                return;

                            provider.setValues(result);
                        }
                    });
            }
        });

        samplesDataProvider = new SamplesDataProvider(display.getSamplesDataTable(), service);

        display.addSamplesClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                service.retrieveSamplesByDepositor(sid, ProfilePresenter.this.info.getEmail(),
                    null, false, new AsyncCallback<LinkedList<Long>>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(LinkedList<Long> result) {
                            if (result == null)
                                return;

                            // TODO : need to get the sort info from the display table
                            samplesDataProvider.setValues(result);
                        }
                    });
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }
}
