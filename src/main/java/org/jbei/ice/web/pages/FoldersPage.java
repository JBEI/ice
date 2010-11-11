package org.jbei.ice.web.pages;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.FolderManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Folder;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.panels.EmptyWorkspaceMessagePanel;
import org.jbei.ice.web.panels.FolderDataTablePanel;
import org.jbei.ice.web.panels.MostRecentEntriesPanel;
import org.jbei.ice.web.panels.UserEntriesViewPanel;
import org.jbei.ice.web.panels.UserRecentlyViewedPanel;
import org.jbei.ice.web.panels.UserSamplesViewPanel;
import org.jbei.ice.web.panels.WorkspaceTablePanel;

/**
 * Page for showing Folder contents
 * and entry collections
 * 
 * @author Hector Plahar
 */
public class FoldersPage extends ProtectedPage {

    private Panel currentPanel;

    private static final String MY_ENTRIES = "my_entries";
    private static final String ALL_ENTRIES = "all";
    private static final String RECENTLY_VIEWED = "recent";
    private static final String WORKSPACE_ENTRIES = "workspace";
    private static final String SAMPLES = "samples";

    private BookmarkablePageLink<FoldersPage> myEntriesLink;
    private BookmarkablePageLink<FoldersPage> allEntriesLink;
    private BookmarkablePageLink<FoldersPage> recentEntriesLink;
    private BookmarkablePageLink<FoldersPage> workspaceLink;
    private BookmarkablePageLink<FoldersPage> samplesLink;

    private final List<AjaxLink<Folder>> links = new LinkedList<AjaxLink<Folder>>();

    public FoldersPage(PageParameters params) {
        super(params);

        // entries links
        myEntriesLink = new BookmarkablePageLink<FoldersPage>("my_entries", this.getClass(),
                new PageParameters("0=" + MY_ENTRIES));
        myEntriesLink.setOutputMarkupId(true);

        allEntriesLink = new BookmarkablePageLink<FoldersPage>("all_entries", this.getClass(),
                new PageParameters("0=" + ALL_ENTRIES));
        allEntriesLink.setOutputMarkupId(true);

        recentEntriesLink = new BookmarkablePageLink<FoldersPage>("recent", this.getClass(),
                new PageParameters("0=" + RECENTLY_VIEWED));
        recentEntriesLink.setOutputMarkupId(true);

        workspaceLink = new BookmarkablePageLink<FoldersPage>("workspace", this.getClass(),
                new PageParameters("0=" + WORKSPACE_ENTRIES));
        workspaceLink.setOutputMarkupId(true);

        samplesLink = new BookmarkablePageLink<FoldersPage>("samples", this.getClass(),
                new PageParameters("0=" + SAMPLES));
        samplesLink.setOutputMarkupId(true);

        initialize(params);

        add(myEntriesLink);
        add(allEntriesLink);
        add(recentEntriesLink);
        add(workspaceLink);
        add(samplesLink);

        // list of folders
        addFolderList();
    }

    protected void addFolderList() {
        links.clear();
        ListView<Folder> listView = new ListView<Folder>("folders", new FolderListModel()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Folder> item) {
                final Folder dir = item.getModelObject();
                FolderLink link = new FolderLink("link", currentPanel, dir);
                link.add(new Label("link_caption", dir.getName()));
                links.add(link);
                item.add(link);
            }
        };

        add(listView);
    }

    private class FolderLink extends AjaxLink<Folder> {

        private static final long serialVersionUID = 1L;
        private Panel panel;
        private Folder folder;

        public FolderLink(String id, Panel panel, Folder folder) {
            super(id);
            this.panel = panel;
            this.folder = folder;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            clearEntriesLinksCSS(target);
            panel = new FolderDataTablePanel("panel", folder);
            panel.setOutputMarkupId(true);
            addActiveLink2(this);
            FoldersPage.this.replace(panel);
            target.addComponent(panel);

            for (AjaxLink<Folder> link : links)
                target.addComponent(link);
        }
    }

    private static class FolderListModel extends LoadableDetachableModel<List<Folder>> {

        private static final long serialVersionUID = 1L;

        @Override
        protected List<Folder> load() {
            try {
                Account systemAccount = AccountManager.getSystemAccount();
                return FolderManager.getFoldersByOwner(systemAccount);
            } catch (ManagerException e) {
                throw new ViewException(e);
            }
        }
    }

    protected void initialize(PageParameters params) {
        if (params == null || params.size() == 0) {
            currentPanel = createUserEntriesPanel();
        } else {
            String dataType = params.getString("0");
            if (MY_ENTRIES.equals(dataType)) {
                currentPanel = createUserEntriesPanel();
            } else if (RECENTLY_VIEWED.equals(dataType)) {
                currentPanel = createRecentlyViewedPanel();
            } else if (ALL_ENTRIES.equals(dataType)) {
                currentPanel = new MostRecentEntriesPanel("panel", 15);
                currentPanel.setOutputMarkupId(true);
                addActiveLink(allEntriesLink);
            } else if (WORKSPACE_ENTRIES.equals(dataType)) {
                currentPanel = createWorkspacePanel();
            } else if (SAMPLES.equals(dataType)) {
                currentPanel = createSamplesPanel();
            } else {
                currentPanel = new UserEntriesViewPanel("panel");
                addActiveLink(myEntriesLink);
            }
        }

        currentPanel.setOutputMarkupId(true);
        add(currentPanel);
    }

    private void clearEntriesLinksCSS(AjaxRequestTarget target) {

        target.addComponent(recentEntriesLink);
        target.addComponent(allEntriesLink);
        target.addComponent(workspaceLink);
        target.addComponent(myEntriesLink);
        target.addComponent(samplesLink);
    }

    private void addActiveLink(BookmarkablePageLink<FoldersPage> link) {
        SimpleAttributeModifier modifier = new SimpleAttributeModifier("class", "active") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isTemporary() {
                return true;
            }
        };
        link.add(modifier);
    }

    private void addActiveLink2(FolderLink link) { // TODO fold into method above. near duplicate
        SimpleAttributeModifier modifier = new SimpleAttributeModifier("class", "active") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isTemporary() {
                return true;
            }
        };
        link.setOutputMarkupId(true);
        link.add(modifier);
    }

    private Panel createUserEntriesPanel() {
        addActiveLink(myEntriesLink);
        return new UserEntriesViewPanel("panel");
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
            workspacePanel = new WorkspaceTablePanel("panel");
            workspacePanel.setOutputMarkupId(true);
        } else {
            workspacePanel = new EmptyWorkspaceMessagePanel("panel");
        }

        addActiveLink(workspaceLink);
        return workspacePanel;
    }

    private Panel createSamplesPanel() {
        UserSamplesViewPanel userSamplesViewPanel = new UserSamplesViewPanel("panel");
        userSamplesViewPanel.setOutputMarkupId(true);

        addActiveLink(samplesLink);
        return userSamplesViewPanel;
    }

    private Panel createRecentlyViewedPanel() {
        UserRecentlyViewedPanel userRecentlyViewedPanel = new UserRecentlyViewedPanel("panel");
        userRecentlyViewedPanel.setOutputMarkupId(true);
        addActiveLink(recentEntriesLink);
        return userRecentlyViewedPanel;
    }
}
