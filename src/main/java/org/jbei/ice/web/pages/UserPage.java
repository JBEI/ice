package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.EmptyFramedMessagePanel;
import org.jbei.ice.web.panels.UserEntriesViewPanel;
import org.jbei.ice.web.panels.UserSamplesViewPanel;
import org.jbei.ice.web.panels.WorkspaceTablePanel;

public class UserPage extends ProtectedPage {
    public Component currentPanel;
    public Component entriesPanel;
    public Component samplesPanel;
    public Component workspacePanel;

    public BookmarkablePageLink<Object> entriesLink;
    public BookmarkablePageLink<Object> samplesLink;
    public BookmarkablePageLink<Object> workspaceLink;

    public String currentPage = null;

    public UserPage(PageParameters parameters) {
        super(parameters);

        initialize(parameters);
    }

    private void initialize(PageParameters parameters) {
        if (parameters == null || parameters.size() == 0) {
            currentPage = null;
        } else {
            currentPage = parameters.getString("0");
        }

        entriesLink = new BookmarkablePageLink<Object>("entriesLink", UserPage.class,
                new PageParameters("0=entries"));
        entriesLink.setOutputMarkupId(true);
        samplesLink = new BookmarkablePageLink<Object>("samplesLink", UserPage.class,
                new PageParameters("0=samples"));
        samplesLink.setOutputMarkupId(true);
        workspaceLink = new BookmarkablePageLink<Object>("workspaceLink", UserPage.class,
                new PageParameters("0=workspace"));
        workspaceLink.setOutputMarkupId(true);

        updateTab();

        add(entriesLink);
        add(samplesLink);
        add(workspaceLink);

        if (currentPage != null && currentPage.equals("samples")) {
            currentPanel = createSamplesPanel();
        } else if (currentPage != null && currentPage.equals("entries")) {
            currentPanel = createEntriesPanel();
        } else {
            currentPanel = createWorkspacePanel();
        }

        add(currentPanel);
    }

    private void updateTab() {
        entriesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        samplesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        workspaceLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);

        if (currentPage != null && currentPage.equals("samples")) {
            samplesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else if (currentPage != null && currentPage.equals("entries")) {
            entriesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else {
            workspaceLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                    true);
        }
    }

    private Panel createEntriesPanel() {
        UserEntriesViewPanel userEntriesViewPanel = new UserEntriesViewPanel("centerPanel");

        userEntriesViewPanel.setOutputMarkupId(true);

        return userEntriesViewPanel;
    }

    private Panel createSamplesPanel() {
        UserSamplesViewPanel userSamplesViewPanel = new UserSamplesViewPanel("centerPanel");

        userSamplesViewPanel.setOutputMarkupId(true);

        return userSamplesViewPanel;
    }

    private Panel createWorkspacePanel() {
        long workspaces = WorkspaceManager.getCountByAccount(IceSession.get().getAccount());
        Panel workspacePanel = null;
        if (workspaces > 0) {

            workspacePanel = new WorkspaceTablePanel("centerPanel");
            workspacePanel.setOutputMarkupId(true);
        } else {
            workspacePanel = new EmptyFramedMessagePanel("centerPanel",
                    "Your workspace is empty! Try adding parts to your workspace.");
        }
        return workspacePanel;
    }

    @Override
    protected String getTitle() {
        return "My Entries - " + super.getTitle();
    }
}
