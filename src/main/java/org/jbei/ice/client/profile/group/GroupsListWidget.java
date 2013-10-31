package org.jbei.ice.client.profile.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.group.GroupMembersWidget;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.profile.group.widget.CreateGroupCell;
import org.jbei.ice.client.profile.group.widget.EditGroupCell;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget that lists groups that are available for user (or created by user)
 *
 * @author Hector Plahar
 */
public class GroupsListWidget extends Composite {

    private FlexTable groupList;
    private final GroupMembersWidget groupMembers;
    private FlexTable layout;
    private final GroupAddMembersWidget addMembersWidget;
    private final GroupAddMembersWidget createGroupMembersWidget;
    private final CreateGroupCell newGroupCell;
    private ServiceDelegate<UserGroup> deleteGroupDelegate;
    private ServiceDelegate<UserGroup> editGroupDelegate;
    private ServiceDelegate<String> emailVerifierDelegate;
    private ServiceDelegate<UserGroup> retrieveMembersDelegate;
    private Mode mode;
    private final boolean addOption;

    public GroupsListWidget(boolean addOptions) {
        this.addOption = addOptions;
        groupList = new FlexTable();
        groupList.setCellPadding(0);
        groupList.setCellSpacing(0);
        groupList.getFlexCellFormatter().setWidth(0, 0, "400px");

        // create and hide widget for create new group
        newGroupCell = new CreateGroupCell();
        groupList.setWidget(0, 0, newGroupCell);
        groupList.getFlexCellFormatter().setVisible(0, 0, false);
        groupList.getFlexCellFormatter().setStyleName(0, 0, "bg_gray_with_border");
        newGroupCell.setVisible(false);

        groupMembers = new GroupMembersWidget();
        groupMembers.setWidth("400px");

        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setWidget(0, 0, groupList);
        layout.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        layout.setWidget(0, 1, groupMembers);
        layout.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        // add members to group
        addMembersWidget = new GroupAddMembersWidget();
        addMembersWidget.setCancelHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setAddGroupVisibility(false);
                addMembersWidget.reset();
            }
        });

        // create group
        createGroupMembersWidget = new GroupAddMembersWidget();
        createGroupMembersWidget.setCancelHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setCreateGroupVisibility(false);
                createGroupMembersWidget.reset();
            }
        });

        createGroupMembersWidget.setAddUserClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (emailVerifierDelegate == null)
                    return;

                String email = createGroupMembersWidget.getRegisteredUserEmailInput().trim();
                if (email == null || email.isEmpty())
                    return;

                emailVerifierDelegate.execute(email);
            }
        });

        addMembersWidget.setAddUserClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (emailVerifierDelegate == null)
                    return;

                mode = Mode.ADDING_MEMBER;
                String email = addMembersWidget.getRegisteredUserEmailInput().trim();
                if (email == null || email.isEmpty())
                    return;

                emailVerifierDelegate.execute(email);
            }
        });

        // default mode of viewing group list
        mode = Mode.VIEW_GROUP_LIST;
    }

    public void addVerifiedAccount(User user) {
        if (mode == Mode.ADDING_MEMBER)
            addMembersWidget.addVerifiedMember(user);
        else
            createGroupMembersWidget.addVerifiedMember(user);
    }

    public void setDeleteGroupDelegate(ServiceDelegate<UserGroup> deleteGroupDelegate) {
        this.deleteGroupDelegate = deleteGroupDelegate;
    }

    public void setDeleteGroupMemberDelegate(ServiceDelegate<User> deleteGroupMemberDelegate) {
        groupMembers.setDeleteMemberDelegate(deleteGroupMemberDelegate);
    }

    public void setEditGroupDelegate(ServiceDelegate<UserGroup> editGroupDelegate) {
        this.editGroupDelegate = editGroupDelegate;
    }

    public void setCreateGroupVisibility(boolean visible) {
        createGroupMembersWidget.setVisible(visible);
        newGroupCell.setVisible(visible);
        clearSelection();
        groupList.getFlexCellFormatter().setVisible(0, 0, visible);
        if (visible) {
            layout.setWidget(0, 1, createGroupMembersWidget);
            layout.getFlexCellFormatter().setVisible(0, 1, true);
            layout.getFlexCellFormatter().setStyleName(0, 1, "bg_gray_with_border");
        } else
            layout.getFlexCellFormatter().removeStyleName(0, 1, "bg_gray_with_border");
    }

    public void setAddGroupVisibility(boolean visible) {
        addMembersWidget.setVisible(visible);
        newGroupCell.setVisible(visible);
        clearSelection();
        groupList.getFlexCellFormatter().setVisible(0, 0, visible);
        if (visible) {
            layout.setWidget(0, 1, addMembersWidget);
            layout.getFlexCellFormatter().setVisible(0, 1, true);
            layout.getFlexCellFormatter().setStyleName(0, 1, "bg_gray_with_border");
        } else
            layout.getFlexCellFormatter().removeStyleName(0, 1, "bg_gray_with_border");
    }

    public ArrayList<User> getSelectedMembers() {
        return addMembersWidget.getSelectedMembers();
    }

    public void setSaveHandler(final ClickHandler handler) {
        addMembersWidget.setSaveHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onClick(event);
                mode = Mode.VIEW_GROUP_LIST;
            }
        });
    }

    public void setCreateNewHandler(ClickHandler handler) {
        createGroupMembersWidget.setSaveHandler(handler);
    }

    public UserGroup getGroupSelection(ClickEvent event) {
        if (newGroupCell.isVisible())
            return null;

        groupMembers.setVisible(false);
        HTMLTable.Cell cell = groupList.getCellForEvent(event);
        if (cell == null || cell.getCellIndex() > 0)
            return null;

        setSelected(cell.getRowIndex(), true);
        if (cell.getRowIndex() == 0)
            return null;

        Widget widget = groupList.getWidget(cell.getRowIndex(), cell.getCellIndex());
        if (!(widget instanceof Cell))
            return null;
        return ((Cell) widget).getUser();
    }

    public void setAvailableAccounts(ArrayList<User> available) {
        Collections.sort(available, new Comparator<User>() {

            @Override
            public int compare(User o1, User o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        });
        createGroupMembersWidget.setAvailableAccounts(available);
        addMembersWidget.setAvailableAccounts(available);
    }

    public void setGroupMembers(UserGroup user, ArrayList<User> members) {
        if (mode == null || mode == Mode.VIEW_GROUP_LIST || mode == Mode.VIEW_GROUP_MEMBERS) {
            groupMembers.setMemberList(members);
            groupMembers.setVisible(true);
            layout.setWidget(0, 1, groupMembers);
            layout.getFlexCellFormatter().setVisible(0, 1, true);
            layout.getFlexCellFormatter().removeStyleName(0, 1, "bg_gray_with_border");

            for (int i = 0; i < groupList.getRowCount(); i += 1) {
                Widget widget = groupList.getWidget(i, 0);
                if (!(widget instanceof Cell))
                    continue;

                Cell cell = (Cell) widget;

                if (user.getId() == cell.getUser().getId()) {
                    cell.updateGroupCount(user.getMemberCount());
                    return;
                }
            }
        } else if (mode == Mode.ADDING_MEMBER) {
            addMembersWidget.setSelectedMembers(members);
        }
    }

    public void setSelectionHandler(ServiceDelegate<UserGroup> delegate) {
        if (delegate == null)
            return;

        retrieveMembersDelegate = delegate;
        groupList.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mode = Mode.VIEW_GROUP_MEMBERS;
                UserGroup user = getGroupSelection(event);
                retrieveMembersDelegate.execute(user);
            }
        });
    }

    public void setGroupList(ArrayList<UserGroup> list) {
        groupMembers.setMemberList(new ArrayList<User>());  // reset
        groupMembers.setVisible(false);

        for (int i = 0; i < list.size(); i += 1) {
            Cell cell = new Cell(list.get(i));
            groupList.setWidget(i + 1, 0, cell);
            groupList.getFlexCellFormatter().setStyleName(i + 1, 0, "group_info_td");
        }
    }

    public void addGroup(UserGroup user) {
        groupMembers.setMemberList(new ArrayList<User>());  // reset
        groupMembers.setVisible(false);

        Cell cell = new Cell(user);
        int row = groupList.getRowCount();
        groupList.setWidget(row, 0, cell);
        groupList.getFlexCellFormatter().setStyleName(row, 0, "group_info_td");
    }

    public void removeGroup(UserGroup user) {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            Widget widget = groupList.getWidget(i, 0);
            if (!(widget instanceof Cell))
                continue;

            if (user.getId() == ((Cell) widget).getUser().getId()) {
                groupList.removeRow(i);
                return;
            }
        }
    }

    protected void setSelected(int row, boolean showGroupMembers) {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            if (i == row) {
                groupList.getFlexCellFormatter().addStyleName(i, 0, "group_info_td_selected");
                if (showGroupMembers) {
                    layout.setWidget(0, 1, groupMembers);
                    layout.getFlexCellFormatter().setVisible(0, 1, true);
                }
            } else
                groupList.getFlexCellFormatter().removeStyleName(i, 0, "group_info_td_selected");
        }
    }

    protected void clearSelection() {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            groupList.getFlexCellFormatter().removeStyleName(i, 0, "group_info_td_selected");
        }
    }

    public UserGroup getNewGroup(GroupType type) {
        UserGroup user = new UserGroup();
        if (!newGroupCell.validate())
            return null;

        user.setType(type);
        user.setDescription(newGroupCell.getGroupDescription());
        user.setLabel(newGroupCell.getGroupName());
        user.setMembers(createGroupMembersWidget.getSelectedMembers());
        return user;
    }

    public void setVerifyUserEmailDelegate(ServiceDelegate<String> serviceDelegate) {
        emailVerifierDelegate = serviceDelegate;
    }

    public void removeGroupMember(UserGroup userGroup, User info) {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            Widget widget = groupList.getWidget(i, 0);
            if (!(widget instanceof Cell))
                continue;

            Cell groupCell = (Cell) widget;

            if (userGroup.getId() == groupCell.getUser().getId()) {
                groupCell.updateGroupCount(userGroup.getMemberCount());
                groupMembers.removeMember(info);
                return;
            }
        }
    }

    //
    // inner classes
    //
    private enum Mode {
        ADDING_MEMBER, EDIT_GROUP, DELETE_GROUP, VIEW_GROUP_LIST, VIEW_GROUP_MEMBERS
    }

    private class Cell extends Composite {

        private final UserGroup user;
        private FlexTable panel;

        public Cell(UserGroup user) {
            this.user = user;
            panel = new FlexTable();
            panel.setWidth("100%");
            panel.setCellPadding(0);
            panel.setCellSpacing(0);

            initWidget(panel);
            String desc = user.getDescription().isEmpty() ? "No description" : user.getDescription();

            HTMLPanel htmlPanel = new HTMLPanel(
                    "<span id=\"" + user.getUuid() + "\"></span> <b>" + user.getLabel() + "</b>"
                            + "<br><span style=\"color: #777; font-size: 10px; top: -5px; position: relative;\">"
                            + desc + " | <b style=\"color: #222\">" + user.getMemberCount() + "</b> members</span>");
            this.panel.setWidget(0, 0, htmlPanel);
            this.panel.getFlexCellFormatter().setWidth(0, 0, "300px");

            HTMLPanel actionPanel;

            if (addOption) {
                // add
                HTML addUser = new HTML("<i class=\"" + FAIconType.USER.getStyleName() + "\"></i>"
                                                + "<i style=\"vertical-align: text-bottom; font-size: 9px\" class=\""
                                                + FAIconType.PLUS.getStyleName() + "\"></i>");
                addUser.setStyleName("display-inline");
                addUser.addStyleName("add_icon");
                addUser.setTitle("Add Group Member");

                // edit
                HTML editGroup = new HTML("<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i>");
                editGroup.setStyleName("display-inline");
                editGroup.addStyleName("edit_icon");
                editGroup.setTitle("Edit Group");

                // delete
                HTML deleteGroup = new HTML("<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i>");
                deleteGroup.setStyleName("display-inline");
                deleteGroup.addStyleName("delete_icon");
                deleteGroup.setTitle("Delete Group");

                actionPanel = new HTMLPanel("<span id=\"add_user\"></span>"
                                                    + "&nbsp;<span style=\"color: #DDD\">|</span>&nbsp;"
                                                    + "<span id=\"edit_group\"></span>"
                                                    + "&nbsp;<span style=\"color: #DDD\">|</span>&nbsp;"
                                                    + "<span id=\"delete_group\"></span> &nbsp;");

                actionPanel.add(addUser, "add_user");
                actionPanel.add(editGroup, "edit_group");
                actionPanel.add(deleteGroup, "delete_group");

                setClickHandlers(addUser, editGroup, deleteGroup);
            } else {
                actionPanel = new HTMLPanel("&nbsp;");
            }

            actionPanel.setStyleName("action_panel");
            this.panel.setWidget(0, 1, actionPanel);
            this.panel.getFlexCellFormatter().setWidth(0, 1, "90px");
        }

        public void updateGroupCount(long newCount) {
            user.setMemberCount(newCount);
            String desc = user.getDescription().isEmpty() ? "No description" : user.getDescription();
            HTMLPanel htmlPanel = new HTMLPanel(
                    "<span id=\"" + user.getUuid() + "\"></span> <b>" + user.getLabel() + "</b>"
                            + "<br><span style=\"color: #777; font-size: 10px; top: -5px; position: relative;\">"
                            + desc + " | <b style=\"color: #222\">" + user.getMemberCount() + "</b> members</span>");
            this.panel.setWidget(0, 0, htmlPanel);
        }

        public UserGroup getUser() {
            return user;
        }

        protected void setClickHandlers(HTML addUser, HTML editGroup, HTML deleteGroup) {
            addUser.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    HTMLTable.Cell cell = groupList.getCellForEvent(event);
                    if (cell == null || cell.getCellIndex() > 0)
                        return;

                    //set action mode
                    mode = Mode.ADDING_MEMBER;
                    // hide any group members that are being displayed
                    groupMembers.setVisible(false);
                    // get current cell
                    final Cell widget = (Cell) groupList.getWidget(cell.getRowIndex(), cell.getCellIndex());
                    // retrieve the existing group members
                    retrieveMembersDelegate.execute(widget.getUser());
                    // highlight current selection and hide all others
                    setSelected(cell.getRowIndex(), false);
                    // hide ??
                    groupList.getFlexCellFormatter().setVisible(0, 0, false);
                    // show the add members widget (this will get updated with the existing members for the group)
                    addMembersWidget.setVisible(true);
                    layout.setWidget(0, 1, addMembersWidget);
                    layout.getFlexCellFormatter().setVisible(0, 1, true);
                    layout.getFlexCellFormatter().setStyleName(0, 1, "bg_gray_with_border");
                    // do not propagate click
                    event.stopPropagation();
                }
            });

            editGroup.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();
                    final HTMLTable.Cell cell = groupList.getCellForEvent(event);
                    if (cell == null || cell.getCellIndex() > 0)
                        return;

                    layout.getFlexCellFormatter().setVisible(0, 1, false);
                    final Cell widget = (Cell) groupList.getWidget(cell.getRowIndex(), cell.getCellIndex());
                    final EditGroupCell editGroupCell = new EditGroupCell(widget.getUser());
                    groupList.setWidget(cell.getRowIndex(), cell.getCellIndex(), editGroupCell);

                    editGroupCell.addCancelHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            event.stopPropagation();
                            groupList.setWidget(cell.getRowIndex(), cell.getCellIndex(), widget);
                            groupList.getFlexCellFormatter().removeStyleName(cell.getRowIndex(), cell.getCellIndex(),
                                                                             "bg_gray_with_border");
                        }
                    });

                    editGroupCell.addSubmitHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            event.stopPropagation();
                            final HTMLTable.Cell cell = groupList.getCellForEvent(event);
                            if (cell == null || cell.getCellIndex() > 0)
                                return;

                            editGroupDelegate.execute(editGroupCell.getGroup());
                            groupMembers.setVisible(false);
                            Cell updatedCell = new Cell(editGroupCell.getGroup());
                            groupList.setWidget(cell.getRowIndex(), cell.getCellIndex(), updatedCell);
                        }
                    });
                }
            });

            deleteGroup.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();
                    final HTMLTable.Cell cell = groupList.getCellForEvent(event);
                    if (cell == null || cell.getCellIndex() > 0)
                        return;

                    groupMembers.setVisible(false);
                    final Cell widget = (Cell) groupList.getWidget(cell.getRowIndex(), cell.getCellIndex());
                    if (!Window.confirm("This action cannot be undone. Continue?"))
                        return;

                    deleteGroupDelegate.execute(widget.getUser());
                }
            });
        }
    }
}
