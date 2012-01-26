package org.jbei.ice.client.collection.view;

import org.jbei.ice.client.collection.ICollectionEntriesView;
import org.jbei.ice.client.collection.menu.CollectionEntryMenu;
import org.jbei.ice.client.collection.menu.CollectionUserMenu;
import org.jbei.ice.client.collection.table.CollectionEntriesDataTable;
import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class CollectionsEntriesView extends AbstractLayout implements ICollectionEntriesView {

    private CollectionEntriesDataTable table;
    private CollectionEntryMenu systemMenu;
    private CollectionUserMenu userMenu;
    private FlexTable contents;
    private FlexTable rightContents;
    private Widget selectionWidget;

    public CollectionsEntriesView() {
    }

    @Override
    protected void initComponents() {
        rightContents = new FlexTable();
        rightContents.setCellPadding(0);
        rightContents.setCellSpacing(0);
        rightContents.setWidth("100%");
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
        contents.getCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

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
    private Widget getTablePanel() {

        if (selectionWidget != null) {
            rightContents.setWidget(0, 0, selectionWidget);
            rightContents.setHTML(1, 0, "&nbsp;");
        }

        // data table
        rightContents.setWidget(2, 0, table);
        rightContents.getFlexCellFormatter().setColSpan(2, 0, 2);

        // table pager
        rightContents.setWidget(3, 0, table.getPager());
        rightContents.getFlexCellFormatter().setColSpan(3, 0, 2);

        return rightContents;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setDataView(CollectionEntriesDataTable table) {
        this.table = table;
        contents.setWidget(0, 2, getTablePanel());
    }

    @Override
    public CollectionEntryMenu getSystemCollectionMenu() {
        return this.systemMenu;
    }

    @Override
    public void setFeedback(Widget feedback) {
        rightContents.setWidget(0, 1, feedback);
        rightContents.getFlexCellFormatter()
                .setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);
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
