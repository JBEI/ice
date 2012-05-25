package org.jbei.ice.client.admin.usermanagement;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;

public class EditUserPanel extends Composite implements AdminPanel {

    private ScrollPanel panel;
    private final UserTable grid;

    public EditUserPanel() {

        panel = new ScrollPanel();
        initWidget(panel);

        grid = new UserTable();
        grid.setWidth("100%");

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        vPanel.add(grid);
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(grid);
        vPanel.add(pager);
        vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);

        panel.add(vPanel);
    }

    @Override
    public String getTabTitle() {
        return "User Management";
    }

    //    public void setData(ArrayList<AccountInfo> data) {
    //        ListHandler<AccountInfo> sortHandler = new ListHandler<AccountInfo>(data);
    //        grid.addColumnSortHandler(sortHandler);
    //        initTableColumns(sortHandler);
    //        grid.setRowCount(data.size(), true);
    //        grid.setRowData(0, data);
    //    }

    @Override
    public HasData<AccountInfo> getDisplay() {
        return this.grid;
    }
}
