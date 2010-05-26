package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.AuthenticatedPermissionManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class MiniPermissionViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private final String PERMISSION_URL_KEY = "permission";

    Entry entry = null;

    public MiniPermissionViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        add(new BookmarkablePageLink<Object>("permissionPageLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + PERMISSION_URL_KEY)));

        List<String> readAllowed = getReadAllowed();
        List<String> writeAllowed = getWriteAllowed();

        BookmarkablePageLink<Object> moreReadableLink = new BookmarkablePageLink<Object>(
                "moreReadableLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()
                        + ",1=" + PERMISSION_URL_KEY));
        moreReadableLink.setVisible(false);
        BookmarkablePageLink<Object> moreWritableLink = new BookmarkablePageLink<Object>(
                "moreWritableLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()
                        + ",1=" + PERMISSION_URL_KEY));
        moreWritableLink.setVisible(false);

        int listLimit = 4;
        if (readAllowed.size() > listLimit) {
            readAllowed = readAllowed.subList(0, listLimit);
            moreReadableLink.setVisible(true);
        }
        if (writeAllowed.size() > listLimit) {
            writeAllowed = writeAllowed.subList(0, listLimit);
            moreWritableLink.setVisible(true);
        }

        ListView<String> readableList = new ListView<String>("readableList", readAllowed) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                String itemLabel = item.getModelObject();
                item.add(new Label("readableItem", itemLabel));
            }

        };

        ListView<String> writableList = new ListView<String>("writableList", writeAllowed) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                String itemLabel = item.getModelObject();
                item.add(new Label("writableItem", itemLabel));
            }
        };

        add(readableList);
        add(writableList);

        add(moreReadableLink);
        add(moreWritableLink);
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public List<String> getReadAllowed() {
        ArrayList<String> readAllowed = new ArrayList<String>();

        Set<Account> readAccounts = null;
        Set<Group> readGroups = null;
        try {
            readAccounts = AuthenticatedPermissionManager.getReadUser(this.entry);
            readGroups = AuthenticatedPermissionManager.getReadGroup(this.entry);
        } catch (ManagerException e) {

        } catch (PermissionException e) {
            throw new ViewPermissionException(e);
        }

        for (Group group : readGroups) {
            readAllowed.add(group.getLabel());
        }
        for (Account account : readAccounts) {
            readAllowed.add(account.getFullName());
        }

        return readAllowed;

    }

    public List<String> getWriteAllowed() {
        ArrayList<String> writeAllowed = new ArrayList<String>();

        Set<Account> writeAccounts = null;
        Set<Group> writeGroups = null;
        try {
            writeAccounts = AuthenticatedPermissionManager.getWriteUser(this.entry);
            writeGroups = AuthenticatedPermissionManager.getWriteGroup(this.entry);
        } catch (ManagerException e) {
            throw new ViewPermissionException(e);
        } catch (PermissionException e) {
            throw new ViewPermissionException(e);
        }

        for (Group group : writeGroups) {
            writeAllowed.add(group.getLabel());
        }
        for (Account account : writeAccounts) {
            writeAllowed.add(account.getFullName());
        }
        return writeAllowed;
    }

}
