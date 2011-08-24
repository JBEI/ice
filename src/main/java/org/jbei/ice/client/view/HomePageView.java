package org.jbei.ice.client.view;

import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.panel.Footer;
import org.jbei.ice.client.panel.Header;
import org.jbei.ice.client.panel.HeaderMenu;
import org.jbei.ice.client.presenter.HomePagePresenter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends Composite implements HomePagePresenter.Display {

    private final FlexTable layout;
    private final Header header;

    public HomePageView() {
        layout = new FlexTable();
        initWidget(layout);
        layout.setWidth("100%");
        header = new Header();

        // add login to page
        layout.setWidget(0, 0, header);
        layout.setWidget(1, 0, new HeaderMenu());
        layout.setWidget(2, 0, getContents());
        layout.getCellFormatter().setHeight(2, 0, "100%");
        layout.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

        // footer
        layout.setWidget(3, 0, Footer.getInstance());
    }

    protected Widget getContents() {
        FlexTable table = new FlexTable();
        table.setHeight("100%");
        table.setHTML(0, 0, "&nbsp;test");
        return table;
    }

    public ILogoutHandler getLogoutHandler() {
        return header;
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
