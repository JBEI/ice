package org.jbei.ice.client.login;

import java.util.ArrayList;

import org.jbei.ice.client.FeedbackType;
import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * TODO : validation
 * 
 * @author hector
 */

public class LoginPresenter extends Presenter {

    public interface Display {

        HasClickHandlers getLoginButton();

        void setKeyPressHandler(KeyPressHandler handler);

        String getLoginName();

        String getLoginPass();

        boolean rememberUserOnComputer();

        ILogoutHandler getLogoutHandler();

        void setFeedback(ArrayList<String> msgs, FeedbackType type);

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    // TODO : check if session is still valid and re-direct user to the main page
    public LoginPresenter(RegistryServiceAsync service, HandlerManager eventBus, Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        bind();
    }

    protected final void bind() {

        HasClickHandlers loginBtn = this.display.getLoginButton();
        loginBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                login();
            }
        });

        display.setKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() == KeyCodes.KEY_ENTER)
                    login();
            }
        });
    }

    @Override
    public void go(HasWidgets container) {

        container.clear();
        container.add(this.display.asWidget());
    }

    // use the service to login and send event
    protected void login() {

        String loginName = this.display.getLoginName();
        String loginPass = this.display.getLoginPass();

        ArrayList<String> err = validate(loginName, loginPass);
        if (!err.isEmpty()) {
            this.display.setFeedback(err, FeedbackType.ERROR);
            return;
        }

        service.login(loginName, loginPass, new AsyncCallback<AccountInfo>() {

            @Override
            public void onSuccess(AccountInfo result) {
                if (result == null) {
                    Window.alert("Could not log in");
                    return;
                }

                eventBus.fireEvent(new LoginEvent(result, display.rememberUserOnComputer()));
            }

            @Override
            public void onFailure(Throwable caught) {
                ArrayList<String> msg = new ArrayList<String>();
                msg.add("Server error: " + caught.getMessage());
                display.setFeedback(msg, FeedbackType.ERROR);
            }

        });
    }

    protected ArrayList<String> validate(String name, String pass) {
        ArrayList<String> errors = new ArrayList<String>();

        if (name == null || name.isEmpty())
            errors.add("Field 'Login' is required.");

        if (pass == null || pass.isEmpty())
            errors.add("Field 'Password' is required.");

        return errors;
    }
}
