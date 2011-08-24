package org.jbei.ice.client;

import java.util.Date;
import java.util.HashMap;

import org.jbei.ice.client.event.ILoginEventHandler;
import org.jbei.ice.client.event.ILogoutEventHandler;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.client.event.LogoutEvent;
import org.jbei.ice.client.presenter.AdvancedSearchPresenter;
import org.jbei.ice.client.presenter.BlastPresenter;
import org.jbei.ice.client.presenter.CollectionsPresenter;
import org.jbei.ice.client.presenter.EntryAddPresenter;
import org.jbei.ice.client.presenter.EntryPresenter;
import org.jbei.ice.client.presenter.HomePagePresenter;
import org.jbei.ice.client.presenter.LoginPresenter;
import org.jbei.ice.client.presenter.ProfilePresenter;
import org.jbei.ice.client.view.AdvancedSearchView;
import org.jbei.ice.client.view.BlastView;
import org.jbei.ice.client.view.CollectionsView;
import org.jbei.ice.client.view.EntryAddView;
import org.jbei.ice.client.view.EntryView;
import org.jbei.ice.client.view.HomePageView;
import org.jbei.ice.client.view.LoginView;
import org.jbei.ice.client.view.ProfileView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

public class AppController implements Presenter, ValueChangeHandler<String> {

    // cookie times out in three days (current value set in Ice
    private static final int COOKIE_TIMEOUT = (1000 * 60 * 60 * 24) * 3;
    private static final String COOKIE_NAME = "gd-ice";
    private static final String COOKIE_PATH = "/";

    private HasWidgets container;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    public static String sessionId;

    public AppController(RegistryServiceAsync service, HandlerManager eventBus) {

        this.service = service;
        this.eventBus = eventBus;
        bind();
    }

    private void bind() {
        History.addValueChangeHandler(this);

        // eventBus.addHandlers... here
        this.eventBus.addHandler(LoginEvent.TYPE, new ILoginEventHandler() {

            @Override
            public void onLogin(LoginEvent event) {
                AppController.sessionId = event.getSessionId();
                if (event.isRememberUser()) {
                    Date expires = new Date(System.currentTimeMillis() + COOKIE_TIMEOUT);
                    // TODO : set the domain ? and change secure to true?
                    Cookies.setCookie(COOKIE_NAME, sessionId, expires, null, COOKIE_PATH, false);
                }
                goToMainPage();
            }
        });

        this.eventBus.addHandler(LogoutEvent.TYPE, new AppLogoutHandler());
    }

    private void goToMainPage() {
        if (sessionId == null)
            History.newItem("page=" + Page.LOGIN.getToken());

        Page currentPage = getPage(History.getToken());
        if (Page.LOGIN != currentPage)
            History.fireCurrentHistoryState();
        else
            History.newItem("page=" + Page.MAIN.getToken());
    }

    /**
     * Required override from ValueChange Handler interface.
     * newItem and fireCurrentHistoryState method calls of History, causes
     * this to be evaluated
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {

        String token = event.getValue();
        Page page = getPage(token);
        Presenter presenter;

        switch (page) {

        case MAIN:
            presenter = new HomePagePresenter(this.service, this.eventBus, new HomePageView());
            break;

        case ADD_ENTRY:
            presenter = new EntryAddPresenter(this.service, this.eventBus, new EntryAddView());
            break;

        case ENTRY_VIEW:
            presenter = new EntryPresenter(this.service, this.eventBus, new EntryView());
            break;

        case PROFILE:
            presenter = new ProfilePresenter(this.service, this.eventBus, new ProfileView());
            break;

        case COLLECTIONS:
            presenter = new CollectionsPresenter(this.service, this.eventBus, new CollectionsView());
            break;

        case BLAST:
            presenter = new BlastPresenter(this.service, this.eventBus, new BlastView());
            break;

        case QUERY:
            presenter = new AdvancedSearchPresenter(this.service, this.eventBus,
                    new AdvancedSearchView());
            break;

        case LOGIN:
        default:
            presenter = new LoginPresenter(this.service, this.eventBus, new LoginView());
            break;
        }

        presenter.go(this.container);
    }

    protected Page getPage(String token) {
        Page page = null;
        if (token.startsWith("page")) {
            HashMap<String, String> values = parseToken(token);
            String pageVal = values.get("page");
            page = Page.tokenToEnum(pageVal);
        } else {
            Page tmp = Page.tokenToEnum(token);
            if (tmp != null)
                page = tmp;
        }

        if (page == null)
            return Page.LOGIN;
        return page;
    }

    private HashMap<String, String> parseToken(String token) {
        HashMap<String, String> tokens = new HashMap<String, String>();
        for (String string : token.split(";")) {
            String[] values = string.split("=");
            if (values != null && values.length >= 2)
                tokens.put(values[0], values[1]);
        }

        return tokens;
    }

    // newItem and fireCurrentHistoryState causes onValueChange call
    @Override
    public void go(final HasWidgets container) {

        this.container = container;

        // check if there is a stored session that is valid
        final String sessionId = Cookies.getCookie(COOKIE_NAME);
        if (sessionId != null) {

            service.sessionValid(sessionId, new AsyncCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean result) {
                    if (result.booleanValue()) {
                        AppController.sessionId = sessionId;
                        goToMainPage();
                    } else {
                        History.newItem("page=" + Page.LOGIN.getToken());
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // technically this is not the result of an invalid sessionId
                    // an attempt to login may cause another failure. punting dealing with that
                    GWT.log("Error checking for session validity", caught);
                    if (Page.LOGIN.getToken().equals(History.getToken()))
                        History.fireCurrentHistoryState();
                    else
                        History.newItem("page=" + Page.LOGIN.getToken());
                }
            });
        } else {

            if ("".equals(History.getToken())) {
                History.newItem("page=" + Page.LOGIN.getToken());
            } else {
                History.fireCurrentHistoryState();
            }
        }
    }

    // handler implementations
    private final class AppLogoutHandler implements ILogoutEventHandler {

        @Override
        public void onLogout(LogoutEvent event) {
            service.logout(AppController.sessionId, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable caught) {
                }

                @Override
                public void onSuccess(Boolean result) {
                    History.newItem("page=" + Page.LOGIN.getToken());
                }
            });

            AppController.sessionId = null;
            Cookies.removeCookie(COOKIE_NAME, COOKIE_PATH);
        }
    }
}
