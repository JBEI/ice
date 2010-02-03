package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;

public class ProfileAboutUserPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ProfileAboutUserPanel(String id, String accountEmail) {
        super(id);

        Account account = null;
        try {
            account = AccountManager.getByEmail(accountEmail);
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        if (account == null) {
            add(new Label("name", accountEmail));
            add(new Label("email", accountEmail));
            add(new Label("memberSince", ""));
            add(new MultiLineLabel("institution", ""));
            add(new MultiLineLabel("description", ""));
        } else {
            Date memberSinceDate = account.getCreationTime();
            String memberSince = "";

            if (memberSinceDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
                memberSince = dateFormat.format(memberSinceDate);
            }

            add(new Label("name", account.getFullName()));
            add(new Label("email", account.getEmail()));
            add(new Label("memberSince", memberSince));
            add(new MultiLineLabel("institution", account.getInstitution()));
            add(new MultiLineLabel("description", account.getDescription()));
        }
    }
}
