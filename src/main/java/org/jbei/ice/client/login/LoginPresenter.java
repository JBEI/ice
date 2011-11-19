package org.jbei.ice.client.login;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Submits user entered login credentials for validation
 * 
 * @author Hector Plahar
 */

public class LoginPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final ILoginView display;

    public LoginPresenter(RegistryServiceAsync service, HandlerManager eventBus, ILoginView display) {
        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        setHandler();
    }

    protected void login() {
        this.display.clearLoginNameError();
        this.display.clearLoginPassError();

        String loginName = this.display.getLoginName();
        String loginPass = this.display.getLoginPass();

        if (loginName == null || loginName.isEmpty()) {
            this.display.setLoginNameError("Username is required and cannot be left empty!");
            return;
        }

        if (loginPass == null || loginPass.isEmpty()) {
            this.display.setLoginPassError("Password is required and cannot be left empty!");
            return;
        }

        // client validation passed. attempt to login
        Utils.showWaitCursor(null);
        service.login(loginName, loginPass, new AsyncCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo result) {
                resetCursor();
                if (result == null) {
                    display.setLoginPassError("The username and/or password you entered is incorrect!");
                    return;
                }

                eventBus.fireEvent(new LoginEvent(result, display.rememberUserOnComputer()));
            }

            @Override
            public void onFailure(Throwable caught) {
                resetCursor();
                display.setLoginPassError("There was an error validating your account. Please try again.");
            }
        });
    }

    private void resetCursor() {
        Utils.showDefaultCursor(null);
    }

    protected final void setHandler() {
        SubmitHandler handler = new SubmitHandler();

        this.display.setSubmitHandler(handler);
        this.display.getSubmitButton().addKeyPressHandler(handler);
        this.display.getSubmitButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                login();
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    public class SubmitHandler implements KeyPressHandler {

        @Override
        public void onKeyPress(KeyPressEvent event) {
            if (event.getCharCode() != KeyCodes.KEY_ENTER)
                return;

            login();
        }
    }
}
