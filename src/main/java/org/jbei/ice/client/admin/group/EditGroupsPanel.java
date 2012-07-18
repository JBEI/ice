package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminTab;
import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;

/**
 * Panel for editing groups. Also acts as the view
 *
 * @author Hector Plahar
 */
public class EditGroupsPanel extends Composite implements AdminPanel<GroupInfo> {

    private ScrollPanel scrollPanel;
    private GroupTable grid;
    private Label label;
    private VerticalPanel vPanel;
    private final AdminTab tab;

    public EditGroupsPanel() {
        scrollPanel = new ScrollPanel();
        initWidget(scrollPanel);

        this.tab = AdminTab.GROUPS;
        initComponents();

        vPanel.add(label);
        vPanel.add(grid);
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(grid);
        vPanel.add(pager);
        vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);

        scrollPanel.add(vPanel);
    }

    protected void initComponents() {
        grid = new GroupTable();
        grid.setWidth("100%");

        label = new Label("Add New Group");

        vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
    }

    @Override
    public String getTabTitle() {
        return "Groups Management";
    }

    @Override
    public HasData<GroupInfo> getDisplay() {
        return grid;
    }

    @Override
    public AdminTab getTab() {
        return this.tab;
    }
}
