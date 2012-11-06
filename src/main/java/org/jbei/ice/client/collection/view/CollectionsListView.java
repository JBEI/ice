package org.jbei.ice.client.collection.view;

import org.jbei.ice.client.collection.CollectionListPager;
import org.jbei.ice.client.collection.ICollectionListView;
import org.jbei.ice.client.collection.table.CollectionListTable;
import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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
        super.initComponents();
        systemCollectionTable = new CollectionListTable();
        systemCollectionTable.addStyleName("border-bottom");
        userCollectionTable = new CollectionListTable();
        userCollectionTable.addStyleName("border-bottom");

        addCollectionButton = new Button("Add");
        addCollectionButton.setStyleName("collection_user_add_button");

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
        systemTable.setCellPadding(0);
        systemTable.setCellSpacing(0);
        systemTable.setWidth("100%");
        systemTable.setWidget(0, 0, new HTML("<b>System Collections</b>"));
        systemTable.getFlexCellFormatter().setStyleName(0, 0, "collection_list_header_label");
        systemTable.getFlexCellFormatter().setWidth(0, 0, "160px");

        // system description
        systemTable
                .setWidget(
                        0,
                        1,
                        new HTML(
                                "<span style=\"color: #aaa; padding-left: 20px\" class=\"font-85em\">Collection of " +
                                        "entries created by your administrator and available to all users.</span>"));
        systemTable.getFlexCellFormatter().setStyleName(0, 1, "collection_list_header_description");

        // pager
        CollectionListPager pager = new CollectionListPager();
        pager.setDisplay(systemCollectionTable);
        systemTable.setWidget(0, 2, pager);
        systemTable.getFlexCellFormatter().setStyleName(0, 2, "collection_list_pager_cell");
        systemTable.getFlexCellFormatter().setWidth(0, 2, "120px");

        systemTable.setWidget(1, 0, systemCollectionTable);
        systemTable.getFlexCellFormatter().setColSpan(1, 0, 3);

        contentPanel.add(systemTable);

        // space between tables
        contentPanel.add(new HTML("&nbsp")); // TODO : use styles

        //
        // USER TABLE
        //
        userTable = new FlexTable();
        userTable.setCellPadding(0);
        userTable.setCellSpacing(0);
        userTable.setWidth("100%");
        userTable.setWidget(0, 0, new HTML("<b>My Collections</b>"));
        userTable.getFlexCellFormatter().setStyleName(0, 0, "collection_list_header_label");
        userTable.getFlexCellFormatter().setWidth(0, 0, "160px");

        // description
        String html = "<span style=\"color: #aaa; padding-left: 20px\" class=\"font-85em\">Collection of entries " +
                "created by you."
                + " These are available only to you.</span> &nbsp; &nbsp; <span " +
                "id=\"collection_user_add_button\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(addCollectionButton, "collection_user_add_button");
        userTable.setWidget(0, 1, panel);
        userTable.getFlexCellFormatter().setStyleName(0, 1, "collection_list_header_description");

        // pager
        CollectionListPager userPager = new CollectionListPager();
        userPager.setDisplay(userCollectionTable);
        userTable.setWidget(0, 2, userPager);
        userTable.getFlexCellFormatter().setStyleName(0, 2, "collection_list_pager_cell");
        userTable.getFlexCellFormatter().setWidth(0, 2, "120px");

        // data
        userTable.setWidget(1, 0, userCollectionTable);
        userTable.getFlexCellFormatter().setColSpan(1, 0, 3);

        contentPanel.add(userTable);
        return contentPanel;
    }
}
