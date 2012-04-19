package org.jbei.ice.client.storage;

import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.common.Footer;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class StorageView extends AbstractLayout implements IStorageView {

    private CellTree tree;
    private StorageTreeModel treeModel;
    private FlexTable main;

    public StorageView() {

        main = new FlexTable();
        main.setCellPadding(0);
        main.setCellSpacing(0);
        main.setWidth("100%");
        main.setHeight("98%");
        initWidget(main);

        main.setHTML(2, 0, "No contents");
        main.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        main.getCellFormatter().setHeight(2, 0, "100%");
        main.setWidget(3, 0, Footer.getInstance());
    }

    @Override
    public void setContent(Widget widget) {
        main.setWidget(2, 0, widget);
    }

    @Override
    protected Widget createContents() {
        // TODO Auto-generated method stub
        return null;
    }
}
