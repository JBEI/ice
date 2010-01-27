package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.jbei.ice.web.IceSession;

public class WelcomePage extends UnprotectedPage {
    public WelcomePage(PageParameters parameters) {
        super(parameters);

        if (IceSession.get().isAuthenticated()) {
            throw new RestartResponseAtInterceptPageException(WorkSpacePage.class);
        }
    }
}
