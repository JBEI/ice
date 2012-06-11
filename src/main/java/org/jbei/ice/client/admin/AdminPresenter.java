package org.jbei.ice.client.admin;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.group.GroupPresenter;
import org.jbei.ice.client.admin.usermanagement.UserPresenter;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Presenter for the admin page
 * 
 * @author Hector Plahar
 */
public class AdminPresenter extends AbstractPresenter {

    private final AdminView view;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private UserPresenter userPresenter;
    private GroupPresenter groupPresenter;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view) {
        this.service = service;
        this.view = view;
        this.eventBus = eventBus;

        retrieveSavedDrafts();
        setMenuSelectionModel();
        addSelectionChangeHandler();

        // initialize presenters
        groupPresenter = new GroupPresenter(service);
    }

    /**
     * Adds a tab selection change handler to the view
     */
    private void addSelectionChangeHandler() {

        this.view.addLayoutHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {

                switch (event.getSelectedItem()) {

                case 1:
                    if (userPresenter == null)
                        userPresenter = new UserPresenter(service);
                    view.setTabPresenter(1, userPresenter);
                    break;

                case 2:
                    view.setTabPresenter(2, groupPresenter);
                    break;

                default:
                    return;
                }
            }
        });
    }

    private void retrieveSavedDrafts() {
        service.retrieveDraftsPendingVerification(AppController.sessionId,
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
