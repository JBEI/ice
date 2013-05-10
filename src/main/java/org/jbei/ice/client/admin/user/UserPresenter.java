package org.jbei.ice.client.admin.user;

import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountResults;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Admin presenter for managing users
 *
 * @author Hector Plahar
 */
public class UserPresenter extends AdminPanelPresenter {

    private final UserPanel view;
    private final UserDataProvider provider;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;

    public UserPresenter(final RegistryServiceAsync service, final HandlerManager eventBus) {
        super(service, eventBus);
        this.view = new UserPanel();
        this.provider = new UserDataProvider(view.getUserTable(), service);
        this.service = service;
        this.eventBus = eventBus;
        setRegistrationHandler();
    }

    protected void setRegistrationHandler() {
        this.view.setRegistrationHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AccountInfo newUser = view.getNewUserDetails();
                if (newUser == null || newUser.getEmail().trim().isEmpty())
                    return;

                new IceAsyncCallback<AccountInfo>() {

                    @Override
                    protected void callService(AsyncCallback<AccountInfo> callback) throws AuthenticationException {
                        service.retrieveAccount(newUser.getEmail().trim(), callback);
                    }

                    @Override
                    public void onSuccess(AccountInfo result) {
                        view.informOfDuplicateRegistrationEmail();
                    }

                    @Override
                    public void onNullResult() {
                        saveNewAccount(newUser);
                    }
                }.go(eventBus);
            }
        });
    }

    protected void saveNewAccount(final AccountInfo newUser) {
        new IceAsyncCallback<String>() {

            @Override
            protected void callService(AsyncCallback<String> callback) throws AuthenticationException {
                service.createNewAccount(newUser, false, callback);
            }

            @Override
            public void onSuccess(String result) {
                view.showResult(result);
            }

            @Override
            public void onNullResult() {
                view.showResult(null);
            }
        }.go(eventBus);
    }

    public void setData(AccountResults data) {
        provider.setResultsData(data, true);
    }

    @Override
    public IAdminPanel getView() {
        return this.view;
    }
}
