package org.jbei.ice.client.admin.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.group.GroupInfo;

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

    private GroupInfo root;
    private FlexTable groupList;
    private int row;
    private HashMap<Integer, GroupInfo> mapping;
    private final GroupMembersWidget groupMembers;

    public GroupsWidget(ServiceDelegate<AccountInfo> delegate) {
        groupList = new FlexTable();
        groupList.setCellPadding(0);
        groupList.setCellSpacing(0);
        groupList.getFlexCellFormatter().setWidth(0, 0, "250px");
        mapping = new HashMap<Integer, GroupInfo>();
        groupMembers = new GroupMembersWidget(delegate);
        groupMembers.setVisible(false);
        HorizontalPanel layout = new HorizontalPanel();
        initWidget(layout);
        layout.setStyleName("margin-top-10");

        layout.add(groupList);
        layout.add(groupMembers);
    }

    public GroupInfo getGroupSelection(ClickEvent event) {
        HTMLTable.Cell cell = groupList.getCellForEvent(event);
        if (cell == null || cell.getCellIndex() > 0)
            return null;

        setSelected(cell.getRowIndex());
        return mapping.get(cell.getRowIndex());
    }

    public void setGroupMembers(ArrayList<AccountInfo> members) {
        groupMembers.setMemberList(members);
        groupMembers.setVisible(true);
    }

    public void setClickHandler(ClickHandler handler) {
        groupList.addClickHandler(handler);
    }

    public void setRootGroup(GroupInfo list) {
        this.root = list;
        mapping.clear();
        groupMembers.setMemberList(new ArrayList<AccountInfo>());  // reset
        groupMembers.setVisible(false);
        row = 0;
    }

    public void showDisplay() {
        displayGroup(root, 0);

//        // test
//        GroupInfo newGroup = new GroupInfo();
//        newGroup.setLabel("Test");
//        newGroup.setDescription("This is a test of the widget add child blab");
//        newGroup.setParentId(root.getId());
//        addNewGroup(newGroup);
//
//        GroupInfo newGroup2 = new GroupInfo();
//        newGroup2.setLabel("Test2");
//        newGroup2.setDescription("This is a test of the widget add child blab2");
//        newGroup2.setParentId(newGroup.getId());
//        addNewGroup(newGroup2);
//
//        GroupInfo newGroup1 = new GroupInfo();
//        newGroup1.setLabel("Test1");
//        newGroup1.setDescription("This is a test of the widget add child blab1");
//        newGroup1.setParentId(root.getId());
//        addNewGroup(newGroup1);
    }

    private void displayGroup(GroupInfo info, int level) {
        if (info == null)
            return;

        boolean expandParent = true && info.getChildren().isEmpty();
        draw(row, info, level, expandParent);
        level += 5;

        for (GroupInfo child : info.getChildren()) {
            row += 1;
            draw(row, child, level, false);
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

    public void addNewGroup(GroupInfo newGroup) {
        long parentId = newGroup.getParentId();
        int parentRow = -1;

        for (Map.Entry<Integer, GroupInfo> entry : mapping.entrySet()) {
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
        draw(row, newGroup, cell.getLevel() + 1, true);
    }

    private void draw(int row, GroupInfo info, int level, boolean expand) {
        Cell cell = new Cell(level, info, expand);
        groupList.setWidget(row, 0, cell);
        groupList.getFlexCellFormatter().setStyleName(row, 0, "group_info_td");
        mapping.put(row, info);
    }

    private class Cell extends Composite {

        private final int level;
        private final GroupInfo info;
        private boolean expand;

        public Cell(int level, GroupInfo info, boolean expand) {

            this.level = level;
            this.info = info;
            this.expand = expand;

            HTMLPanel panel = new HTMLPanel("<div style=\"margin-left: " + (level * 15)
                                                    + "px\"><span style=\"float: left; margin-right: 5px;\" id=\""
                                                    + info.getUuid() + "\"></span> <b>"
                                                    + info.getLabel()
                                                    + "</b><i class=\""
                                                    + FAIconType.CHEVRON_RIGHT.getStyleName() + "\"></i>"
                                                    + "<br><span style=\"color: #888; font-size: 0.62em; top: "
                                                    + "-5px; position: relative; left: 16px\">"
                                                    + info.getDescription() + "</span></div>");
            initWidget(panel);

            Icon icon;
            if (expand)
                icon = new Icon(FAIconType.CARET_DOWN);
            else
                icon = new Icon(FAIconType.CARET_RIGHT);
            panel.add(icon, info.getUuid());

            icon.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.stopPropagation();
                }
            });
        }

        public int getLevel() {
            return level;
        }
    }
}
