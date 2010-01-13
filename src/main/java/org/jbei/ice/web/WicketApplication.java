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
import org.apache.wicket.settings.ISecuritySettings;
import org.jbei.ice.lib.authentication.AuthenticationBackendManager;
import org.jbei.ice.lib.authentication.IAuthenticationBackend;
import org.jbei.ice.lib.permissions.IceAuthorizationStrategy;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.pages.EntriesPage;
import org.jbei.ice.web.pages.EntryNewPage;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.FeedbackPage;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.RegistrationPage;
import org.jbei.ice.web.pages.UpdateAccountPage;
import org.jbei.ice.web.pages.UpdatePasswordPage;
import org.jbei.ice.web.pages.UserEntryPage;
import org.jbei.ice.web.pages.WelcomePage;

//import org.odlabs.wiquery.core.commons.WiQueryInstantiationListener;

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

	protected void init() {
		try {
			authenticator = AuthenticationBackendManager.loadAuthenticationBackend();
		} catch (AuthenticationBackendManager.AuthenticationBackendManagerException e) {
			e.printStackTrace();
		}

		mountBookmarkablePage("/login", WelcomePage.class);
		mountBookmarkablePage("/logout", LogOutPage.class);
		mountBookmarkablePage("/registration", RegistrationPage.class);
		mountBookmarkablePage("/update-account", UpdateAccountPage.class);
		mountBookmarkablePage("/update-password", UpdatePasswordPage.class);
		mountBookmarkablePage("/feedback", FeedbackPage.class);
		mount(new IndexedParamUrlCodingStrategy("/entry/view", EntryViewPage.class));
		mount(new IndexedParamUrlCodingStrategy("/entry/update", EntryUpdatePage.class));
		mountBookmarkablePage("/entry/new", EntryNewPage.class);
		mountBookmarkablePage("/entries", EntriesPage.class);
		mountBookmarkablePage("/user/entries", UserEntryPage.class);
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
		jobThread.start();

		// settings
		ISecuritySettings securitySettings = getSecuritySettings();
		IceAuthorizationStrategy authorizationStrategy = new IceAuthorizationStrategy();
		securitySettings.setAuthorizationStrategy(authorizationStrategy);
		securitySettings.setUnauthorizedComponentInstantiationListener(authorizationStrategy);

		// wiquery
		/*
		 * WiQueryInstantiationListener wiQueryInstantiationListener = new WiQueryInstantiationListener(); addComponentInstantiationListener(wiQueryInstantiationListener);
		 */
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
	public Class<WelcomePage> getHomePage() {
		return WelcomePage.class;
	}
}
