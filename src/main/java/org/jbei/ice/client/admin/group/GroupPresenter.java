package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;

public class GroupPresenter implements AdminPanelPresenter<GroupInfo> {

    private final AdminPanel<GroupInfo> view;
    private ListDataProvider<GroupInfo> dataProvider = new ListDataProvider<GroupInfo>();

    public GroupPresenter() {
        this.view = new EditGroupsPanel();
    }

    private void retrieveAllGroups(final RegistryServiceAsync service, HandlerManager eventBus) {

        new IceAsyncCallback<ArrayList<GroupInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<GroupInfo>> callback) {
                try {
                    service.retrieveAllGroups(AppController.sessionId, callback);
                } catch (AuthenticationException e) {
                    History.newItem(Page.LOGIN.getLink());
                }
            }

            @Override
            public void onSuccess(ArrayList<GroupInfo> result) {
                if (result == null)
                    return;

                dataProvider.getList().clear();
                dataProvider.getList().addAll(result);
            }
        }.go(eventBus);
    }

    @Override
    public void go(RegistryServiceAsync service, HandlerManager eventBus) {
        if (dataProvider.getDataDisplays().contains(this.view.getDisplay()))
            return;
        dataProvider.addDataDisplay(this.view.getDisplay());
        retrieveAllGroups(service, eventBus);
    }

    @Override
    public AdminPanel<GroupInfo> getView() {
        return this.view;
    }

    @Override
    public int getTabIndex() {
        return this.view.getTab().ordinal();
    }
}
