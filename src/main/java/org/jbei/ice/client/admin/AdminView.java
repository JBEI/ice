package org.jbei.ice.client.admin;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class AdminView extends AbstractLayout {

    private FlexTable contentTable;

    @Override
    protected void initComponents() {
        super.initComponents();

        contentTable = new FlexTable();
    }

    @Override
    protected Widget createContents() {

        contentTable.setWidth("100%");
        contentTable.setHTML(0, 0, "menu");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);

        // TODO : middle sliver goes here
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        return contentTable;
    }

    protected Widget createMainContent() {
        return new HTML("Content");
    }
}
