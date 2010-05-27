package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.AuthenticatedPermissionManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

import edu.emory.mathcs.backport.java.util.Collections;

public class MiniPermissionViewPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private final String PERMISSION_URL_KEY = "permission";

    Entry entry = null;

    public MiniPermissionViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        EntryController entryController = new EntryController(IceSession.get().getAccount());
        WebMarkupContainer editLink = new WebMarkupContainer("editLink");
        editLink.add(new BookmarkablePageLink<Object>("permissionPageLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + PERMISSION_URL_KEY)));
        try {
            editLink.setVisible(entryController.hasWritePermission(entry));
        } catch (ControllerException e1) {
            throw new ViewException(e1);
        }
        add(editLink);

        List<String> readAllowed = getReadAllowed();
        List<String> writeAllowed = getWriteAllowed();
        readAllowed.removeAll(writeAllowed);

        if (readAllowed.size() == 0) {
            readAllowed.add("Only you");
        }
        if (writeAllowed.size() == 0) {
            writeAllowed.add("Only you");
        }

        int listLimit = 4;
        if (readAllowed.size() > listLimit) {
            readAllowed = readAllowed.subList(0, listLimit);
            Panel moreReadableLinkPanel = new MorePermissionLinkPanel("moreReadableLinkPanel",
                    entry);
            add(moreReadableLinkPanel);
        } else {
            add(new EmptyPanel("moreReadableLinkPanel"));
        }

        if (writeAllowed.size() > listLimit) {
            writeAllowed = writeAllowed.subList(0, listLimit);
            Panel moreWritableLinkPanel = new MorePermissionLinkPanel("moreWritableLinkPanel",
                    entry);
            add(moreWritableLinkPanel);
        } else {
            add(new EmptyPanel("moreWritableLinkPanel"));
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
        ArrayList<String> tempArray = new ArrayList<String>();
        for (Group group : readGroups) {
            tempArray.add(group.getLabel());
        }
        Collections.sort(tempArray);
        readAllowed.addAll(tempArray);
        tempArray.clear();
        for (Account account : readAccounts) {
            tempArray.add(account.getFullName());
        }
        Collections.sort(tempArray);
        readAllowed.addAll(tempArray);

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
        ArrayList<String> tempArray = new ArrayList<String>();
        for (Group group : writeGroups) {
            tempArray.add(group.getLabel());
        }
        Collections.sort(tempArray);
        writeAllowed.addAll(tempArray);
        tempArray.clear();
        for (Account account : writeAccounts) {
            tempArray.add(account.getFullName());
        }
        Collections.sort(tempArray);
        writeAllowed.addAll(tempArray);
        return writeAllowed;
    }

}
