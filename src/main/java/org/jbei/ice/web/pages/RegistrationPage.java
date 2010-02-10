package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.panels.EmptyMessagePanel;
import org.jbei.ice.web.panels.RegistrationPanel;

public class RegistrationPage extends UnprotectedPage {

    public RegistrationPage(PageParameters parameters) {
        super(parameters);
        if (JbeirSettings.getSetting("NEW_REGISTRATION_ALLOWED").equals("yes")) {
            add(new RegistrationPanel("registrationPanel"));
        } else {
            String msg = "New registration has been disable by the administrator.";
            add(new EmptyMessagePanel("registrationPanel", msg));
        }
    }

    @Override
    protected String getTitle() {
        return "Registration - " + super.getTitle();
    }
}
