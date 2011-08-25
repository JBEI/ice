package org.jbei.ice.client.presenter;

import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.event.LoginEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
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

public class LoginPresenter implements Presenter {

    public interface Display {

        HasClickHandlers getLoginButton();

        String getLoginName();

        String getLoginPass();

        boolean rememberUserOnComputer();

        ILogoutHandler getLogoutHandler();

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

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

        String err = validate(loginName, loginPass);
        if (err != null) {
            Window.alert(err);
            return;
        }

        service.login(loginName, loginPass, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (result == null) {
                    Window.alert("Could not log in");
                    return;
                }

                eventBus.fireEvent(new LoginEvent(result, display.rememberUserOnComputer()));
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO : show error message
                Window.alert("Failed: " + caught.getMessage());
            }
        });
    }

    protected String validate(String name, String pass) {
        return null;
    }
}
