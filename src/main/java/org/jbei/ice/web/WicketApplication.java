package org.jbei.ice.web;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.settings.ISecuritySettings;
import org.jbei.ice.lib.permissions.IceAuthorizationStrategy;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.pages.AdminPage;
import org.jbei.ice.web.pages.BlastPage;
import org.jbei.ice.web.pages.EntriesPage;
import org.jbei.ice.web.pages.EntryNewPage;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.FeedbackPage;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.QueryPage;
import org.jbei.ice.web.pages.RegistrationPage;
import org.jbei.ice.web.pages.SearchResultPage;
import org.jbei.ice.web.pages.UpdateAccountPage;
import org.jbei.ice.web.pages.UpdatePasswordPage;
import org.jbei.ice.web.pages.UserPage;
import org.jbei.ice.web.pages.WelcomePage;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see org.jbei.Start#main(String[])
 */
public class WicketApplication extends WebApplication {

    /**
     * Constructor
     */
    public WicketApplication() {
    }

    @Override
    protected void init() {
        mountPages();

        initializeQueueingSystem();

        // settings
        ISecuritySettings securitySettings = getSecuritySettings();
        IceAuthorizationStrategy authorizationStrategy = new IceAuthorizationStrategy();
        securitySettings.setAuthorizationStrategy(authorizationStrategy);
        securitySettings.setUnauthorizedComponentInstantiationListener(authorizationStrategy);
        // Be careful with below. It captures all requests, including login pages (passwords)
        // Application.get().getRequestLoggerSettings().setRequestLoggerEnabled(true);
    }

    @Override
    public RequestCycle newRequestCycle(Request request, Response response) {
        return new IceRequestCycle(this, (WebRequest) request, response);
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new IceSession(request);
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<WelcomePage> getHomePage() {
        return WelcomePage.class;
    }

    private void mountPages() {
        mountBookmarkablePage("/login", WelcomePage.class);
        mountBookmarkablePage("/logout", LogOutPage.class);
        mountBookmarkablePage("/registration", RegistrationPage.class);
        mountBookmarkablePage("/update-account", UpdateAccountPage.class);
        mountBookmarkablePage("/update-password", UpdatePasswordPage.class);
        mountBookmarkablePage("/feedback", FeedbackPage.class);
        mount(new IndexedParamUrlCodingStrategy("/entry/view", EntryViewPage.class));
        mount(new IndexedParamUrlCodingStrategy("/entry/update", EntryUpdatePage.class));
        mountBookmarkablePage("/entry/new", EntryNewPage.class);
        mount(new IndexedParamUrlCodingStrategy("/entry/tip", EntryTipPage.class));
        mount(new IndexedParamUrlCodingStrategy("/entries", EntriesPage.class));
        mount(new IndexedParamUrlCodingStrategy("/user", UserPage.class));
        mount(new IndexedParamUrlCodingStrategy("/profile", ProfilePage.class));
        mount(new QueryStringUrlCodingStrategy("/search", SearchResultPage.class));
        mountBookmarkablePage("/blast", BlastPage.class);
        mountBookmarkablePage("/query", QueryPage.class);
        mountBookmarkablePage("/admin", AdminPage.class);
    }

    private void initializeQueueingSystem() {
        JobCue jobCue = JobCue.getInstance();
        Thread jobThread = new Thread(jobCue);
        jobThread.setPriority(Thread.MIN_PRIORITY);
        jobThread.start();
    }
}
