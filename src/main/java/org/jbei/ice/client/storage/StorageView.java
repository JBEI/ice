package org.jbei.ice.client.storage;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

public class StorageView extends AbstractLayout implements IStorageView {

    private CellTree tree;
    private StorageTreeModel treeModel;
    private FlexTable main;
    private FlexTable contentTable;

    @Override
    protected void initComponents() {
        contentTable = new FlexTable();
    }

    @Override
    public void setContent(Widget widget) {
        main.setWidget(2, 0, widget);
    }

    @Override
    protected Widget createContents() {
        contentTable.setWidth("100%");
        return contentTable;
    }
}
