package org.jbei.ice.client.home;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends AbstractLayout implements IHomePageView {

    private FlexTable contentTable;

    @Override
    protected void initComponents() {
        super.initComponents();

        contentTable = new FlexTable();
    }

    @Override
    protected Widget createContents() {

        contentTable.setWidth("100%");
        contentTable.setHTML(0, 0, "&nbsp;");
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        return contentTable;
    }
}
