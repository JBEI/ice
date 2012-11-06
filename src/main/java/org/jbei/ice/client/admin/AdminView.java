package org.jbei.ice.client.admin;

import org.jbei.ice.client.admin.widget.AdminViewMenu;
import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
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
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 1, HasAlignment.ALIGN_TOP);
    }

    public void show(AdminOption selected, Widget widget) {
        mainContent.setWidget(0, 1, createHeaderLabel(selected));
        mainContent.setWidget(1, 1, widget);
    }

    public SingleSelectionModel<AdminOption> getUserSelectionModel() {
        return this.menu.getSelectionModel();
    }

    public void showMenuSelection(AdminOption option) {
        menu.showSelected(option);
    }

    @Override
    protected Widget createContents() {
        mainContent.setWidth("100%");
        mainContent.setCellPadding(0);
        mainContent.setCellSpacing(0);
        mainContent.setWidget(0, 0, contentHeader);

        mainContent.setWidget(1, 0, menu);
        mainContent.getFlexCellFormatter().setWidth(1, 0, "200px");
        mainContent.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        mainContent.getFlexCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
        return mainContent;
    }

    private Widget createHeaderLabel(AdminOption selected) {
        return new HTML("<span style=\"font-size: 1.5em; color: #777; "
                                + "font-weight: bold; text-transform: uppercase;\">"
                                + selected.toString() + "</span><br><span style=\"font-size: " +
                                "11px; font-weight: bold; text-transform: uppercase; position: "
                                + "relative; top: -6px; color: #999\">" + selected.getDescription() + "</span>");
    }
}
