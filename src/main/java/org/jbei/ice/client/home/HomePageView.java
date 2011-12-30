package org.jbei.ice.client.home;

import gwtupload.client.BaseUploadStatus;
import gwtupload.client.Uploader;

import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.header.HeaderView;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends Composite implements IHomePageView {

    private HeaderView header;

    public HomePageView() {
        HeaderPanel layout = new HeaderPanel();
        layout.setWidth("100%");
        layout.setHeight("100%");
        initWidget(layout);

        layout.setHeaderWidget(getHeader());
        layout.setContentWidget(getContents());
        layout.setFooterWidget(getFooter());
    }

    private Widget getHeader() {
        header = new HeaderView();
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
        Uploader uploader = new Uploader();
        uploader.setStatusWidget(new BaseUploadStatus());
        table.setWidget(1, 0, uploader);
        return table;
    }

    @Override
    public ILogoutHandler getLogoutHandler() {
        return header;
    }

    public Button getQuickSearchButton() {
        return header.getQuickSearchButton();
    }

    public String getQuickSearchInput() {
        return header.getSearchInput();
    }

    private Widget getFooter() {
        return Footer.getInstance();
    }

    @Override
    public Widget asWidget() {
        return this;
    }
}
