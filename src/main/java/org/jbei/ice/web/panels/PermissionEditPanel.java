package org.jbei.ice.web.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.CollectionModel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.AuthenticatedPermissionManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.pages.EntryViewPage;

public class PermissionEditPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private Entry entry = null;

    private ArrayList<ChoiceItem> choiceItems = new ArrayList<ChoiceItem>();
    private ArrayList<CustomChoice> customChoices = new ArrayList<CustomChoice>();

    private ArrayList<CustomChoice> accountsSelected = new ArrayList<CustomChoice>();
    private ArrayList<CustomChoice> groupsSelected = new ArrayList<CustomChoice>();
    @SuppressWarnings("unchecked")
    private AjaxFallbackLink usersLink = null;
    @SuppressWarnings("unchecked")
    private AjaxFallbackLink groupsLink = null;

    private ListMultipleChoice<CustomChoice> accountsChoiceList = null;
    private ListMultipleChoice<CustomChoice> groupsChoiceList = null;

    private ArrayList<CustomChoice> readAllowed = new ArrayList<CustomChoice>();
    private ArrayList<CustomChoice> writeAllowed = new ArrayList<CustomChoice>();
    private ArrayList<CustomChoice> readAllowedSelected = new ArrayList<CustomChoice>();
    private ArrayList<CustomChoice> writeAllowedSelected = new ArrayList<CustomChoice>();

    @SuppressWarnings("unchecked")
    public PermissionEditPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        // Get all the groups and accounts, and combine them into a large list
        // that will be used later to disambiguate groups and users.
        Set<Account> accounts = null;
        Set<Group> groups = null;
        ArrayList<CustomChoice> accountsChoices = new ArrayList<CustomChoice>();
        ArrayList<CustomChoice> groupsChoices = new ArrayList<CustomChoice>();

        try {
            accounts = AccountManager.getAllByFirstName();
        } catch (ManagerException e) {
            Logger.warn(e.toString());
            accounts = new HashSet<Account>();
        }
        try {
            groups = GroupManager.getAll();
        } catch (ManagerException e) {
            Logger.warn(e.toString());
            groups = new HashSet<Group>();
        }

        for (Account account : accounts) {
            choiceItems.add(new ChoiceItem("account", account.getId()));
            customChoices.add(new CustomChoice(
                    account.getFirstName() + " " + account.getLastName(), ""
                            + (choiceItems.size() - 1)));
            accountsChoices.add(customChoices.get(choiceItems.size() - 1));
        }
        for (Group group : groups) {
            choiceItems.add(new ChoiceItem("group", group.getId()));
            customChoices.add(new CustomChoice(group.getLabel(), "" + (choiceItems.size() - 1)));
            groupsChoices.add(customChoices.get(choiceItems.size() - 1));
        }

        try {
            Set<Account> readAccounts = AuthenticatedPermissionManager.getReadUser(this.entry,
                    IceSession.get().getSessionKey());
            Set<Account> writeAccounts = AuthenticatedPermissionManager.getWriteUser(this.entry,
                    IceSession.get().getSessionKey());
            Set<Group> readGroups = AuthenticatedPermissionManager.getReadGroup(this.entry,
                    IceSession.get().getSessionKey());
            Set<Group> writeGroups = AuthenticatedPermissionManager.getWriteGroup(this.entry,
                    IceSession.get().getSessionKey());

            for (Account account : readAccounts) {
                int choiceItemIndex = -1;
                choiceItemIndex = choiceItems.indexOf(new ChoiceItem("account", account.getId()));
                if (choiceItemIndex > 0) {
                    readAllowed.add(customChoices.get(choiceItemIndex));
                }
            }

            for (Account account : writeAccounts) {
                int choiceItemIndex = -1;
                choiceItemIndex = choiceItems.indexOf(new ChoiceItem("account", account.getId()));
                if (choiceItemIndex > 0) {
                    writeAllowed.add(customChoices.get(choiceItemIndex));
                }
            }

            for (Group group : readGroups) {
                int choiceItemIndex = -1;
                choiceItemIndex = choiceItems.indexOf(new ChoiceItem("group", group.getId()));
                if (choiceItemIndex > 0) {
                    readAllowed.add(customChoices.get(choiceItemIndex));
                }
            }

            for (Group group : writeGroups) {
                int choiceItemIndex = -1;
                choiceItemIndex = choiceItems.indexOf(new ChoiceItem("group", group.getId()));
                if (choiceItemIndex > 0) {
                    writeAllowed.add(customChoices.get(choiceItemIndex));
                }
            }

        } catch (ManagerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // end populate

        groupsChoiceList = generateGroupsChoices(groupsChoices);
        accountsChoiceList = generateUsersChoices(accountsChoices);

        usersLink = new AjaxFallbackLink("usersLink") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                thisPanel.usersLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
                thisPanel.groupsLink.add(new SimpleAttributeModifier("class", "inactive"))
                        .setOutputMarkupId(true);
                getParent().replace(thisPanel.usersLink);
                getParent().replace(thisPanel.groupsLink);
                target.addComponent(thisPanel.usersLink);
                target.addComponent(thisPanel.groupsLink);

                thisPanel.groupsSelected.clear();
                Component temp = thisPanel.accountsChoiceList;
                getParent().replace(temp);
                target.addComponent(temp);
            }

        };

        groupsLink = new AjaxFallbackLink("groupsLink") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                thisPanel.usersLink.add(new SimpleAttributeModifier("class", "inactive"))
                        .setOutputMarkupId(true);
                thisPanel.groupsLink.add(new SimpleAttributeModifier("class", "active"))
                        .setOutputMarkupId(true);
                getParent().replace(thisPanel.usersLink);
                getParent().replace(thisPanel.groupsLink);
                target.addComponent(thisPanel.usersLink);
                target.addComponent(thisPanel.groupsLink);

                thisPanel.accountsSelected.clear();
                Component temp = thisPanel.groupsChoiceList;
                getParent().replace(temp);
                target.addComponent(temp);
            }
        };

        Form form = new StatelessForm("permissionForm") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                // submit handled by savePermssion ajax button
            }
        };

        usersLink.add(new SimpleAttributeModifier("class", "active"));
        groupsLink.add(new SimpleAttributeModifier("class", "inactive"));

        AjaxButton addToReadableButton = new AjaxButton("addToReadableButton", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                for (CustomChoice item : thisPanel.accountsSelected) {
                    if (!thisPanel.readAllowed.contains(item)) {
                        thisPanel.readAllowed.add(item);
                    }
                }
                for (CustomChoice item : thisPanel.groupsSelected) {
                    if (!thisPanel.readAllowed.contains(item)) {
                        thisPanel.readAllowed.add(item);
                    }
                }

                ListMultipleChoice<CustomChoice> temp = thisPanel.getReadAllowedChoices();
                getParent().replace(temp);
                target.addComponent(temp);
            }
        };

        AjaxButton addToWritableButton = new AjaxButton("addToWritableButton", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                for (CustomChoice item : thisPanel.accountsSelected) {
                    if (!thisPanel.writeAllowed.contains(item)) {
                        thisPanel.writeAllowed.add(item);
                        if (!thisPanel.readAllowed.contains(item)) {
                            thisPanel.readAllowed.add(item);
                        }

                    }
                }
                for (CustomChoice item : thisPanel.groupsSelected) {
                    if (!thisPanel.writeAllowed.contains(item)) {
                        thisPanel.writeAllowed.add(item);
                        if (!thisPanel.readAllowed.contains(item))
                            thisPanel.readAllowed.add(item);
                    }
                }
                ListMultipleChoice<CustomChoice> temp = thisPanel.getReadAllowedChoices();
                getParent().replace(temp);
                target.addComponent(temp);
                ListMultipleChoice<CustomChoice> temp2 = thisPanel.getWriteAllowedChoices();
                getParent().replace(temp2);
                target.addComponent(temp2);
            }
        };

        AjaxButton removeFromReadableButton = new AjaxButton("removeFromReadableButton", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                thisPanel.readAllowed.removeAll(thisPanel.readAllowedSelected);
                thisPanel.writeAllowed.removeAll(thisPanel.readAllowedSelected);
                ListMultipleChoice<CustomChoice> temp = thisPanel.getReadAllowedChoices();
                getParent().replace(temp);
                target.addComponent(temp);
                ListMultipleChoice<CustomChoice> temp2 = thisPanel.getWriteAllowedChoices();
                getParent().replace(temp2);
                target.addComponent(temp2);
            }
        };

        AjaxButton removeFromWritableButton = new AjaxButton("removeFromWritableButton", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                thisPanel.writeAllowed.removeAll(thisPanel.writeAllowedSelected);

                ListMultipleChoice<CustomChoice> temp = thisPanel.getWriteAllowedChoices();
                getParent().replace(temp);
                target.addComponent(temp);
            }
        };

        AjaxButton savePermissionButton = new AjaxButton("savePermissionButton", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                PermissionEditPanel thisPanel = (PermissionEditPanel) getParent().getParent();
                HashSet<Account> readAccounts = new HashSet<Account>();
                HashSet<Account> writeAccounts = new HashSet<Account>();
                HashSet<Group> readGroups = new HashSet<Group>();
                HashSet<Group> writeGroups = new HashSet<Group>();

                for (CustomChoice item : thisPanel.readAllowed) {
                    String type = thisPanel.choiceItems.get(Integer.parseInt(item.getValue()))
                            .getKey();
                    Integer id = thisPanel.choiceItems.get(Integer.parseInt(item.getValue()))
                            .getId();

                    System.out.print(type + ":" + id + " ");
                    if (type.equals("account")) {
                        Account account = null;
                        try {
                            account = AccountManager.get(id);
                        } catch (ManagerException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (account != null) {
                            readAccounts.add(account);
                        }
                    } else if (type.equals("group")) {
                        Group group = null;
                        try {
                            group = GroupManager.get(id);
                        } catch (ManagerException e) {
                            e.printStackTrace();
                        }
                        if (group != null) {
                            readGroups.add(group);
                        }
                    }
                }

                for (CustomChoice item : thisPanel.writeAllowed) {
                    String type = thisPanel.choiceItems.get(Integer.parseInt(item.getValue()))
                            .getKey();
                    Integer id = thisPanel.choiceItems.get(Integer.parseInt(item.getValue()))
                            .getId();
                    System.out.print(type + ":" + id + " ");

                    if (type.equals("account")) {
                        Account account = null;
                        try {
                            account = AccountManager.get(id);
                        } catch (ManagerException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (account != null) {
                            readAccounts.add(account);
                            writeAccounts.add(account);
                        }
                    } else if (type.equals("group")) {
                        Group group = null;
                        try {
                            group = GroupManager.get(id);
                        } catch (ManagerException e) {
                            e.printStackTrace();
                        }
                        if (group != null) {
                            readGroups.add(group);
                            writeGroups.add(group);
                        }
                    }

                }

                try {
                    AuthenticatedPermissionManager.setReadGroup(thisPanel.entry, readGroups,
                            IceSession.get().getSessionKey());
                    AuthenticatedPermissionManager.setWriteGroup(thisPanel.entry, writeGroups,
                            IceSession.get().getSessionKey());
                    AuthenticatedPermissionManager.setReadUser(thisPanel.entry, readAccounts,
                            IceSession.get().getSessionKey());
                    AuthenticatedPermissionManager.setWriteUser(thisPanel.entry, writeAccounts,
                            IceSession.get().getSessionKey());
                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + thisPanel.entry.getId() + ",1=" + "permission"));
                    JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);
                } catch (ManagerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (PermissionException e) {
                    String msg = "You do not have permission to edit";
                    thisPanel.error(msg);
                }

            }
        };

        form.add(usersLink.setOutputMarkupId(true));
        form.add(groupsLink.setOutputMarkupId(true));

        form.add(accountsChoiceList);
        form.add(getReadAllowedChoices());
        form.add(getWriteAllowedChoices());

        form.add(addToReadableButton);
        form.add(addToWritableButton);

        form.add(removeFromReadableButton);
        form.add(removeFromWritableButton);

        form.add(savePermissionButton);

        add(form);
        add(new FeedbackPanel("feedback"));
    }

    private static class CustomChoiceComparator implements Comparator<CustomChoice>, Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(CustomChoice arg0, CustomChoice arg1) {
            return arg0.getName().compareToIgnoreCase(arg1.getName());
        }

    }

    public ArrayList<CustomChoice> sortCustomChoiceArrayList(ArrayList<CustomChoice> list) {
        CustomChoiceComparator c = new CustomChoiceComparator();
        Collections.sort(list, c);
        return list;

    }

    private static class ChoiceItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private java.lang.String key;
        private java.lang.Integer id;

        @Override
        public java.lang.String toString() {
            java.lang.String result = null;
            result = getKey() + ": " + getId();
            return result;
        }

        public ChoiceItem(java.lang.String key, Integer id) {
            this.setKey(key);
            this.setId(id);
        }

        @Override
        public boolean equals(Object item) {
            if (item == null) {
                return false;
            }
            if (!(item instanceof ChoiceItem)) {
                return false;
            }
            ChoiceItem temp = (ChoiceItem) item;
            if (getKey().equals(temp.getKey()) && getId().equals(temp.getId())) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int id = 1;
            id = id + getKey().hashCode() / 2 + getId().hashCode() / 2;
            return id;
        }

        public void setKey(java.lang.String key) {
            this.key = key;
        }

        public java.lang.String getKey() {
            return key;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getId() {
            return id;
        }
    }

    public ListMultipleChoice<CustomChoice> generateUsersChoices(
            ArrayList<CustomChoice> accountsChoices) {
        ListMultipleChoice<CustomChoice> accountChoices = new ListMultipleChoice<CustomChoice>(
                "choiceList", new CollectionModel<CustomChoice>(accountsSelected), accountsChoices,
                new ChoiceRenderer<CustomChoice>("name", "value"));

        accountChoices.setMaxRows(15);
        accountChoices.setOutputMarkupId(true);

        return accountChoices;
    }

    public ListMultipleChoice<CustomChoice> generateGroupsChoices(
            ArrayList<CustomChoice> groupsChoices) {
        ListMultipleChoice<CustomChoice> groupChoices = new ListMultipleChoice<CustomChoice>(
                "choiceList", new CollectionModel<CustomChoice>(groupsSelected), groupsChoices,
                new ChoiceRenderer<CustomChoice>("name", "value"));
        groupChoices.setMaxRows(15);
        groupChoices.setOutputMarkupId(true);

        return groupChoices;
    }

    public ListMultipleChoice<CustomChoice> getReadAllowedChoices() {
        ListMultipleChoice<CustomChoice> result = new ListMultipleChoice<CustomChoice>(
                "readAllowedList", new CollectionModel<CustomChoice>(readAllowedSelected),
                sortCustomChoiceArrayList(readAllowed), new ChoiceRenderer<CustomChoice>("name",
                        "value"));

        result.setOutputMarkupId(true);
        return result;
    }

    public ListMultipleChoice<CustomChoice> getWriteAllowedChoices() {
        ListMultipleChoice<CustomChoice> result = new ListMultipleChoice<CustomChoice>(
                "writeAllowedList", new CollectionModel<CustomChoice>(writeAllowedSelected),
                sortCustomChoiceArrayList(writeAllowed), new ChoiceRenderer<CustomChoice>("name",
                        "value"));
        result.setOutputMarkupId(true);
        return result;
    }

}
