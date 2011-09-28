package org.jbei.ice.client.view;

import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.presenter.HomePagePresenter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends Composite implements HomePagePresenter.Display {

    private final HeaderView header;

    public HomePageView() {
        HeaderPanel layout = new HeaderPanel();
        layout.setWidth("100%");
        initWidget(layout);

        layout.setHeaderWidget(getHeader());
        layout.setContentWidget(getContents());
        layout.setFooterWidget(getFooter());

        header = new HeaderView();
    }

    private Widget getHeader() {

        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("100%");
        panel.add(header);
        panel.add(new HeaderMenu());
        return panel;
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

    private Widget getFooter() {
        return Footer.getInstance();
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
