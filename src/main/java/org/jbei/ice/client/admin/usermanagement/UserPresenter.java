package org.jbei.ice.client.admin.usermanagement;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

public class UserPresenter implements AdminPanelPresenter<AccountInfo> {

    private final AdminPanel<AccountInfo> view;

    private ListDataProvider<AccountInfo> dataProvider = new ListDataProvider<AccountInfo>();

    public UserPresenter() {
        this.view = new EditUserPanel();
    }

    private void retrieveAllUsers(final RegistryServiceAsync service, HandlerManager eventBus) {
        new IceAsyncCallback<ArrayList<AccountInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback) {
                try {
                    service.retrieveAllUserAccounts(AppController.sessionId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<AccountInfo> result) {
                if (result == null)
                    return;

                dataProvider.getList().clear();
                dataProvider.getList().addAll(result);
            }
        }.go(eventBus);
    }

    @Override
    public void go(RegistryServiceAsync service, HandlerManager eventBus) {
        if (dataProvider.getDataDisplays().contains(view.getDisplay()))
            return;
        dataProvider.addDataDisplay(view.getDisplay());

        retrieveAllUsers(service, eventBus);
    }

    @Override
    public AdminPanel<AccountInfo> getView() {
        return this.view;
    }

    @Override
    public int getTabIndex() {
        return this.view.getTab().ordinal();
    }
}
