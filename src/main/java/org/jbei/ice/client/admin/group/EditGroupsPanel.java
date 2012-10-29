package org.jbei.ice.client.admin.group;

import java.util.ArrayList;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Panel for editing groups. Also acts as the view
 *
 * @author Hector Plahar
 */
public class EditGroupsPanel extends Composite implements AdminPanel {

    private ScrollPanel scrollPanel;
    //    private GroupTable grid;
    private VerticalPanel vPanel;
    private Button createGroup;
    private GroupsWidget widget;

    public EditGroupsPanel() {
        scrollPanel = new ScrollPanel();
        initWidget(scrollPanel);

        initComponents();

        vPanel.add(createGroup);
//        vPanel.add(grid);
//        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
//        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
//        pager.setDisplay(grid);
//
//        vPanel.add(pager);
//        vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);
//        GroupsWidget widget = new GroupsWidget();

        scrollPanel.add(vPanel);
        vPanel.setStyleName("margin-top-20");
    }

    public void setGroups(ArrayList<GroupInfo> groups) {
        if (widget == null)
            widget = new GroupsWidget();

        widget.setGroups(groups);
        widget.showDisplay();
        vPanel.add(widget);
    }

    protected void initComponents() {
//        grid = new GroupTable();
//        grid.setWidth("100%");
//        grid.setStyleName("margin-top-10");

        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");

        createGroup = new Button("<i class=\"" + FAIconType.PLUS.getStyleName() + "\"></i>&nbsp; Create Group");
    }

//    public HasData<GroupInfo> getDataPanel() {
//        return this.grid;
//    }

    private class GroupsWidget extends Composite {

        private ArrayList<GroupInfo> groups;
        private FlexTable table;
        private int row;

        public GroupsWidget() {
            table = new FlexTable();
            table.getFlexCellFormatter().setWidth(0, 0, "300px");
            initWidget(table);
        }

        public void setGroups(ArrayList<GroupInfo> list) {
            this.groups = list;
        }

        public void showDisplay() {
            displayGroup(groups.get(0), 0);

            TextArea area = new TextArea();
            area.setStyleName("input_box");
            area.setVisibleLines(20);

            table.setWidget(0, 2, area);
            table.getFlexCellFormatter().setRowSpan(0, 2, row);
        }

        private void displayGroup(GroupInfo info, int level) {
            if (info == null)
                return;

            draw(info, level);
            GroupInfo child = getChild(info.getId());
            level += 5;
            displayGroup(child, level);
        }

        private void draw(GroupInfo info, int level) {
            table.setWidget(row, 0, new HTML("<b style=\"margin-left: " + level + "px\">" + info.getLabel() + "</b>"
                                                     + "&nbsp;<span style=\"color: #ddd; font-size: 0.65em\">"
                                                     + info.getDescription() + "</span>"));
            table.getFlexCellFormatter().setStyleName(row, 0, "pad-6");
            table.getFlexCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
            table.setWidget(row, 1, new Icon(FAIconType.CHEVRON_RIGHT));
            table.getFlexCellFormatter().setVerticalAlignment(row, 1, HasAlignment.ALIGN_TOP);
            row += 1;
        }

        private GroupInfo getChild(long id) {
            for (GroupInfo info : groups) {
                if (info.getParentId() == id)
                    return info;
            }

            return null;
        }
    }
}
