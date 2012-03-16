package org.jbei.ice.client.entry.view.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Widget for displaying the entry permissions
 * 
 * @author Hector Plahar
 */

public class PermissionsDisplayWidget extends Composite {

    private final FlexTable layout;

    public PermissionsDisplayWidget() {

        layout = new FlexTable();
        initWidget(layout);
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("permissions_display");

        addHeaderLabel();
        addPermissions();
    }

    private void addHeaderLabel() {
        layout.setHTML(0, 0, "Permissions");
        layout.getCellFormatter().setStyleName(0, 0, "permissions_sub_header");
    }

    public void addPermissions() {
        Tree tree = new Tree();
        tree.setStyleName("font-75em");
        TreeItem root = new TreeItem("Read Allowed");

        root.addItem(new TreeItem("JBEI"));
        tree.addItem(root);

        layout.setWidget(1, 0, tree);
        root.setState(true, false);

        Tree rwTree = new Tree();
        rwTree.setStyleName("font-75em");
        TreeItem rwTreeRoot = new TreeItem("Read/Write Allowed");
        rwTree.addItem(rwTreeRoot);
        rwTreeRoot.addItem(new TreeItem("Only you"));

        layout.setWidget(2, 0, rwTree);
        rwTreeRoot.setState(true, false);
    }

}
