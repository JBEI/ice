package org.jbei.ice.client.admin.user;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

public class UserPresenter implements AdminPanelPresenter {

    private final AdminPanel view;

    private ListDataProvider<AccountInfo> dataProvider = new ListDataProvider<AccountInfo>();

    public UserPresenter() {
        this.view = new EditUserPanel();
    }

    private void retrieveAllUsers(final RegistryServiceAsync service, HandlerManager eventBus) {
        new IceAsyncCallback<ArrayList<AccountInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback) throws AuthenticationException {
                service.retrieveAllUserAccounts(AppController.sessionId, callback);
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
    public AdminPanel getView() {
        return this.view;
    }
}
