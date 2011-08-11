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
import org.jbei.ice.web.panels.AdminFoldersEditPanel;
import org.jbei.ice.web.panels.VerifyBulkImportPanel;
import org.jbei.ice.web.panels.VerifyUserBulkImportPanel;
import org.jbei.ice.web.panels.adminpage.AdminAccountUpdatePanel;
import org.jbei.ice.web.panels.adminpage.AdminImportExportPanel;
import org.jbei.ice.web.panels.adminpage.AdminUpdateGroupPanel;
import org.jbei.ice.web.panels.adminpage.EditGroupPanel;
import org.jbei.ice.web.panels.adminpage.EditPartsPanel;
import org.jbei.ice.web.panels.adminpage.EditUserAccountPanel;
import org.jbei.ice.web.panels.adminpage.StorageSchemeChoicePanel;

/**
 * Page for performing administrative tasks. Requires "Moderator" privilege
 * 
 * @author Hector Plahar
 */
public class AdminPage extends ProtectedPage {
    private static final String MAIN_PANEL_ID = "centerPanel";

    private Component currentPanel;

    private BookmarkablePageLink<AdminPage> editUsersLink;
    private BookmarkablePageLink<AdminPage> editPartsLink;
    private BookmarkablePageLink<AdminPage> editGroupsLink;
    private BookmarkablePageLink<AdminPage> editStorageSchemeLink;
    private BookmarkablePageLink<AdminPage> editFoldersLink;
    private BookmarkablePageLink<AdminPage> importExportLink;
    private BookmarkablePageLink<AdminPage> editBulkImportLink;

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
            setResponsePage(FoldersPage.class);
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
        editUsersLink = new BookmarkablePageLink<AdminPage>("editUsersLink", AdminPage.class,
                new PageParameters("0=users"));
        editUsersLink.setOutputMarkupId(true);

        // edit parts tab
        editPartsLink = new BookmarkablePageLink<AdminPage>("editPartsLink", AdminPage.class,
                new PageParameters("0=parts"));
        editPartsLink.setOutputMarkupId(true);

        // edit groups tab
        editGroupsLink = new BookmarkablePageLink<AdminPage>("editGroupsLink", AdminPage.class,
                new PageParameters("0=groups"));
        editGroupsLink.setOutputMarkupId(true);

        // edit location scheme tab
        editStorageSchemeLink = new BookmarkablePageLink<AdminPage>("editStorageSchemeLink",
                AdminPage.class, new PageParameters("0=locations"));

        // edit folders tab
        editFoldersLink = new BookmarkablePageLink<AdminPage>("editFoldersLink", AdminPage.class,
                new PageParameters("0=folders"));
        editFoldersLink.setOutputMarkupId(true);

        // import export tab
        importExportLink = new BookmarkablePageLink<AdminPage>("importExportLink", AdminPage.class,
                new PageParameters("0=import"));
        importExportLink.setOutputMarkupId(true);

        // edit bulk import
        editBulkImportLink = new BookmarkablePageLink<AdminPage>("editBulkImportLink",
                AdminPage.class, new PageParameters("0=bulk_import"));
        editBulkImportLink.setOutputMarkupId(true);

        // set tabs css
        editUsersLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        editPartsLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        editGroupsLink.add(new SimpleAttributeModifier("class", "inactive"))
                .setOutputMarkupId(true);
        editStorageSchemeLink.add(new SimpleAttributeModifier("class", "inactive"))
                .setOutputMarkupId(true);
        editFoldersLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(
            true);
        importExportLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(
            true);

        editBulkImportLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(
            true);

        add(editUsersLink);
        add(editPartsLink);
        add(editGroupsLink);
        add(editStorageSchemeLink);
        add(editFoldersLink);
        add(importExportLink);
        add(editBulkImportLink);

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
            } else if ("folders".equals(currentPage)) {
                currentPanel = new AdminFoldersEditPanel(MAIN_PANEL_ID);
                editFoldersLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
            } else if ("import".equals(currentPage)) {
                currentPanel = new AdminImportExportPanel(MAIN_PANEL_ID);
                importExportLink.add(new SimpleAttributeModifier("class", "active"));
            } else if ("bulk_import".equals(currentPage)) {
                String importId = parameters.getString("1");
                if (importId == null || importId.isEmpty())
                    currentPanel = new VerifyBulkImportPanel(MAIN_PANEL_ID);
                else
                    currentPanel = new VerifyUserBulkImportPanel(MAIN_PANEL_ID, importId);

                editBulkImportLink.add(new SimpleAttributeModifier("class", "active"))
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
