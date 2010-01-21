package org.jbei.ice.web;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.request.WebExternalResourceRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.basic.URIRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.settings.ISecuritySettings;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager;
import org.jbei.ice.lib.authentication.IAuthenticationBackend;
import org.jbei.ice.lib.permissions.IceAuthorizationStrategy;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.pages.BlastPage;
import org.jbei.ice.web.pages.EntriesPage;
import org.jbei.ice.web.pages.EntryNewPage;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.FeedbackPage;
import org.jbei.ice.web.pages.HomePage;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.LoginPage;
import org.jbei.ice.web.pages.RegistrationPage;
import org.jbei.ice.web.pages.SearchResultPage;
import org.jbei.ice.web.pages.UpdateAccountPage;
import org.jbei.ice.web.pages.UpdatePasswordPage;
import org.jbei.ice.web.pages.UserEntryPage;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see org.jbei.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
    private IAuthenticationBackend authenticator = null;

    /**
     * Constructor
     */
    public WicketApplication() {
    }

    @Override
    protected void init() {
        try {
            authenticator = AuthenticationBackendManager.loadAuthenticationBackend();
        } catch (AuthenticationBackendManager.AuthenticationBackendManagerException e) {
            e.printStackTrace();
        }

        mountBookmarkablePage("/login", LoginPage.class);
        mountBookmarkablePage("/logout", LogOutPage.class);
        mountBookmarkablePage("/registration", RegistrationPage.class);
        mountBookmarkablePage("/update-account", UpdateAccountPage.class);
        mountBookmarkablePage("/update-password", UpdatePasswordPage.class);
        mountBookmarkablePage("/feedback", FeedbackPage.class);
        mount(new IndexedParamUrlCodingStrategy("/entry/view", EntryViewPage.class));
        mount(new IndexedParamUrlCodingStrategy("/entry/update", EntryUpdatePage.class));
        mountBookmarkablePage("/entry/new", EntryNewPage.class);
        mount(new IndexedParamUrlCodingStrategy("/entry/tip", EntryTipPage.class));
        mountBookmarkablePage("/entries", EntriesPage.class);
        mountBookmarkablePage("/user/entries", UserEntryPage.class);
        mount(new QueryStringUrlCodingStrategy("/search", SearchResultPage.class));
		mountBookmarkablePage("/blast", BlastPage.class);
        mount(new URIRequestTargetUrlCodingStrategy("/static") {
            @Override
            public IRequestTarget decode(RequestParameters requestParameters) {
                String path = "/static/" + getURI(requestParameters);
                return new WebExternalResourceRequestTarget(path);
            }
        });

        // job cue
        JobCue jobCue = JobCue.getInstance();
        Thread jobThread = new Thread(jobCue);
        jobThread.setPriority(Thread.MIN_PRIORITY);
        jobThread.start();

        // settings
        ISecuritySettings securitySettings = getSecuritySettings();
        IceAuthorizationStrategy authorizationStrategy = new IceAuthorizationStrategy();
        securitySettings.setAuthorizationStrategy(authorizationStrategy);
        securitySettings.setUnauthorizedComponentInstantiationListener(authorizationStrategy);
    }

    @Override
    public RequestCycle newRequestCycle(Request request, Response response) {
        return new IceRequestCycle(this, (WebRequest) request, response);
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new IceSession(request, response, authenticator);
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    public Class<HomePage> getHomePage() {
        return HomePage.class;
    }
}
