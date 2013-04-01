package org.jbei.ice.client.admin.user;

import org.jbei.ice.client.admin.IAdminPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UserPanel extends Composite implements IAdminPanel {

    private ScrollPanel panel;
    private final UserTable table;

    public UserPanel() {
        panel = new ScrollPanel();
        initWidget(panel);

        table = new UserTable();
        table.setWidth("100%");

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        vPanel.add(table);
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(table);
        vPanel.add(pager);
        vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);

        panel.add(vPanel);
    }

    public UserTable getUserTable() {
        return this.table;
    }
}
