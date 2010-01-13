package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.LoginPanel;

public class LoginPage extends UnprotectedPage {
	public LoginPage(PageParameters parameters) {
		super(parameters);

		if (IceSession.get().isAuthenticated()) {
			setResponsePage(WelcomePage.class);

			return;
		}

		add(new LoginPanel("loginPanel"));
	}
}
