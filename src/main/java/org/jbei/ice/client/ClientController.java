package org.jbei.ice.client;

import java.util.Date;
import java.util.HashMap;

import org.jbei.ice.client.admin.AdminPresenter;
import org.jbei.ice.client.admin.AdminView;
import org.jbei.ice.client.bulkupload.BulkUploadPresenter;
import org.jbei.ice.client.bulkupload.BulkUploadView;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.collection.view.CollectionsView;
import org.jbei.ice.client.common.header.HeaderView;
import org.jbei.ice.client.event.ILoginEventHandler;
import org.jbei.ice.client.event.ILogoutEventHandler;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.client.event.LogoutEvent;
import org.jbei.ice.client.login.LoginPresenter;
import org.jbei.ice.client.login.LoginView;
import org.jbei.ice.client.news.NewsPresenter;
import org.jbei.ice.client.news.NewsView;
import org.jbei.ice.client.profile.ProfilePresenter;
import org.jbei.ice.client.profile.ProfileView;
import org.jbei.ice.client.search.advanced.SearchView;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

public class ClientController extends AbstractPresenter implements ValueChangeHandler<String> {

    // cookie times out in three days (current value set in Ice)
    private static final int COOKIE_TIMEOUT = (1000 * 60 * 60 * 24) * 3;
    private static final int DAY_TIMEOUT = (1000 * 60 * 60 * 24);
    private static final String COOKIE_NAME = "gd-ice"; // TODO: this is set in the backend. does anyone even change it?
    private static final String COOKIE_PATH = "/";

    private HasWidgets container; // root panel
    public static String sessionId;
    public static User account;
    public static String pageViewAttempt;   // page user attempted to view but was sent to the login page
    private static SearchQuery lastQuery;

    public ClientController(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        bind();
    }

    /**
     * Adds a window close event that clears the cookies when the user closes the window and has not selected "remember
     * me"
     */
    //    private void addCookieClearOnWindowClose() {
    //        Window.addCloseHandler(new CloseHandler<Window>() {
    //
    //            @Override
    //            public void onClose(CloseEvent<Window> event) {
    //                logout();
    //            }
    //        });
    //    }
    private void bind() {
        History.addValueChangeHandler(this);

        // eventBus.addHandlers... here
        this.eventBus.addHandler(LoginEvent.TYPE, new ILoginEventHandler() {

            @Override
            public void onLogin(LoginEvent event) {
                ClientController.sessionId = event.getSessionId();
                account = event.getAccountInfo();
                HeaderView.getInstance().setHeaderData(account);
                HeaderView.getInstance().setHeader(Page.COLLECTIONS);
                HeaderView.getInstance().setQueryDelegate(createServiceDelegate());

                Date expires;
                if (!event.isRememberUser())
                    expires = new Date(System.currentTimeMillis() + DAY_TIMEOUT);
                else
                    expires = new Date(System.currentTimeMillis() + COOKIE_TIMEOUT);

                Cookies.setCookie(COOKIE_NAME, sessionId, expires, null, COOKIE_PATH, true);
                //                if (!event.isRememberUser())
                //                    addCookieClearOnWindowClose();

                goToMainPage();
            }
        });

        // add log out handler
        this.eventBus.addHandler(LogoutEvent.TYPE, new AppLogoutHandler());
    }

    private ServiceDelegate<SearchQuery> createServiceDelegate() {
        return new ServiceDelegate<SearchQuery>() {
            @Override
            public void execute(SearchQuery query) {
                SearchView searchView = new SearchView();
                CollectionsView collectionsView = new CollectionsView();
                lastQuery = query;
                CollectionsPresenter presenter = new CollectionsPresenter(service, eventBus, collectionsView,
                                                                          searchView, query);
                presenter.go(container);
            }
        };
    }

    private void goToMainPage() {
        Page currentPage = getPage(History.getToken());
        if (Page.LOGIN != currentPage)
            History.fireCurrentHistoryState();
        else {
            History.newItem(Page.MAIN.getLink());
        }
    }

    private void logout() {
        this.eventBus.fireEvent(new LogoutEvent());
    }

    /**
     * Required override from ValueChange Handler interface. newItem and fireCurrentHistoryState method calls of
     * History, causes this to be evaluated
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        String token = event.getValue();
        if (ClientController.account != null)
            HeaderView.getInstance().setHeader(getPage(token));

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

        HashMap<String, String> parsed = parseToken(token);
        String param = parsed.get("id");
        String selection = parsed.get("s");

        if (ClientController.sessionId == null) {
            if (page != Page.LOGIN) {
                pageViewAttempt = token;
                page = Page.LOGIN;
            }
        } else {
            if (page == Page.LOGIN)
                page = Page.MAIN;
        }

        AbstractPresenter presenter;
        if (page != Page.LOGIN && page != Page.QUERY)
            HeaderView.getInstance().resetSearchBox();

        switch (page) {
            case ENTRY_VIEW:
                CollectionsView cView = new CollectionsView();
                long id = Long.decode(param);
                EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                context.setId(id);
                presenter = new CollectionsPresenter(this.service, this.eventBus, cView, context);
                break;

            case PROFILE:
                ProfileView pView = new ProfileView(param);
                presenter = new ProfilePresenter(this.service, this.eventBus, pView, param, selection);
                break;

            case COLLECTIONS:
            case MAIN:
                CollectionsView collectionsView = new CollectionsView();
                presenter = new CollectionsPresenter(this.service, this.eventBus, collectionsView, param);
                break;

            case BULK_IMPORT:
                BulkUploadView uploadView = new BulkUploadView();
                presenter = new BulkUploadPresenter(service, eventBus, uploadView);
                break;

            case NEWS:
                NewsView nView = new NewsView();
                presenter = new NewsPresenter(this.service, this.eventBus, nView);
                break;

            case ADMIN:
                if (!ClientController.account.isAdmin()) {
                    History.newItem(Page.MAIN.getLink());
                    return;
                }
                AdminView aView = new AdminView();
                presenter = new AdminPresenter(this.service, this.eventBus, aView, param);
                break;

            case QUERY:
                // this case occurs when the user clicks "back" so the composite search box should be filled
                SearchView searchView = new SearchView();
                collectionsView = new CollectionsView();
                presenter = new CollectionsPresenter(this.service, this.eventBus, collectionsView, searchView,
                                                     lastQuery);
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
            service.sessionValid(sessionId, new AsyncCallback<User>() {

                @Override
                public void onSuccess(User result) {
                    if (result != null) {
                        ClientController.account = result;
                        ClientController.sessionId = sessionId;
                        String token = History.getToken();
                        goToPage(token);
                        HeaderView.getInstance().setHeader(getPage(token));
                        HeaderView.getInstance().setHeaderData(account);
                        HeaderView.getInstance().setQueryDelegate(createServiceDelegate());
                    } else {
                        ClientController.sessionId = null;
                        ClientController.account = null;
                        Cookies.removeCookie(COOKIE_NAME);
                        new LoginPresenter(service, eventBus, new LoginView()).go(container);
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
            new LoginPresenter(service, eventBus, new LoginView()).go(container);
        }
    }

    //
    // Inner classes
    //

    /**
     * Logout handler implementation. Clears cookies and session id and redirects user to login page
     */
    private final class AppLogoutHandler implements ILogoutEventHandler {

        @Override
        public void onLogout(LogoutEvent event) {
            service.logout(ClientController.sessionId, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable caught) {
                    History.newItem("page=" + Page.LOGIN.getToken());
                }

                @Override
                public void onSuccess(Boolean result) {
                    History.newItem("page=" + Page.LOGIN.getToken());
                }
            });

            ClientController.sessionId = null;
            ClientController.account = null;
            Cookies.removeCookie(COOKIE_NAME, COOKIE_PATH);
        }
    }
}
