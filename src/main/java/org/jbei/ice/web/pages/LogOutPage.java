package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.IceSession;

/**
 * @author tham
 */
public class LogOutPage extends HomePage {
	public LogOutPage(PageParameters parameters) {
		super(parameters);
		
		((IceSession)getSession()).deAuthenticateUser();
		((IceSession)getSession()).invalidateNow();
		setResponsePage(WelcomePage.class);		
	}
}

