package org.jbei.ice.client.admin;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.bulkimport.BulkImportMenuItem;
import org.jbei.ice.client.admin.bulkimport.DeleteBulkImportHandler;
import org.jbei.ice.client.admin.group.GroupPresenter;
import org.jbei.ice.client.admin.usermanagement.UserPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import java.util.ArrayList;

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

        if (!AppController.accountInfo.isModerator())
            History.newItem(Page.MAIN.getLink());

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
        new IceAsyncCallback<ArrayList<BulkImportDraftInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<BulkImportDraftInfo>> callback) {
                try {
                    service.retrieveDraftsPendingVerification(AppController.sessionId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<BulkImportDraftInfo> result) {
                ArrayList<BulkImportMenuItem> data = new
                        ArrayList<BulkImportMenuItem>();
                for (BulkImportDraftInfo info : result) {
                    String name = info.getName();
                    String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                    BulkImportMenuItem item = new BulkImportMenuItem(
                            info.getId(),
                            name,
                            info.getCount(),
                            dateTime,
                            info.getType().toString(),
                            info.getAccount().getEmail());
                    data.add(item);
                }

                if (!data.isEmpty()) {
                    view.setSavedDraftsData(data, new DeleteBulkImportHandler(service, eventBus));
                }
            }
        }.go(eventBus);
    }

    private void setMenuSelectionModel() {
        final SingleSelectionModel<BulkImportMenuItem> draftSelection = view.getDraftMenuModel();
        draftSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final BulkImportMenuItem item = draftSelection.getSelectedObject();

                new IceAsyncCallback<BulkImportDraftInfo>() {

                    @Override
                    protected void callService(AsyncCallback<BulkImportDraftInfo> callback) {
                        try {
                            service.retrieveBulkImport(AppController.sessionId, item.getId(), callback);
                        } catch (AuthenticationException e) {
                            History.newItem(Page.LOGIN.getLink());
                        }
                    }

                    @Override
                    public void onSuccess(BulkImportDraftInfo result) {
                        view.setSheet(result, false);
                    }
                }.go(eventBus);
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
