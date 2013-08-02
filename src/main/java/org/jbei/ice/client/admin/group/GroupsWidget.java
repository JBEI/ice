package org.jbei.ice.client.admin.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * @author Hector Plahar
 */
public class GroupsWidget extends Composite {

    private UserGroup root;
    private FlexTable groupList;
    private int row;
    private HashMap<Integer, UserGroup> mapping;
    private final GroupMembersWidget groupMembers;

    public GroupsWidget(ServiceDelegate<User> delegate) {
        groupList = new FlexTable();
        groupList.setCellPadding(0);
        groupList.setCellSpacing(0);
        groupList.getFlexCellFormatter().setWidth(0, 0, "250px");
        mapping = new HashMap<Integer, UserGroup>();
        groupMembers = new GroupMembersWidget();
        groupMembers.setVisible(false);
        HorizontalPanel layout = new HorizontalPanel();
        initWidget(layout);
        layout.setStyleName("margin-top-10");

        layout.add(groupList);
        layout.add(groupMembers);
    }

    public UserGroup getGroupSelection(ClickEvent event) {
        HTMLTable.Cell cell = groupList.getCellForEvent(event);
        if (cell == null || cell.getCellIndex() > 0)
            return null;

        setSelected(cell.getRowIndex());
        return mapping.get(cell.getRowIndex());
    }

    public void setGroupMembers(ArrayList<User> members) {
        groupMembers.setMemberList(members);
        groupMembers.setVisible(true);
    }

    public void setClickHandler(ClickHandler handler) {
        groupList.addClickHandler(handler);
    }

    public void setRootGroup(UserGroup list) {
        this.root = list;
        mapping.clear();
        groupMembers.setMemberList(new ArrayList<User>());  // reset
        groupMembers.setVisible(false);
        row = 0;
    }

    public void showDisplay() {
        displayGroup(root);
    }

    private void displayGroup(UserGroup user) {
        if (user == null)
            return;

        draw(row, user);

        for (UserGroup child : user.getChildren()) {
            row += 1;
            draw(row, child);
        }
    }

    public void setSelected(int row) {
        for (int i = 0; i < groupList.getRowCount(); i += 1) {
            if (i == row)
                groupList.getFlexCellFormatter().addStyleName(i, 0, "group_info_td_selected");
            else
                groupList.getFlexCellFormatter().removeStyleName(i, 0, "group_info_td_selected");
        }
    }

    public void addNewGroup(UserGroup newUserGroup) {
        long parentId = newUserGroup.getParentId();
        int parentRow = -1;

        for (Map.Entry<Integer, UserGroup> entry : mapping.entrySet()) {
            if (entry.getValue().getId() == parentId) {
                parentRow = entry.getKey().intValue();
                break;
            }
        }

        if (parentRow == -1) {
            return;
        }

        // insert
        Cell cell = (Cell) groupList.getWidget(parentRow, 0);
        int row = groupList.insertRow(parentRow + 1);
        draw(row, newUserGroup);
    }

    private void draw(int row, UserGroup user) {
        Cell cell = new Cell(user);
        groupList.setWidget(row, 0, cell);
        groupList.getFlexCellFormatter().setStyleName(row, 0, "group_info_td");
        mapping.put(row, user);
    }

    private class Cell extends Composite {

        private final UserGroup user;

        public Cell(UserGroup user) {
            this.user = user;

            HTMLPanel panel = new HTMLPanel("<div><b>" + user.getLabel() + "</b><br><span style=\"color: #888; "
                                                    + "font-size: 0.62em; top: -5px; position: relative\">"
                                                    + user.getDescription() + "</span></div>");
            initWidget(panel);
        }

        public UserGroup getGroup() {
            return this.user;
        }
    }
}
