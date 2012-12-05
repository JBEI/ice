package org.jbei.ice.client.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.group.GroupPresenter;
import org.jbei.ice.client.admin.setting.SystemSettingPresenter;
import org.jbei.ice.client.admin.transfer.TransferEntryPanel;
import org.jbei.ice.client.admin.user.UserPresenter;
import org.jbei.ice.client.admin.web.WebOfRegistriesPresenter;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.group.GroupInfo;

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
    private WebOfRegistriesPresenter webPresenter;

    public AdminPresenter(RegistryServiceAsync service, HandlerManager eventBus, AdminView view, String optionStr) {
        super(service, eventBus);
        this.view = view;
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
                if (systemSettingPresenter == null)
                    systemSettingPresenter = new SystemSettingPresenter(service, eventBus);
                retrieveSystemSettings();
                break;

            case WEB:
                if (webPresenter == null)
                    webPresenter = new WebOfRegistriesPresenter(service, eventBus);
                retrieveWebOfRegistriesSettings();
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

            case TRANSFER:
                view.show(currentOption, new TransferEntryPanel());
                break;
        }
    }

    // GROUPS
    private void retrieveGroups() {
        new IceAsyncCallback<GroupInfo>() {

            @Override
            protected void callService(AsyncCallback<GroupInfo> callback) throws AuthenticationException {
                service.retrieveGroup(AppController.sessionId, null, callback);
            }

            @Override
            public void onSuccess(GroupInfo result) {
                if (result == null || currentOption != AdminOption.GROUPS)
                    return;

                groupPresenter.setRootGroup(result);
                view.show(currentOption, groupPresenter.getView().asWidget());
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
            public void onSuccess(HashMap<String, String> settings) {
                if (settings == null || currentOption != AdminOption.SETTINGS)
                    return;

                systemSettingPresenter.setData(settings);
                view.show(currentOption, systemSettingPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    private void retrieveWebOfRegistriesSettings() {
        new IceAsyncCallback<HashMap<String, String>>() {

            @Override
            protected void callService(AsyncCallback<HashMap<String, String>> callback) throws AuthenticationException {
                service.retrieveSystemSettings(AppController.sessionId, callback);
            }

            @Override
            public void onSuccess(HashMap<String, String> settings) {
                if (settings == null || currentOption != AdminOption.WEB)
                    return;

                webPresenter.setData(settings);
                view.show(currentOption, webPresenter.getView().asWidget());
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

                userPresenter.setData(result);
                view.show(currentOption, userPresenter.getView().asWidget());
            }
        }.go(eventBus);
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
