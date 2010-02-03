package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.IceSession;

public class LogOutPage extends UnprotectedPage {
    public LogOutPage(PageParameters parameters) {
        super(parameters);

        ((IceSession) getSession()).deAuthenticateUser();
        ((IceSession) getSession()).invalidateNow();

        setResponsePage(HomePage.class);
    }
}
