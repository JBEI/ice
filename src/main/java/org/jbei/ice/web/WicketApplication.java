package org.jbei.ice.web;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.request.WebExternalResourceRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.basic.URIRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
import org.apache.wicket.settings.ISecuritySettings;
import org.jbei.ice.lib.authentication.AuthenticationBackend;
import org.jbei.ice.lib.authentication.LblLdapAuthenticationBackend;
import org.jbei.ice.lib.permissions.IceAuthorizationStrategy;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.pages.EntryUpdatePage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.LogOutPage;
import org.jbei.ice.web.pages.RegistrationPage;
import org.jbei.ice.web.pages.UserEntryPage;
import org.jbei.ice.web.pages.WelcomePage;

//import org.odlabs.wiquery.core.commons.WiQueryInstantiationListener;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see org.jbei.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
	private AuthenticationBackend authenticator = null;
	
	/**
	 * Constructor
	 */
	public WicketApplication() {
	}
	
	protected void init() {
		// authenticator = new NullAuthenticator();
		authenticator = new LblLdapAuthenticationBackend();
		
		mountBookmarkablePage("/login", WelcomePage.class);
		mountBookmarkablePage("/logout", LogOutPage.class);
		mountBookmarkablePage("/registration", RegistrationPage.class);
		mount(new IndexedParamUrlCodingStrategy("/entry/view",
				EntryViewPage.class));
		mount(new IndexedParamUrlCodingStrategy("/entry/update",
				EntryUpdatePage.class));
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
		securitySettings
				.setUnauthorizedComponentInstantiationListener(authorizationStrategy);
		
		// wiquery
		/*
		 * WiQueryInstantiationListener wiQueryInstantiationListener = new WiQueryInstantiationListener(); addComponentInstantiationListener(wiQueryInstantiationListener);
		 */
	}
	
	@Override
	public Session newSession(Request request, Response response) {
		IceSession s = new IceSession(request, response, authenticator);
		
		return s;
	}
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<WelcomePage> getHomePage() {
		return WelcomePage.class;
	}
}
