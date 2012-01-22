package org.jbei.ice.client.collection.view;

import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.menu.CollectionEntryMenu;
import org.jbei.ice.client.collection.menu.CollectionUserMenu;
import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryTablePager;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CollectionsEntriesView extends AbstractLayout implements ICollectionEntriesView {

    private DataTable<?> table;
    private CollectionEntryMenu systemMenu;
    private CollectionUserMenu userMenu;
    private FlexTable contents;
    private VerticalPanel tablePanel;
    private Widget selectionWidget;

    public CollectionsEntriesView() {
    }

    @Override
    protected void initComponents() {
        tablePanel = new VerticalPanel();
        tablePanel.setWidth("100%");
    }

    @Override
    protected Widget createContents() {
        contents = new FlexTable();
        contents.setWidth("100%");
        contents.setCellSpacing(0);
        contents.setCellPadding(0);

        // systems collections menu
        systemMenu = new CollectionEntryMenu();
        contents.setWidget(0, 0, systemMenu);

        // separator menus
        contents.setHTML(1, 0, "&nbsp;");

        // user collection menu
        userMenu = new CollectionUserMenu();
        contents.setWidget(2, 0, userMenu);
        contents.getCellFormatter().setVerticalAlignment(2, 0, HasAlignment.ALIGN_TOP);

        // separator between menu and content
        contents.setHTML(0, 1, "&nbsp;&nbsp;&nbsp;&nbsp;");
        contents.getFlexCellFormatter().setRowSpan(0, 1, 3);

        // data table
        contents.setWidget(0, 2, new HTML("&nbsp;")); // place holder while content is set by the presenter
        contents.getFlexCellFormatter().setRowSpan(0, 2, 3);
        contents.getCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);
        contents.getCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_RIGHT);
        contents.getCellFormatter().setWidth(0, 2, "100%");
        return contents;
    }

    /**
     * @return table of results panel
     */
    private VerticalPanel getTablePanel() {

        tablePanel.clear();

        if (selectionWidget != null) {
            tablePanel.add(selectionWidget);
            tablePanel.add(new HTML("&nbsp;"));
        }

        tablePanel.add(table);
        EntryTablePager tablePager = new EntryTablePager();
        tablePager.setDisplay(table);
        tablePanel.add(tablePager);

        return tablePanel;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setDataView(DataTable<?> table) {
        this.table = table;
        contents.setWidget(0, 2, getTablePanel());
    }

    @Override
    public CollectionEntryMenu getSystemCollectionMenu() {
        return this.systemMenu;
    }

    @Override
    public CollectionUserMenu getUserCollectionMenu() {
        return this.userMenu;
    }

    @Override
    public void setCollectionSubMenu(Widget widget) {
        selectionWidget = widget;
    }
}
