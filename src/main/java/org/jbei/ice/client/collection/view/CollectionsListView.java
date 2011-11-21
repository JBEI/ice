package org.jbei.ice.client.collection.view;

import org.jbei.ice.client.collection.CollectionListPager;
import org.jbei.ice.client.collection.ICollectionListView;
import org.jbei.ice.client.collection.table.CollectionListTable;
import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View for list of collections.
 * 
 * @author Hector Plahar
 */

public class CollectionsListView extends AbstractLayout implements ICollectionListView {

    private CollectionListTable systemCollectionTable;
    private CollectionListTable userCollectionTable;

    private VerticalPanel contentPanel;
    private Button addCollectionButton;
    private FlexTable userTable;

    public CollectionsListView() {
    }

    @Override
    protected void initComponents() {
        systemCollectionTable = new CollectionListTable();
        userCollectionTable = new CollectionListTable();
        addCollectionButton = new Button("Add");

        contentPanel = new VerticalPanel();
        contentPanel.setWidth("100%");
    }

    @Override
    public void showAddCollectionWidget(Widget widget) {
        if (userTable.getRowCount() > 2) {
            hideAddCollectionWidget();
            return;
        }

        Widget existing = userTable.getWidget(1, 0);
        userTable.setWidget(1, 0, widget);
        userTable.getFlexCellFormatter().setColSpan(1, 0, 3);
        userTable.setWidget(2, 0, existing);
        userTable.getFlexCellFormatter().setColSpan(2, 0, 3);
    }

    @Override
    public void hideAddCollectionWidget() {
        userTable.removeRow(1);
    }

    @Override
    public CollectionListTable getDataTable() {
        return this.systemCollectionTable;
    }

    @Override
    public CollectionListTable getUserDataTable() {
        return this.userCollectionTable;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public Button getAddCollectionButton() {
        return this.addCollectionButton;
    }

    @Override
    protected Widget createContents() {

        FlexTable systemTable = new FlexTable();
        systemTable.setWidth("100%");
        systemTable.setWidget(0, 0, new HTML("<b>System Collections</b>"));
        CollectionListPager pager = new CollectionListPager();
        pager.setDisplay(systemCollectionTable);
        systemTable.setWidget(0, 1, pager);
        systemTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_RIGHT);
        systemTable.setWidget(1, 0, systemCollectionTable);
        systemTable.getFlexCellFormatter().setColSpan(1, 0, 2);

        contentPanel.add(systemTable);

        // space between tables
        contentPanel.add(new HTML("&nbsp")); // TODO : use styles

        userTable = new FlexTable();
        userTable.setWidth("100%");
        userTable.setWidget(0, 0, new HTML("<b>User Collections</b>"));
        userTable.setWidget(0, 1, addCollectionButton);
        CollectionListPager userPager = new CollectionListPager();
        userPager.setDisplay(userCollectionTable);
        userTable.setWidget(0, 2, userPager);
        userTable.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_RIGHT);
        userTable.setWidget(1, 0, userCollectionTable);
        userTable.getFlexCellFormatter().setColSpan(1, 0, 3);

        contentPanel.add(userTable);

        return contentPanel;
    }
}
