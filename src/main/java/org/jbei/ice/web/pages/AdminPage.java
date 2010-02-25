package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.AdminPanel;
import org.jbei.ice.web.panels.EmptyMessagePanel;

public class AdminPage extends ProtectedPage {
    public AdminPage(PageParameters parameters) {
        super(parameters);
        Account account = IceSession.get().getAccount();
        if (account != null) {
            if (AccountManager.isModerator(account)) {
                add(new AdminPanel("adminPanel"));
            } else {
                add(new EmptyMessagePanel("adminPanel", "You do not have permission"));
            }
        }
    }
}
