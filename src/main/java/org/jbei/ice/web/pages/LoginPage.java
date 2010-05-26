package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.LoginPanel;

public class LoginPage extends UnprotectedPage {
    public LoginPage(PageParameters parameters) {
        super(parameters);

        initialize();
    }

    private void initialize() {
        if (IceSession.get().isAuthenticated()) {
            setRedirect(true);
            setResponsePage(UserPage.class);

            return;
        }

        add(new LoginPanel("loginPanel"));
    }
}
