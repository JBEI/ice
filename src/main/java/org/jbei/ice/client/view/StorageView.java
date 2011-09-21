package org.jbei.ice.client.view;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.Header;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.model.StorageTreeModel;
import org.jbei.ice.client.presenter.StoragePresenter;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class StorageView extends Composite implements StoragePresenter.Display {

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

        main.setWidget(0, 0, new Header());
        main.setWidget(1, 0, new HeaderMenu());
        main.setHTML(2, 0, "No contents");
        main.getFlexCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
        main.getCellFormatter().setHeight(2, 0, "100%");
        main.setWidget(3, 0, Footer.getInstance());
    }

    public Widget createContents() {

        tree = new CellTree(treeModel, null);
        tree.setAnimationEnabled(true);
        FlexTable table = new FlexTable();
        table.setWidget(0, 0, tree);
        return table;
    }

    @Override
    public void setTreeModel(StorageTreeModel treeModel) {

        this.treeModel = treeModel;
        main.setWidget(2, 0, createContents());
    }
}
