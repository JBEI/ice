package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
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
    private final Label editLabel;

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

        editLabel = new Label("Edit");
        editLabel.setStyleName("edit_permissions_label");
        HTMLPanel panel = new HTMLPanel(
                "Permissions<span style=\"float:right; display: inline\" id=\"permissions_edit_link\"></span>");
        panel.add(editLabel, "permissions_edit_link");
        layout.setWidget(0, 0, panel);
        layout.getCellFormatter().setStyleName(0, 0, "permissions_sub_header");

        layout.setWidget(1, 0, readTree);
        layout.setWidget(2, 0, rwTree);
    }

    public void addPermissionEditClickHandler(ClickHandler handler) {
        editLabel.addClickHandler(handler);
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
