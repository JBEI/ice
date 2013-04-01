package org.jbei.ice.client.common;

import org.jbei.ice.client.common.footer.Footer;
import org.jbei.ice.client.common.header.HeaderView;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractLayout extends Composite {

    public AbstractLayout() {
        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);

        initComponents();

        layout.setWidget(0, 0, HeaderView.getInstance());
        layout.setWidget(1, 0, createContents());
        layout.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        layout.getCellFormatter().setHeight(1, 0, "100%");
        layout.setWidget(2, 0, Footer.getInstance());
    }

    protected abstract Widget createContents();

    /**
     * Initialization of components used in this view.
     * sub-classes that override should make sure to call
     * super.initComponents();
     */
    protected void initComponents() {}

    public void reset() {}
}
