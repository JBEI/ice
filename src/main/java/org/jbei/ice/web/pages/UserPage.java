package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.panels.EmptyWorkspaceMessagePanel;
import org.jbei.ice.web.panels.UserEntriesViewPanel;
import org.jbei.ice.web.panels.UserProjectsViewPanel;
import org.jbei.ice.web.panels.UserRecentlyViewedPanel;
import org.jbei.ice.web.panels.UserSamplesViewPanel;
import org.jbei.ice.web.panels.WorkspaceTablePanel;

public class UserPage extends ProtectedPage {
    public Component currentPanel;
    public Component entriesPanel;
    public Component samplesPanel;
    public Component workspacePanel;
    public Component recentlyViewedPanel;
    public Component projectsPanel;

    public BookmarkablePageLink<Object> entriesLink;
    public BookmarkablePageLink<Object> samplesLink;
    public BookmarkablePageLink<Object> workspaceLink;
    public BookmarkablePageLink<Object> recentlyViewedLink;
    public BookmarkablePageLink<Object> projectsLink;

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
        recentlyViewedLink = new BookmarkablePageLink<Object>("recentlyViewedLink", UserPage.class,
                new PageParameters("0=recent"));
        recentlyViewedLink.setOutputMarkupId(true);
        projectsLink = new BookmarkablePageLink<Object>("projectsLink", UserPage.class,
                new PageParameters("0=projects"));
        projectsLink.setOutputMarkupId(true);

        projectsLink.setVisible(false); // TODO: Uncomment this to see projects tab

        updateTab();

        add(entriesLink);
        add(samplesLink);
        add(workspaceLink);
        add(recentlyViewedLink);
        add(projectsLink);

        if (currentPage != null && currentPage.equals("samples")) {
            currentPanel = createSamplesPanel();
        } else if (currentPage != null && currentPage.equals("workspace")) {
            currentPanel = createWorkspacePanel();
        } else if (currentPage != null && currentPage.equals("recent")) {
            currentPanel = createRecentlyViewedPanel();
        } else if (currentPage != null && currentPage.equals("projects")) {
            currentPanel = createProjectsPanel();
        } else {
            currentPanel = createEntriesPanel();
        }

        add(currentPanel);
    }

    private void updateTab() {
        entriesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        samplesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        workspaceLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        recentlyViewedLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(
            true);
        projectsLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);

        if (currentPage != null && currentPage.equals("samples")) {
            samplesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else if (currentPage != null && currentPage.equals("workspace")) {
            workspaceLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                true);
        } else if (currentPage != null && currentPage.equals("recent")) {
            recentlyViewedLink.add(new SimpleAttributeModifier("class", "active"))
                    .setOutputMarkupId(true);
        } else if (currentPage != null && currentPage.equals("projects")) {
            projectsLink.add(new SimpleAttributeModifier("class", "active"))
                    .setOutputMarkupId(true);
        } else {
            entriesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
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
        long workspaces = 0;
        try {
            workspaces = WorkspaceManager.getCountByAccount(IceSession.get().getAccount());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        Panel workspacePanel = null;
        if (workspaces > 0) {

            workspacePanel = new WorkspaceTablePanel("centerPanel");
            workspacePanel.setOutputMarkupId(true);
        } else {
            workspacePanel = new EmptyWorkspaceMessagePanel("centerPanel");
        }
        return workspacePanel;
    }

    private Panel createProjectsPanel() {
        UserProjectsViewPanel userProjectsViewPanel = new UserProjectsViewPanel("centerPanel");

        userProjectsViewPanel.setOutputMarkupId(true);

        return userProjectsViewPanel;
    }

    private Panel createRecentlyViewedPanel() {
        UserRecentlyViewedPanel userRecentlyViewedPanel = new UserRecentlyViewedPanel("centerPanel");
        userRecentlyViewedPanel.setOutputMarkupId(true);
        return userRecentlyViewedPanel;
    }

    @Override
    protected String getTitle() {
        return "My Entries - " + super.getTitle();
    }
}
