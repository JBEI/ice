package org.jbei.ice.client.profile.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.group.GroupMembersWidget;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.profile.group.widget.CreateGroupCell;
import org.jbei.ice.client.profile.group.widget.EditGroupCell;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;

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
    private ServiceDelegate<GroupInfo> delegate;
    private ServiceDelegate<GroupInfo> editGroupDelegate;
    private ServiceDelegate<String> emailVerifierDelegate;
    private Delegate<GroupInfo> groupSelectionDelegate; // used to notify presenter which group is being worked on
    private GroupInfo currentGroup;

    public GroupsListWidget() {
        groupList = new FlexTable();
        groupList.setCellPadding(0);
        groupList.setCellSpacing(0);
        groupList.getFlexCellFormatter().setWidth(0, 0, "400px");

        newGroupCell = new CreateGroupCell();
        groupList.setWidget(0, 0, newGroupCell);
        groupList.getFlexCellFormatter().setVisible(0, 0, false);
        groupList.getFlexCellFormatter().setStyleName(0, 0, "bg_gray_with_border");
        newGroupCell.setVisible(false);

        groupMembers = new GroupMembersWidget();
        groupMembers.setVisible(false);
        groupMembers.setWidth("400px");

        layout = new FlexTable();
        initWidget(layout);

        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setStyleName("margin-top-20");
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

                String email = createGroupMembersWidget.getRegisteredUserEmailInput();
                emailVerifierDelegate.execute(email);
            }
        });
    }

    public GroupInfo getCurrentGroup() {
        return this.currentGroup;
    }

    public void addVerifiedAccount(AccountInfo accountInfo) {
        createGroupMembersWidget.addVerifiedMember(accountInfo);
    }

    public void setDeleteGroupDelegate(ServiceDelegate<GroupInfo> deleteGroupDelegate) {
        delegate = deleteGroupDelegate;
    }

    public void setGroupSelectionDelegate(Delegate<GroupInfo> selectionDelegate) {
        this.groupSelectionDelegate = selectionDelegate;
    }

    public void setDeleteGroupMemberDelegate(ServiceDelegate<AccountInfo> deleteGroupMemberDelegate) {
        groupMembers.setDeleteMemberDelegate(deleteGroupMemberDelegate);
    }

    public void setEditGroupDelegate(ServiceDelegate<GroupInfo> editGroupDelegate) {
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

    public ArrayList<AccountInfo> getSelectedMembers() {
        return addMembersWidget.getSelectedMembers();
    }

    public void setSaveHandler(ClickHandler handler) {
        addMembersWidget.setSaveHandler(handler);
    }

    public void setCreateNewHandler(ClickHandler handler) {
        createGroupMembersWidget.setSaveHandler(handler);
    }

    public GroupInfo getGroupSelection(ClickEvent event) {
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
        return ((Cell) widget).getInfo();
    }

    public void setAvailableAccounts(ArrayList<AccountInfo> available) {
        Collections.sort(available, new Comparator<AccountInfo>() {
            @Override
            public int compare(AccountInfo o1, AccountInfo o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        });
        createGroupMembersWidget.setAvailableAccounts(available);
        addMembersWidget.setAvailableAccounts(available);
    }

    public void setGroupMembers(GroupInfo info, ArrayList<AccountInfo> members) {
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

            if (info.getId() == cell.getInfo().getId()) {
                cell.updateGroupCount(info.getMemberCount());
                return;
            }
        }
    }

    public void setSelectionHandler(ClickHandler handler) {
        groupList.addClickHandler(handler);
    }

    public void setGroupList(ArrayList<GroupInfo> list) {
        groupMembers.setMemberList(new ArrayList<AccountInfo>());  // reset
        groupMembers.setVisible(false);

        for (int i = 0; i < list.size(); i += 1) {
            Cell cell = new Cell(list.get(i));
            groupList.setWidget(i + 1, 0, cell);
            groupList.getFlexCellFormatter().setStyleName(i + 1, 0, "group_info_td");
        }
    }

    public void addGroup(GroupInfo info) {
        groupMembers.setMemberList(new ArrayList<AccountInfo>());  // reset
        groupMembers.setVisible(false);

        Cell cell = new Cell(info);
        int row = groupList.getRowCount();
        groupList.setWidget(row, 0, cell);
        groupList.getFlexCellFormatter().setStyleName(row, 0, "group_info_td");
    }

    public void removeGroup(GroupInfo info) {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            Widget widget = groupList.getWidget(i, 0);
            if (!(widget instanceof Cell))
                continue;

            if (info.getId() == ((Cell) widget).getInfo().getId()) {
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

    public GroupInfo getNewGroup(GroupType type) {
        GroupInfo info = new GroupInfo();
        if (!newGroupCell.validate())
            return null;

        info.setType(type);
        info.setDescription(newGroupCell.getGroupDescription());
        info.setLabel(newGroupCell.getGroupName());
        info.setMembers(createGroupMembersWidget.getSelectedMembers());
        return info;
    }

    public void setVerifyUserEmailDelegate(ServiceDelegate<String> serviceDelegate) {
        emailVerifierDelegate = serviceDelegate;
    }

    public void removeGroupMember(GroupInfo group, AccountInfo info) {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            Widget widget = groupList.getWidget(i, 0);
            if (!(widget instanceof Cell))
                continue;

            Cell groupCell = (Cell) widget;

            if (group.getId() == groupCell.getInfo().getId()) {
                groupCell.updateGroupCount(group.getMemberCount());
                groupMembers.removeMember(info);
                return;
            }
        }
    }

    //
    // inner classes
    //
    private class Cell extends Composite {

        private final GroupInfo info;
        private final HTML addUser;
        private final HTML editGroup;
        private final HTML deleteGroup;
        private FlexTable panel;

        public Cell(GroupInfo info) {
            this.info = info;
            panel = new FlexTable();
            panel.setWidth("100%");
            panel.setCellPadding(0);
            panel.setCellSpacing(0);

            initWidget(panel);
            String desc = info.getDescription().isEmpty() ? "No description" : info.getDescription();

            HTMLPanel htmlPanel = new HTMLPanel(
                    "<span id=\"" + info.getUuid() + "\"></span> <b>" + info.getLabel() + "</b>"
                            + "<br><span style=\"color: #777; font-size: 10px; top: -5px; position: relative;\">"
                            + desc + " | <b style=\"color: #222\">" + info.getMemberCount() + "</b> members</span>");
            this.panel.setWidget(0, 0, htmlPanel);
            this.panel.getFlexCellFormatter().setWidth(0, 0, "300px");

            // add
            addUser = new HTML("<i class=\"" + FAIconType.USER.getStyleName() + "\"></i>"
                                       + "<i style=\"vertical-align: text-bottom; font-size: 9px\" class=\""
                                       + FAIconType.PLUS.getStyleName() + "\"></i>");
            addUser.setStyleName("display-inline");
            addUser.addStyleName("add_icon");
            addUser.setTitle("Add Group Member");

            // edit
            editGroup = new HTML("<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i>");
            editGroup.setStyleName("display-inline");
            editGroup.addStyleName("edit_icon");
            editGroup.setTitle("Edit Group");

            // delete
            deleteGroup = new HTML("<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i>");
            deleteGroup.setStyleName("display-inline");
            deleteGroup.addStyleName("delete_icon");
            deleteGroup.setTitle("Delete Group");

            HTMLPanel actionPanel = new HTMLPanel("<span id=\"add_user\"></span>"
                                                          + "&nbsp;<span style=\"color: #DDD\">|</span>&nbsp;"
                                                          + "<span id=\"edit_group\"></span>"
                                                          + "&nbsp;<span style=\"color: #DDD\">|</span>&nbsp;"
                                                          + "<span id=\"delete_group\"></span> &nbsp;");
            actionPanel.setStyleName("action_panel");
            actionPanel.add(addUser, "add_user");
            actionPanel.add(editGroup, "edit_group");
            actionPanel.add(deleteGroup, "delete_group");

            this.panel.setWidget(0, 1, actionPanel);
            setClickHandlers();
        }

        public void updateGroupCount(long newCount) {
            info.setMemberCount(newCount);
            String desc = info.getDescription().isEmpty() ? "No description" : info.getDescription();
            HTMLPanel htmlPanel = new HTMLPanel(
                    "<span id=\"" + info.getUuid() + "\"></span> <b>" + info.getLabel() + "</b>"
                            + "<br><span style=\"color: #777; font-size: 10px; top: -5px; position: relative;\">"
                            + desc + " | <b style=\"color: #222\">" + info.getMemberCount() + "</b> members</span>");
            this.panel.setWidget(0, 0, htmlPanel);
        }

        public GroupInfo getInfo() {
            return info;
        }

        protected void setClickHandlers() {
            addUser.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    HTMLTable.Cell cell = groupList.getCellForEvent(event);
                    if (cell == null || cell.getCellIndex() > 0)
                        return;

                    groupMembers.setVisible(false);
                    final Cell widget = (Cell) groupList.getWidget(cell.getRowIndex(), cell.getCellIndex());
                    groupSelectionDelegate.execute(widget.getInfo());
                    setSelected(cell.getRowIndex(), false);
                    groupList.getFlexCellFormatter().setVisible(0, 0, false);
                    addMembersWidget.setVisible(true);
                    layout.setWidget(0, 1, addMembersWidget);
                    layout.getFlexCellFormatter().setStyleName(0, 1, "bg_gray_with_border");
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
                    final EditGroupCell editGroupCell = new EditGroupCell(widget.getInfo());
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

                    delegate.execute(widget.getInfo());
                }
            });
        }
    }
}
