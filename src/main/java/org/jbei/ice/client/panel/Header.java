package org.jbei.ice.client.panel;

import java.util.Date;

import org.jbei.ice.client.ILogoutHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Composite implements ILogoutHandler {

    interface Resources extends ClientBundle {
        @Source("org/jbei/ice/client/resource/image/logo.gif")
        ImageResource logo();
    }

    private final Resources resources = GWT.create(Resources.class);
    private Anchor logout;

    public Header() {

        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");
        initWidget(table);

        table.setWidget(0, 0, getImageHeader());
        table.setWidget(0, 1, createLoggedInContents());
        table.getCellFormatter().setStyleName(0, 1, "header_info");
        table.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);

        table.setWidget(1, 0, getUnderLine());
        table.getFlexCellFormatter().setColSpan(1, 0, 2);
    }

    private Widget getImageHeader() {
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);

        Image img = new Image(resources.logo());
        table.setWidget(0, 0, img);
        return table;
    }

    // TODO : set styles
    private Widget createLoggedInContents() {
        HorizontalPanel panel = new HorizontalPanel();

        DateTimeFormat format = DateTimeFormat.getFormat("EEEE, MMMM d, yyyy");
        String dateFormat = format.format(new Date());
        String html = "As of <b>" + dateFormat + "</b><br />there are [1454] Entries available";
        HTML entriesAvailable = new HTML(html);
        entriesAvailable.setStyleName("header_info_left");
        panel.add(entriesAvailable);

        // welcome
        HorizontalPanel right = new HorizontalPanel();
        HTML welcometxt = new HTML("Welcome,");
        Hyperlink link = new Hyperlink("[Hector Plahar]", "page=profile;id=[email]");
        HTML pipe = new HTML("&nbsp;|&nbsp;");
        logout = new Anchor("Log Out");

        right.add(welcometxt);
        right.add(link);
        right.add(pipe);
        right.add(logout);
        right.setStyleName("header_info_right");

        // add right side to panel
        panel.add(right);

        return panel;
    }

    private Widget getUnderLine() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("blue_underline");
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        panel.setWidth("100%");
        return panel;
    }

    @Override
    public HasClickHandlers getClickHandler() {
        return this.logout;
    }
}
