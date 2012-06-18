package org.jbei.ice.client.admin.usermanagement;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

public class UserPresenter implements AdminPanelPresenter {

    private final RegistryServiceAsync service;
    private ListDataProvider<AccountInfo> dataProvider = new ListDataProvider<AccountInfo>();

    public UserPresenter(RegistryServiceAsync service) {
        this.service = service;
        retrieveAllUsers();
    }

    private void retrieveAllUsers() {
        try {
            service.retrieveAllUserAccounts(AppController.sessionId,
                new AsyncCallback<ArrayList<AccountInfo>>() {

                    @Override
                    public void onSuccess(ArrayList<AccountInfo> result) {
                        if (result == null)
                            return;

                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                    }
                });
        } catch (org.jbei.ice.client.exception.AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void go(AdminPanel container) {
        if (dataProvider.getDataDisplays().contains(container.getDisplay()))
            return;
        dataProvider.addDataDisplay(container.getDisplay());
    }
}
