package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

public class GroupPresenter implements AdminPanelPresenter<GroupInfo> {

    private final RegistryServiceAsync service;
    private ListDataProvider<GroupInfo> dataProvider = new ListDataProvider<GroupInfo>();

    public GroupPresenter(RegistryServiceAsync service) {
        this.service = service;
        retrieveAllGroups();
    }

    private void retrieveAllGroups() {
        try {
            service.retrieveAllGroups(AppController.sessionId,
                new AsyncCallback<ArrayList<GroupInfo>>() {

                    @Override
                    public void onSuccess(ArrayList<GroupInfo> result) {
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
    public void go(AdminPanel<GroupInfo> container) {
        if (dataProvider.getDataDisplays().contains(container.getDisplay()))
            return;
        dataProvider.addDataDisplay(container.getDisplay());
    }
}
