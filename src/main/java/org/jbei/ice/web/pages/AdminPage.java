package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.panels.adminpage.AdminAccountUpdatePanel;
import org.jbei.ice.web.panels.adminpage.AdminUpdateGroupPanel;
import org.jbei.ice.web.panels.adminpage.StorageSchemeChoicePanel;
import org.jbei.ice.web.panels.adminpage.EditGroupPanel;
import org.jbei.ice.web.panels.adminpage.EditPartsPanel;
import org.jbei.ice.web.panels.adminpage.EditUserAccountPanel;

public class AdminPage extends ProtectedPage {
    private static final String MAIN_PANEL_ID = "centerPanel";

    private Component currentPanel;

    private BookmarkablePageLink<Object> editUsersLink;
    private BookmarkablePageLink<Object> editPartsLink;
    private BookmarkablePageLink<Object> editGroupsLink;
    private BookmarkablePageLink<Object> editStorageSchemeLink;

    private String currentPage;

    public AdminPage(PageParameters parameters) {
        super(parameters);

        Boolean isModerator = false;

        try {
            isModerator = AccountController.isModerator(IceSession.get().getAccount());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        if (!isModerator) {
            setResponsePage(WelcomePage.class);
        }

        initializeControls(parameters);
    }

    @Override
    public String getTitle() {
        return "Admin - " + super.getTitle();
    }

    private void initializeControls(PageParameters parameters) {

        currentPage = parameters.getString("0");

        // edit users tab
        editUsersLink = new BookmarkablePageLink<Object>("editUsersLink", AdminPage.class,
                new PageParameters("0=users"));
        editUsersLink.setOutputMarkupId(true);

        // edit parts tab
        editPartsLink = new BookmarkablePageLink<Object>("editPartsLink", AdminPage.class,
                new PageParameters("0=parts"));
        editPartsLink.setOutputMarkupId(true);

        // edit groups tab
        editGroupsLink = new BookmarkablePageLink<Object>("editGroupsLink", AdminPage.class,
                new PageParameters("0=groups"));
        editGroupsLink.setOutputMarkupId(true);

        // edit location scheme tab
        editStorageSchemeLink = new BookmarkablePageLink<Object>("editStorageSchemeLink",
                AdminPage.class, new PageParameters("0=locations"));
        editStorageSchemeLink.setOutputMarkupId(true);

        // set tabs css
        editUsersLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        editPartsLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        editGroupsLink.add(new SimpleAttributeModifier("class", "inactive"))
                .setOutputMarkupId(true);
        editStorageSchemeLink.add(new SimpleAttributeModifier("class", "inactive"))
                .setOutputMarkupId(true);

        add(editUsersLink);
        add(editPartsLink);
        add(editGroupsLink);
        add(editStorageSchemeLink);

        if (currentPage != null) {
            if ("users".equals(currentPage)) {
                String email = parameters.getString("1");
                if (email != null && !email.isEmpty()) {
                    Account account;
                    try {
                        account = AccountController.getByEmail(email);
                    } catch (ControllerException e) {
                        throw new ViewException(e);
                    }
                    currentPanel = new AdminAccountUpdatePanel(MAIN_PANEL_ID, account);
                } else {
                    currentPanel = new EditUserAccountPanel(MAIN_PANEL_ID);
                }
                editUsersLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
            } else if ("parts".equals(currentPage)) {
                currentPanel = new EditPartsPanel(MAIN_PANEL_ID);
                editPartsLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
            } else if ("groups".equals(currentPage)) {
                String uuid = parameters.getString("1");
                Group group = null;
                if (uuid != null && !uuid.isEmpty()) {
                    try {
                        group = GroupManager.get(uuid);
                    } catch (ManagerException e) {
                        throw new ViewException(e);
                    }
                    currentPanel = new AdminUpdateGroupPanel(MAIN_PANEL_ID, group);
                } else {
                    currentPanel = new EditGroupPanel(MAIN_PANEL_ID);
                }
                editGroupsLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
            } else if ("locations".equals(currentPage)) {
                currentPanel = new StorageSchemeChoicePanel(MAIN_PANEL_ID);
                editStorageSchemeLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
            } else {
                currentPanel = createDefaultPanel();
                editUsersLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
            }
        } else {
            currentPanel = createDefaultPanel();
            editUsersLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                true);
        }
        currentPanel.setOutputMarkupId(true);
        add(currentPanel);
    }

    protected Panel createDefaultPanel() {
        return new EditUserAccountPanel(MAIN_PANEL_ID);
    }
}
