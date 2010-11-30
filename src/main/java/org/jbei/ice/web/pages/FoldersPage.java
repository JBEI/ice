package org.jbei.ice.web.pages;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.PageParameters;
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
    private static final String FOLDERS = "id";

    private BookmarkablePageLink<FoldersPage> myEntriesLink;
    private BookmarkablePageLink<FoldersPage> allEntriesLink;
    private BookmarkablePageLink<FoldersPage> recentEntriesLink;
    private BookmarkablePageLink<FoldersPage> workspaceLink;
    private BookmarkablePageLink<FoldersPage> samplesLink;
    private List<FolderLink> folderLinks;

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

        add(myEntriesLink);
        add(allEntriesLink);
        add(recentEntriesLink);
        add(workspaceLink);
        add(samplesLink);

        // collection links
        FolderListModel model = new FolderListModel(this.getClass());
        addFolderList(model);

        folderLinks = model.getObject();

        initialize(params);
    }

    protected void addFolderList(FolderListModel model) {

        ListView<FolderLink> listView = new ListView<FolderLink>("folders", model) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<FolderLink> item) {
                item.add(item.getModelObject().getLink());
            }
        };

        add(listView);
    }

    /**
     * Detachable model for retrieving the system folders.
     */
    private static class FolderListModel extends LoadableDetachableModel<List<FolderLink>> {

        private static final long serialVersionUID = 1L;
        private final Class<? extends FoldersPage> clazz;

        public FolderListModel(Class<? extends FoldersPage> class1) {
            super();
            this.clazz = class1;
        }

        @Override
        protected List<FolderLink> load() {
            try {
                List<FolderLink> links = new LinkedList<FolderLink>();

                Account systemAccount = AccountManager.getSystemAccount();
                int i = 0;
                for (Folder folder : FolderManager.getFoldersByOwner(systemAccount)) {
                    links.add(new FolderLink(folder, this.clazz, i));
                    i += 1;
                }
                return links;
            } catch (ManagerException e) {
                throw new ViewException(e);
            }
        }
    }

    private static class FolderLink implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Folder folder;
        private final BookmarkablePageLink<FoldersPage> link;

        public FolderLink(Folder folder, Class<? extends FoldersPage> clazz, int index) {
            this.folder = folder;
            link = new BookmarkablePageLink<FoldersPage>("link", clazz, new PageParameters("0="
                    + FOLDERS + ", 1=" + index));
            link.add(new Label("link_caption", folder.getName()));
        }

        public BookmarkablePageLink<FoldersPage> getLink() {
            return link;
        }

        public Folder getFolder() {
            return this.folder;
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
            } else if (FOLDERS.equals(dataType)) {
                String linkIndex = params.getString("1");
                currentPanel = createFolderPanel(linkIndex);
            } else {
                currentPanel = new UserEntriesViewPanel("panel");
                addActiveLink(myEntriesLink);
            }
        }

        currentPanel.setOutputMarkupId(true);
        add(currentPanel);
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

    private Panel createFolderPanel(String linkIndex) {

        int index = 0;

        try {
            index = Integer.decode(linkIndex);
        } catch (NumberFormatException nfe) {
            return new UserEntriesViewPanel("panel");
        }

        Folder folder = folderLinks.get(index).getFolder();
        BookmarkablePageLink<FoldersPage> activeLink = folderLinks.get(index).getLink();

        if (folder == null)
            return new UserEntriesViewPanel("panel");

        addActiveLink(activeLink);
        Panel panel = new FolderDataTablePanel("panel", folder);
        panel.setOutputMarkupId(true);
        return panel;
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
