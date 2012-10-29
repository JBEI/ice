package org.jbei.ice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.group.EditGroupsPanel;
import org.jbei.ice.client.admin.transfer.TransferEntryPanel;
import org.jbei.ice.client.admin.user.UserTable;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Presenter for the admin page
 *
 * @author Hector Plahar
 */
public class AdminPresenter extends AbstractPresenter {

    private final AdminView view;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private AdminOption currentOption;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view, String optionStr) {
        this.service = service;
        this.view = view;
        this.eventBus = eventBus;

        AdminOption option = AdminOption.urlToOption(optionStr);
        if (option == null)
            option = AdminOption.SETTINGS;

        view.showMenuSelection(option);
        setViewForOption(option);
        setSelectionHandler();
    }

    protected void setSelectionHandler() {
        this.view.getUserSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                setViewForOption(view.getUserSelectionModel().getSelectedObject());
            }
        });
    }

    private void setViewForOption(AdminOption option) {
        if (option == null)
            return;

        currentOption = option;

        switch (option) {
            case SETTINGS:
                retrieveSystemSettings();
                break;

            case GROUPS:
                retrieveGroups();
                break;

            case USERS:
                retrieveUsers();
                break;

            case TRANSFER:
                currentOption = AdminOption.TRANSFER;
                view.show(currentOption, new TransferEntryPanel());
                break;
        }
    }

    // GROUPS
    private void retrieveGroups() {
        new IceAsyncCallback<ArrayList<GroupInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<GroupInfo>> callback) throws AuthenticationException {
                service.retrieveAllGroups(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<GroupInfo> result) {
                if (result == null || currentOption != AdminOption.GROUPS)
                    return;

                EditGroupsPanel panel = new EditGroupsPanel();
                panel.setGroups(result);
//                ListDataProvider<GroupInfo> dataProvider = new ListDataProvider<GroupInfo>();
//                dataProvider.addDataDisplay(panel.getDataPanel());
//                dataProvider.getList().addAll(result);
                view.show(currentOption, panel);
            }
        }.go(eventBus);
    }

    // SYSTEMS
    private void retrieveSystemSettings() {
        new IceAsyncCallback<HashMap<String, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<String, String>> callback) throws AuthenticationException {
                service.retrieveSystemSettings(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(HashMap<String, String> profileInfo) {
                if (profileInfo == null || currentOption != AdminOption.SETTINGS)
                    return;

                view.show(currentOption, new HTML("&nbsp;"));
            }
        }.go(eventBus);
    }

    private void retrieveUsers() {
        new IceAsyncCallback<ArrayList<AccountInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<AccountInfo>> callback) throws AuthenticationException {
                service.retrieveAllUserAccounts(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<AccountInfo> result) {
                if (result == null || currentOption != AdminOption.USERS)
                    return;

                // move most to admin
                UserTable table = new UserTable();
                ListDataProvider<AccountInfo> dataProvider = new ListDataProvider<AccountInfo>();
                dataProvider.setList(result);
                dataProvider.addDataDisplay(table);

                VerticalPanel vPanel = new VerticalPanel();
                vPanel.setWidth("100%");
                vPanel.add(table);
                SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
                SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER, pagerResources, false, 0, true);
                pager.setDisplay(table);
                vPanel.add(pager);
                vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);
                view.show(currentOption, vPanel);
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
