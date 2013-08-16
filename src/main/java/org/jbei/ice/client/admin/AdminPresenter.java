package org.jbei.ice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.group.GroupPresenter;
import org.jbei.ice.client.admin.part.AdminTransferredPartPresenter;
import org.jbei.ice.client.admin.setting.SystemSettingPresenter;
import org.jbei.ice.client.admin.user.UserPresenter;
import org.jbei.ice.client.admin.web.WebOfRegistriesPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.AccountResults;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Presenter for the admin page
 *
 * @author Hector Plahar
 */
public class AdminPresenter extends AbstractPresenter {

    private final AdminView view;
    private AdminOption currentOption;
    private GroupPresenter groupPresenter;
    private UserPresenter userPresenter;
    private SystemSettingPresenter systemSettingPresenter;
    private AdminTransferredPartPresenter partPresenter;
    private WebOfRegistriesPresenter webPresenter;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view, String optionStr) {
        super(service, eventBus);
        this.view = view;
        AdminOption option = AdminOption.urlToOption(optionStr);
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
                if (systemSettingPresenter == null)
                    systemSettingPresenter = new SystemSettingPresenter(service, eventBus);
                retrieveSystemSettings();
                break;

            case WEB:
                if (webPresenter == null)
                    webPresenter = new WebOfRegistriesPresenter(service, eventBus);
                retrieveWebOfRegistryPartners();
                break;

            case GROUPS:
                if (groupPresenter == null)
                    groupPresenter = new GroupPresenter(service, eventBus);
                retrieveGroups();
                break;

            case USERS:
                if (userPresenter == null)
                    userPresenter = new UserPresenter(service, eventBus);
                retrieveUsers();
                break;

            case PARTS:
                if (partPresenter == null)
                    partPresenter = new AdminTransferredPartPresenter(service, eventBus);
                retrievePendingTransfers();
                break;
        }
    }

    // GROUPS
    private void retrieveGroups() {
        new IceAsyncCallback<ArrayList<UserGroup>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<UserGroup>> callback) throws AuthenticationException {
                service.retrieveGroups(ClientController.sessionId, GroupType.PUBLIC, callback);
            }

            @Override
            public void onSuccess(ArrayList<UserGroup> result) {
                if (result == null || currentOption != AdminOption.GROUPS)
                    return;

                groupPresenter.setGroups(result);
                view.show(currentOption, groupPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    // SYSTEMS
    private void retrieveSystemSettings() {
        new IceAsyncCallback<HashMap<String, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<String, String>> callback) throws AuthenticationException {
                service.retrieveSystemSettings(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(HashMap<String, String> settings) {
                if (settings == null || currentOption != AdminOption.SETTINGS)
                    return;

                systemSettingPresenter.setData(settings);
                view.show(currentOption, systemSettingPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    // WEB OF REGISTRIES
    private void retrieveWebOfRegistryPartners() {
        new IceAsyncCallback<WebOfRegistries>() {

            @Override
            protected void callService(AsyncCallback<WebOfRegistries> callback) throws AuthenticationException {
                service.retrieveWebOfRegistryPartners(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(WebOfRegistries partners) {
                if (partners == null || currentOption != AdminOption.WEB)
                    return;

                webPresenter.setData(partners);
                view.show(currentOption, webPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    // USERS
    private void retrieveUsers() {
        new IceAsyncCallback<AccountResults>() {

            @Override
            protected void callService(AsyncCallback<AccountResults> callback) throws AuthenticationException {
                service.retrieveAllUserAccounts(ClientController.sessionId, 0, 30, callback);
            }

            @Override
            public void onSuccess(AccountResults result) {
                if (result == null || currentOption != AdminOption.USERS)
                    return;

                userPresenter.setData(result);
                view.show(currentOption, userPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    // PENDING TRANSFERS
    private void retrievePendingTransfers() {
        new IceAsyncCallback<ArrayList<PartData>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<PartData>> callback) throws AuthenticationException {
                service.retrieveTransferredParts(ClientController.sessionId, callback);
            }

            @Override
            public void onSuccess(ArrayList<PartData> result) {
                if (currentOption != AdminOption.PARTS)
                    return;

                partPresenter.setData(result);
                view.show(currentOption, partPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
