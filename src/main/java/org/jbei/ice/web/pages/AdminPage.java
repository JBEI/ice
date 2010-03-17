package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.panels.AdminPanel;
import org.jbei.ice.web.panels.EmptyMessagePanel;

public class AdminPage extends ProtectedPage {
    public AdminPage(PageParameters parameters) {
        super(parameters);

        initializeControls();
    }

    private void initializeControls() {
        Boolean isModerator = false;

        try {
            isModerator = AccountController.isModerator(IceSession.get().getAccount());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        if (isModerator) {
            add(new AdminPanel("adminPanel"));
        } else {
            add(new EmptyMessagePanel("adminPanel", "You do not have permission"));
        }
    }
}
