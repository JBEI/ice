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

public class UserPresenter extends AdminPanelPresenter {

    private final UserPanel view;
    private ListDataProvider<AccountInfo> dataProvider = new ListDataProvider<AccountInfo>();

    public UserPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        this.view = new UserPanel();
    }

    private void retrieveAllUsers() {
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

    public void setData(ArrayList<AccountInfo> data) {
        this.view.setData(data);
    }

    @Override
    public AdminPanel getView() {
        return this.view;
    }
}
