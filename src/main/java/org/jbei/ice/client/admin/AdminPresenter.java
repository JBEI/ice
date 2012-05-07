package org.jbei.ice.client.admin;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class AdminPresenter extends AbstractPresenter {

    private final AdminView view;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view) {
        this.service = service;
        this.view = view;
        this.eventBus = eventBus;

        retrieveSavedDrafts();
        setMenuSelectionModel();
    }

    private void retrieveSavedDrafts() {
        service.retrieveImportDraftData(AppController.sessionId,
            AppController.accountInfo.getEmail(), // TODO : retrieve all instead of current user only
            new AsyncCallback<ArrayList<BulkImportDraftInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error retrieving saved drafts");
                }

                @Override
                public void onSuccess(ArrayList<BulkImportDraftInfo> result) {
                    ArrayList<MenuItem> data = new ArrayList<MenuItem>();
                    for (BulkImportDraftInfo info : result) {
                        String name = info.getName();
                        if (name == null) {
                            name = DateUtilities.formatDate(info.getCreated());
                            info.setName(name);
                        }
                        MenuItem item = new MenuItem(info.getId(), name, info.getCount(), true);
                        data.add(item);
                    }

                    if (!data.isEmpty()) {
                        view.setSavedDraftsData(data);
                    }
                }
            });
    }

    private void setMenuSelectionModel() {
        final SingleSelectionModel<MenuItem> draftSelection = view.getDraftMenuModel();
        draftSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final MenuItem item = draftSelection.getSelectedObject();

                service.retrieveBulkImport(AppController.sessionId, item.getId(),
                    new AsyncCallback<BulkImportDraftInfo>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Could not retrieve your saved drafts.");
                        }

                        @Override
                        public void onSuccess(BulkImportDraftInfo result) {
                            if (result == null) {
                                Window.alert("Could not retrieve your saved drafts.");
                                return;
                            }

                            view.setSheet(result, false);
                        }
                    });
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
