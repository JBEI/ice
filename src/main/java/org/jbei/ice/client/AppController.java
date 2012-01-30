package org.jbei.ice.client;

import java.util.Date;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.BulkImportPresenter;
import org.jbei.ice.client.bulkimport.BulkImportView;
import org.jbei.ice.client.collection.presenter.CollectionsEntriesPresenter;
import org.jbei.ice.client.collection.presenter.CollectionsListPresenter;
import org.jbei.ice.client.collection.view.CollectionsEntriesView;
import org.jbei.ice.client.collection.view.CollectionsListView;
import org.jbei.ice.client.entry.add.EntryAddPresenter;
import org.jbei.ice.client.entry.add.EntryAddView;
import org.jbei.ice.client.entry.view.EntryPresenter;
import org.jbei.ice.client.entry.view.view.EntryView;
import org.jbei.ice.client.event.ILoginEventHandler;
import org.jbei.ice.client.event.ILogoutEventHandler;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.client.event.LogoutEvent;
import org.jbei.ice.client.home.HomePagePresenter;
import org.jbei.ice.client.home.HomePageView;
import org.jbei.ice.client.login.LoginPresenter;
import org.jbei.ice.client.login.LoginView;
import org.jbei.ice.client.profile.ProfilePresenter;
import org.jbei.ice.client.profile.ProfileView;
import org.jbei.ice.client.search.advanced.AdvancedSearchPresenter;
import org.jbei.ice.client.search.advanced.AdvancedSearchView;
import org.jbei.ice.client.search.blast.BlastPresenter;
import org.jbei.ice.client.search.blast.BlastView;
import org.jbei.ice.client.storage.StoragePresenter;
import org.jbei.ice.client.storage.StorageView;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

public class AppController extends AbstractPresenter implements ValueChangeHandler<String> {

    // cookie times out in three days (current value set in Ice)
    private static final int COOKIE_TIMEOUT = (1000 * 60 * 60 * 24) * 3;
    private static final String COOKIE_NAME = "gd-ice";
    private static final String COOKIE_PATH = "/";

    private HasWidgets container; // rootpanel
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    public static String sessionId;
    public static AccountInfo accountInfo;

    public AppController(RegistryServiceAsync service, HandlerManager eB) {

        this.service = service;
        eventBus = eB;
        bind();
    }

    private void bind() {
        History.addValueChangeHandler(this);

        // eventBus.addHandlers... here
        this.eventBus.addHandler(LoginEvent.TYPE, new ILoginEventHandler() {

            @Override
            public void onLogin(LoginEvent event) {
                AppController.sessionId = event.getSessionId();
                accountInfo = event.getAccountInfo();

                if (event.isRememberUser()) {
                    Date expires = new Date(System.currentTimeMillis() + COOKIE_TIMEOUT);
                    // TODO : set the domain ? and change secure to true?
                    Cookies.setCookie(COOKIE_NAME, sessionId, expires, null, COOKIE_PATH, true);
                }
                goToMainPage();
            }
        });

        // add log out handler
        this.eventBus.addHandler(LogoutEvent.TYPE, new AppLogoutHandler());
    }

    private void goToMainPage() {
        Page currentPage = getPage(History.getToken());
        if (Page.LOGIN != currentPage)
            History.fireCurrentHistoryState();
        else
            History.newItem(Page.MAIN.getLink());
    }

    private void logout() {
        this.eventBus.fireEvent(new LogoutEvent());
    }

    /**
     * Required override from ValueChange Handler interface.
     * newItem and fireCurrentHistoryState method calls of History, causes
     * this to be evaluated
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String token = event.getValue();
        goToPage(token);
    }

    private void goToPage(String token) {
        Page page;
        if (token == null || token.isEmpty())
            page = Page.MAIN;
        else
            page = getPage(token);

        if (page == Page.LOGOUT) {
            logout();
            return;
        }

        String param = getParam("id", token);

        // TODO redirect after login
        if (AppController.sessionId == null)
            page = Page.LOGIN;

        AbstractPresenter presenter;

        switch (page) {

        // TODO : cache the views and call reset() in the presenter when displaying them. they are apparently expensive to create or sum such
        // TODO : presenters are cheap however
        case MAIN:
            presenter = new HomePagePresenter(this.service, this.eventBus, new HomePageView());
            break;

        case ADD_ENTRY:
            presenter = new EntryAddPresenter(this.service, this.eventBus, new EntryAddView());
            break;

        case ENTRY_VIEW:
            if (param == null)
                presenter = new EntryPresenter(this.service, this.eventBus, new EntryView());
            else
                presenter = new EntryPresenter(this.service, this.eventBus, new EntryView(), param);
            break;

        case PROFILE:
            presenter = new ProfilePresenter(this.service, this.eventBus, new ProfileView(), param);
            break;

        case COLLECTIONS:
            if (param != null && !param.isEmpty())
                presenter = new CollectionsEntriesPresenter(this.service, this.eventBus,
                        new CollectionsEntriesView(), param);
            else
                presenter = new CollectionsListPresenter(this.service, this.eventBus,
                        new CollectionsListView());
            break;

        case BLAST:
            presenter = new BlastPresenter(this.service, this.eventBus, new BlastView());
            break;

        case QUERY:
            presenter = new AdvancedSearchPresenter(this.service, this.eventBus,
                    new AdvancedSearchView());
            break;

        case BULK_IMPORT:
            presenter = new BulkImportPresenter(this.service, this.eventBus, new BulkImportView());
            break;

        case STORAGE:
            presenter = new StoragePresenter(this.service, this.eventBus, new StorageView(), param);
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

    private String getParam(String key, String token) {
        return parseToken(token).get(key);
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
        // TODO : what if sessionId is still set and is different

        final String sessionId = Cookies.getCookie(COOKIE_NAME);
        if (sessionId != null) {

            service.sessionValid(sessionId, new AsyncCallback<AccountInfo>() {

                @Override
                public void onSuccess(AccountInfo result) {// TODO : this does not return session Id
                    if (result != null) {
                        AppController.accountInfo = result;
                        AppController.sessionId = sessionId;
                        goToPage(History.getToken());
                    } else {
                        AppController.sessionId = null;
                        AppController.accountInfo = null;
                        Cookies.removeCookie(COOKIE_NAME);
                        if (Page.LOGIN.getToken().equals(History.getToken()))
                            History.fireCurrentHistoryState();
                        else
                            History.newItem(Page.LOGIN.getLink());
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // technically this is not the result of an invalid sessionId
                    // an attempt to login may cause another failure. punting dealing with that
                    if (Page.LOGIN.getLink().equals(History.getToken()))
                        History.fireCurrentHistoryState();
                    else
                        History.newItem(Page.LOGIN.getLink());
                }
            });
        } else {

            if (Page.LOGIN.getLink().equals(History.getToken()))
                History.fireCurrentHistoryState();
            else
                History.newItem(Page.LOGIN.getLink());
        }
    }

    //
    // Inner classes
    //

    /**
     * Logout handler implementation. Clears cookies and session id
     * and redirects user to login page
     */
    private final class AppLogoutHandler implements ILogoutEventHandler {

        @Override
        public void onLogout(LogoutEvent event) {
            service.logout(AppController.sessionId, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable caught) {
                    History.newItem("page=" + Page.LOGIN.getToken());
                }

                @Override
                public void onSuccess(Boolean result) {
                    History.newItem("page=" + Page.LOGIN.getToken());
                }
            });

            AppController.sessionId = null;
            AppController.accountInfo = null;
            Cookies.removeCookie(COOKIE_NAME, COOKIE_PATH);
        }
    }
}
