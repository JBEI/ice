package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.HasData;

public class EditGroupsPanel extends Composite implements AdminPanel {

    private ScrollPanel panel;

    public EditGroupsPanel() {
        panel = new ScrollPanel();
        initWidget(panel);

        //        grid = new UserTable();
        //        grid.setWidth("100%");
        //
        //        VerticalPanel vPanel = new VerticalPanel();
        //        vPanel.setWidth("100%");
        //        vPanel.add(grid);
        //        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        //        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        //        pager.setDisplay(grid);
        //        vPanel.add(pager);
        //        vPanel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);

        //        panel.add(vPanel);
    }

    @Override
    public String getTabTitle() {
        return "Groups Management";
    }

    @Override
    public HasData<AccountInfo> getDisplay() {
        // TODO Auto-generated method stub
        return null;
    }

}
