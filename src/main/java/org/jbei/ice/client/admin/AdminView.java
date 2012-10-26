package org.jbei.ice.client.admin;

import org.jbei.ice.client.admin.widget.AdminViewMenu;
import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class AdminView extends AbstractLayout {

    private FlexTable mainContent;
    private AdminViewMenu menu;
    private HTML contentHeader;

    @Override
    protected void initComponents() {
        super.initComponents();
        mainContent = new FlexTable();
        menu = new AdminViewMenu();
        menu.addStyleName("margin-top-20");
        contentHeader = new HTML("<span style=\"margin-left: 14px; font-size: 12px; "
                                         + " background-color: orange; color: #fefefe;"
                                         + "text-shadow: #333 1px 1px 1px;"
                                         + " padding: 5px; -webkit-border-radius: 2px; border-radius: 2px;"
                                         + "-moz-border-radius: 2px;\"><b>Site Administration</b></span>");
    }

    public void setTabWidget(AdminTab tab, AdminPanel<?> view) {
//        panel.insert(view, view.getTabTitle(), tab.ordinal());
    }

    public SingleSelectionModel<AdminOption> getUserSelectionModel() {
        return this.menu.getSelectionModel();
    }

    @Override
    protected Widget createContents() {
        mainContent.setWidth("100%");
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidget(0, 0, contentHeader);
        mainContent.setWidget(1, 0, menu);
//        mainContent.setHTML(0, 1, "&nbsp;");

        mainContent.getFlexCellFormatter().setRowSpan(0, 1, 2);
        mainContent.getFlexCellFormatter().setWidth(1, 0, "200px");

//        mainContent.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        mainContent.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);

        return mainContent;
    }
}
