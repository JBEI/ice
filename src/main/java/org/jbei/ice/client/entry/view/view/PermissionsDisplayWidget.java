package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

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
    private final Tree readTree;
    private final Tree rwTree;

    public PermissionsDisplayWidget() {

        layout = new FlexTable();
        initWidget(layout);
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.addStyleName("permissions_display");

        readTree = new Tree();
        readTree.setStyleName("font-75em");
        readTree.addItem(new TreeItem("Read Allowed"));
        readTree.getItem(0).setState(true, false);

        rwTree = new Tree();
        rwTree.setStyleName("font-75em");
        rwTree.addItem(new TreeItem("Read/Write Allowed"));
        rwTree.getItem(0).setState(true, false);

        layout.setHTML(0, 0, "Permissions");
        layout.getCellFormatter().setStyleName(0, 0, "permissions_sub_header");

        layout.setWidget(1, 0, readTree);
        layout.setWidget(2, 0, rwTree);
    }

    public void setPermissionData(ArrayList<PermissionItem> data) {
        if (data == null)
            return;

        for (PermissionItem datum : data) {
            if (datum.isWrite())
                rwTree.getItem(0).addItem(new TreeItem(datum.getName()));
            else
                readTree.getItem(0).addItem(new TreeItem(datum.getName()));
        }

        rwTree.getItem(0).setState(true, false);
        readTree.getItem(0).setState(true, false);
    }

}
