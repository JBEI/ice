package org.jbei.ice.client.login;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.util.Utils;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Presenter for the login page
 * Submits user entered login credentials to the server for validation
 *
 * @author Hector Plahar
 */

public class LoginPresenter extends AbstractPresenter {

    private final ILoginView display;

    public LoginPresenter(RegistryServiceAsync service, HandlerManager eventBus, ILoginView display) {
        super(service, eventBus);
        this.display = display;

        determineCanChangePassword();
        determineCanRegister();
        setHandler();
    }

    protected void determineCanChangePassword() {
        service.getConfigurationSetting("PASSWORD_CHANGE_ALLOWED", new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (!"yes".equalsIgnoreCase(result) && !"true".equalsIgnoreCase(result))
                    return;

                display.addForgotPasswordLinkHandler(new ForgotPasswordHandler());
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    protected void determineCanRegister() {
        service.getConfigurationSetting("NEW_REGISTRATION_ALLOWED", new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (!"yes".equalsIgnoreCase(result) && !"true".equalsIgnoreCase(result))
                    return;

                display.addRegisterHandler(new RegisterHandler());
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    protected void login() {
        this.display.clearErrorMessages();
        this.display.setInputFieldsEnable(false);

        String loginName = this.display.getLoginName();
        String loginPass = this.display.getLoginPass();
        boolean error = false;

        if (loginName == null || loginName.isEmpty()) {
            this.display.setLoginNameError("Username is required");
            error = true;
        }

        if (loginPass == null || loginPass.isEmpty()) {
            this.display.setLoginPassError("Password is required");
            error = true;
        }

        if (error) {
            display.setInputFieldsEnable(true);
            return;
        }

        // client validation passed. attempt to login
        Utils.showWaitCursor(null);
        service.login(loginName, loginPass, new AsyncCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo result) {

                if (result == null) {
                    display.setLoginPassError("Invalid username and/or password!");
                    enableInputFields();
                    return;
                }

                eventBus.fireEvent(new LoginEvent(result, display.rememberUserOnComputer()));
                enableInputFields();
            }

            @Override
            public void onFailure(Throwable caught) {
                display.setLoginPassError("Could not retrieve authentication details.");
                enableInputFields();
            }

            private void enableInputFields() {
                display.setInputFieldsEnable(true);
                resetCursor();
            }
        });
    }

    private void resetCursor() {
        Utils.showDefaultCursor(null);
    }

    protected final void setHandler() {
        SubmitHandler handler = new SubmitHandler();

        this.display.setSubmitKeyPressHandler(handler);
        this.display.setLoginHandler(new ClickHandler() {

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

    private void createNewAccount(final RegistrationDetails details) {
        service.retrieveAccount(details.getEmail(), new AsyncCallback<AccountInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not connect to the server. Please try again later");
            }

            @Override
            public void onSuccess(AccountInfo result) {
                if (result != null) {
                    display.informOfDuplicateRegistrationEmail();
                } else {
                    saveNewAccount(details);
                }
            }
        });
    }

    /**
     * actual creation of account when it has been determined that the unique aspect of the account
     * details (email) will remain unique
     *
     * @param details user registration information
     */
    private void saveNewAccount(RegistrationDetails details) {
        AccountInfo info = new AccountInfo();
// TODO : user ID

        info.setEmail(details.getEmail());
        info.setDescription(details.getAbout());
        info.setFirstName(details.getFirstName());
        info.setLastName(details.getLastName());
        info.setInitials(details.getInitials());
        info.setInstitution(details.getInstitution());

        service.createNewAccount(info, true, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(
                        "There was an error communicating with the server.\n\nPlease contact the site administrator " +
                                "if you repeatedly see this message");
            }

            @Override
            public void onSuccess(String result) {
                Window.alert("Registration successful. \n\nPlease check your email for\n your login credentials");
                display.switchToLoginMode();
            }
        });
    }

    // inner classes

    private class ForgotPasswordHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            display.switchToForgotPasswordMode();
            display.setResetPasswordHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    attemptToSendUserPassword();
                }
            });
        }
    }

    private void attemptToSendUserPassword() {
        final String login = display.getForgotPasswordLogin();
        if (login.isEmpty())
            return;

        service.retrieveAccount(login, new AsyncCallback<AccountInfo>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not retrieve user account");
            }

            @Override
            public void onSuccess(AccountInfo result) {
                if (result == null) {
                    Window.alert("Could not retrieve user account");
                    return;
                }

                generateNewPasswordAndSend(login);
            }
        });
    }

    private void generateNewPasswordAndSend(final String email) {
        final String url = GWT.getHostPageBaseURL() + "#" + Page.PROFILE.getLink();

        new IceAsyncCallback<Boolean>() {

            @Override
            protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                service.handleForgotPassword(email, url, callback);
            }

            @Override
            public void onSuccess(Boolean result) {
                if (result.booleanValue()) {
                    Window.alert("A new password has been emailed to you");
                    display.switchToLoginMode();
                }
            }
        }.go(eventBus);
    }

    private class RegisterHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            display.switchToRegisterMode(new CreateAccountHandler(), new CancelCreateAccountHandler());
        }
    }

    private class CreateAccountHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            createNewAccount(display.getRegistrationDetails());
        }
    }

    private class CancelCreateAccountHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            display.switchToLoginMode();
        }
    }

    public class SubmitHandler implements KeyPressHandler, ClickHandler {

        @Override
        public void onKeyPress(KeyPressEvent event) {
            if (event.getNativeEvent().getKeyCode() != KeyCodes.KEY_ENTER)
                return;

            login();
        }

        @Override
        public void onClick(ClickEvent event) {
            login();
        }
    }
}
