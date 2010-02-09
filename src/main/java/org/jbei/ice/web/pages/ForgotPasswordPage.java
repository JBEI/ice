package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.web.panels.ForgotPasswordPanel;

public class ForgotPasswordPage extends UnprotectedPage {
    public ForgotPasswordPage(PageParameters parameters) {
        super(parameters);
        add(new ForgotPasswordPanel("forgotPasswordPanel").setOutputMarkupId(true));

    }
}
