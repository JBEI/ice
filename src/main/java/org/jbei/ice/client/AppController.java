package org.jbei.ice.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.jbei.ice.client.admin.AdminPresenter;
import org.jbei.ice.client.admin.AdminView;
import org.jbei.ice.client.bulkupload.BulkUploadPresenter;
import org.jbei.ice.client.bulkupload.BulkUploadView;
import org.jbei.ice.client.bulkupload.model.BulkUploadModel;
import org.jbei.ice.client.collection.model.CollectionsModel;
import org.jbei.ice.client.collection.presenter.CollectionsPresenter;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.collection.view.CollectionsView;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.header.QuickSearchParser;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.client.event.ILoginEventHandler;
import org.jbei.ice.client.event.ILogoutEventHandler;
import org.jbei.ice.client.event.LoginEvent;
import org.jbei.ice.client.event.LogoutEvent;
import org.jbei.ice.client.event.SearchEvent;
import org.jbei.ice.client.event.SearchEventHandler;
import org.jbei.ice.client.home.HomePagePresenter;
import org.jbei.ice.client.home.HomePageView;
import org.jbei.ice.client.login.LoginPresenter;
import org.jbei.ice.client.login.LoginView;
import org.jbei.ice.client.news.NewsPresenter;
import org.jbei.ice.client.news.NewsView;
import org.jbei.ice.client.profile.ProfilePresenter;
import org.jbei.ice.client.profile.ProfileView;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;

// TODO : this class is due for a makeover
public class AppController extends AbstractPresenter implements ValueChangeHandler<String> {

    // cookie times out in three days (current value set in Ice)
    private static final int COOKIE_TIMEOUT = (1000 * 60 * 60 * 24) * 3;
    private static final int DAY_TIMEOUT = (1000 * 60 * 60 * 24);
    private static final String COOKIE_NAME = "gd-ice";
    private static final String COOKIE_PATH = "/";

    private HasWidgets container; // root panel
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    public static String sessionId;

    // TODO : see permissions auto complete for how to run this on the server and avoid sending data back and forth
    public static HashMap<AutoCompleteField, ArrayList<String>> autoCompleteData;
    public static AccountInfo accountInfo;

    public AppController(RegistryServiceAsync service, HandlerManager eB) {
        this.service = service;
        eventBus = eB;
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
                AppController.sessionId = event.getSessionId();
                accountInfo = event.getAccountInfo();

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

        // search handler
        this.eventBus.addHandler(SearchEvent.TYPE, new SearchEventHandler() {

            @Override
            public void onSearch(SearchEvent event) {
                showSearchResults(event.getFilters());
            }
        });

        // entry view event
        this.eventBus.addHandler(EntryViewEvent.TYPE, new EntryViewEventHandler() {

            @Override
            public void onEntryView(EntryViewEvent event) {
                showEntryView(event);
            }
        });
    }

    private void showSearchResults(ArrayList<SearchFilterInfo> operands) {
        if (operands == null)
            return;

        Page currentPage = getPage(History.getToken());
        if (currentPage == Page.COLLECTIONS)
            return; // collections is also listening on the eventBus for search events so do not respond. 

        History.newItem(Page.COLLECTIONS.getLink(), false);
        final CollectionsView cView = new CollectionsView();
        addHeaderSearchHandler(cView);
        CollectionsPresenter presenter = new CollectionsPresenter(new CollectionsModel(
                this.service, this.eventBus), cView, operands);
        presenter.go(container);
    }

    private void showEntryView(EntryViewEvent event) {
        Page currentPage = getPage(History.getToken());
        if (currentPage == Page.COLLECTIONS || currentPage == Page.ENTRY_VIEW)
            return;

        CollectionsView cView = new CollectionsView();
        addHeaderSearchHandler(cView); // TODO : not sure if this is needed

        CollectionsPresenter presenter = new CollectionsPresenter(new CollectionsModel(
                this.service, this.eventBus), cView, event.getContext());
        presenter.go(container);
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
     * Required override from ValueChange Handler interface. newItem and fireCurrentHistoryState method calls of
     * History, causes this to be evaluated
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

        HashMap<String, String> parsed = parseToken(token);
        String param = parsed.get("id");
        String selection = parsed.get("s");

        // TODO redirect after login
        if (AppController.sessionId == null)
            page = Page.LOGIN;
        else {
            if (page == Page.LOGIN)
                page = Page.MAIN;
        }

        AbstractPresenter presenter;

        switch (page) {

            // TODO : cache the views and call reset() in the presenter when displaying them. they are apparently
            // expensive to create or sum such
            // TODO : presenters are cheap however
            case MAIN:
                HomePageView homePageView = new HomePageView();
                addHeaderSearchHandler(homePageView);
                presenter = new HomePagePresenter(this.service, this.eventBus, homePageView);
                break;

            case ENTRY_VIEW:
                CollectionsView cView = new CollectionsView();
                addHeaderSearchHandler(cView); // TODO : not sure if this is needed
                long id = Long.decode(param);
                EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                context.setCurrent(id);

                presenter = new CollectionsPresenter(new CollectionsModel(this.service, this.eventBus),
                                                     cView, context);
                break;

            case PROFILE:
                ProfileView pView = new ProfileView();
                presenter = new ProfilePresenter(this.service, this.eventBus, pView, param); //, selection);
                addHeaderSearchHandler(pView);
                break;

            case COLLECTIONS:
                CollectionsView collectionsView = new CollectionsView();
                addHeaderSearchHandler(collectionsView);
                CollectionsModel collectionsModel = new CollectionsModel(this.service, this.eventBus);
                presenter = new CollectionsPresenter(collectionsModel, collectionsView, param);
                break;

            case BULK_IMPORT:
                BulkUploadModel model = new BulkUploadModel(this.service, this.eventBus);
                BulkUploadView uploadView = new BulkUploadView();
                addHeaderSearchHandler(uploadView);
                presenter = new BulkUploadPresenter(model, uploadView);
                break;

            case NEWS:
                NewsView nView = new NewsView();
                addHeaderSearchHandler(nView);
                presenter = new NewsPresenter(this.service, this.eventBus, nView);
                break;

            case ADMIN:
                if (!AppController.accountInfo.isAdmin()) {
                    History.newItem(Page.MAIN.getLink());
                    return;
                }
                AdminView aView = new AdminView();
                addHeaderSearchHandler(aView);
                presenter = new AdminPresenter(this.service, this.eventBus, aView);
                break;

            case LOGIN:
            default:
                presenter = new LoginPresenter(this.service, this.eventBus, new LoginView());
                break;
        }

        presenter.go(this.container);
    }

    // TODO : this can go into the abstract presenter at some point
    private void addHeaderSearchHandler(final AbstractLayout view) {
        if (view == null)
            return;

        view.getHeader().addSearchClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                view.getHeader().setSearchButtonEnable(false);
                ArrayList<SearchFilterInfo> parse = QuickSearchParser.parse(view.getHeader()
                                                                                .getSearchInput());
                SearchFilterInfo blastInfo = view.getHeader().getBlastInfo();
                if (blastInfo != null)
                    parse.add(blastInfo);
                SearchEvent searchInProgressEvent = new SearchEvent();
                searchInProgressEvent.setFilters(parse);
                eventBus.fireEvent(searchInProgressEvent);
                view.getHeader().setSearchButtonEnable(true);
            }
        });
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

            service.sessionValid(sessionId, new AsyncCallback<AccountInfo>() {

                @Override
                public void onSuccess(AccountInfo result) {
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
     * Logout handler implementation. Clears cookies and session id and redirects user to login page
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
