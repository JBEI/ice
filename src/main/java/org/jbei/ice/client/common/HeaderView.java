package org.jbei.ice.client.common;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.Page;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderView extends Composite implements ILogoutHandler {

    interface Resources extends ClientBundle {
        @Source("org/jbei/ice/client/resource/image/logo.gif")
        ImageResource logo();
    }

    private final Resources resources = GWT.create(Resources.class);
    private Anchor logout;

    public HeaderView() {

        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");
        initWidget(table);

        VerticalPanel vertical = new VerticalPanel();
        vertical.add(createLoggedInContents());
        vertical.add(createSearchPanel());
        vertical.addStyleName("float_right");

        HorizontalPanel horizontal = new HorizontalPanel();
        horizontal.setWidth("100%");
        horizontal.add(getImageHeader());
        horizontal.add(vertical);

        table.setWidget(0, 0, horizontal);
        table.setWidget(1, 0, getUnderLine());
    }

    private Widget getImageHeader() {
        Image img = new Image(resources.logo());
        return img;
    }

    protected Widget createSearchPanel() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(0);

        TextBox input = new TextBox();
        input.setWidth("250px");
        input.setStyleName("quick_search_input");
        layout.setWidget(0, 0, input);
        layout.getFlexCellFormatter().setRowSpan(0, 0, 2);

        Button searchBtn = new Button("Search");
        searchBtn.setStyleName("quick_search_btn");
        layout.setWidget(0, 1, searchBtn);
        layout.getFlexCellFormatter().setRowSpan(0, 1, 2);
        layout.setStyleName("float_right");

        Hyperlink searchLink = new Hyperlink("Advanced Search", Page.QUERY.getLink());
        searchLink.addStyleName("small_text");
        layout.setWidget(0, 2, searchLink);

        Hyperlink blastLink = new Hyperlink("Blast Search", Page.BLAST.getLink());
        blastLink.addStyleName("small_text");
        layout.setWidget(1, 0, blastLink);
        return layout;
    }

    /**
     * @return top right hand corner widget. Empty when the user is not logged in
     *         TODO the logic pertaining to setting the names should be moved to a
     *         controller/presenter
     */
    private Widget createLoggedInContents() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("float_right");

        if (AppController.accountInfo == null) {
            panel.add(new HTML(SafeHtmlUtils.EMPTY_SAFE_HTML));
            return panel;
        }

        AccountInfo info = AppController.accountInfo;

        // Welcome text
        HTML welcometxt = new HTML("Welcome,&nbsp;");
        Hyperlink link = new Hyperlink(info.getFirstName() + " " + info.getLastName(),
                "page=profile;id=" + info.getEmail());
        panel.add(welcometxt);
        panel.add(link);

        // pipe
        HTML pipe = new HTML("&nbsp;|&nbsp;");
        pipe.addStyleName("color_eee");
        panel.add(pipe);

        // Entries Available
        HTML entriesAvailable = new HTML("[xxx] Entries Available");
        panel.add(entriesAvailable);

        // pipe
        pipe = new HTML("&nbsp;|&nbsp;");
        pipe.addStyleName("color_eee");
        panel.add(pipe);

        // logout link
        logout = new Anchor("Log Out");
        panel.add(logout);

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
