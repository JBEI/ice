package org.jbei.ice.client.common.header;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.ILogoutHandler;
import org.jbei.ice.client.Page;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderView extends Composite implements ILogoutHandler { // TODO: should implement IHasLogOut instead of handler

    interface Resources extends ClientBundle {

        static Resources INSTANCE = GWT.create(Resources.class);

        @Source("org/jbei/ice/client/resource/image/logo.gif")
        ImageResource logo();

        @Source("org/jbei/ice/client/resource/image/arrow_down.png")
        ImageResource arrowDown();
    }

    private Hyperlink logout;
    private CompositeText searchInput;
    private Button searchBtn;

    public HeaderView() {
        Widget searchPanel = createSearchPanel();
        FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("pad-right-10");
        table.setWidth("100%");
        initWidget(table);

        VerticalPanel vertical = new VerticalPanel();
        vertical.add(createLoggedInContents());
        vertical.add(searchPanel);
        vertical.addStyleName("float_right");
        vertical.setSpacing(4);

        HorizontalPanel horizontal = new HorizontalPanel();
        horizontal.setWidth("100%");
        horizontal.add(getImageHeader());
        horizontal.add(vertical);

        table.setWidget(0, 0, horizontal);
        new HeaderPresenter(this);
    }

    public Image getSearchArrow() { // TODO : more descriptive name
        return searchInput.getImage();
    }

    public Button getSearchButton() {
        return this.searchBtn;
    }

    private Widget getImageHeader() {
        Image img = new Image(Resources.INSTANCE.logo());
        return img;
    }

    protected Widget createSearchPanel() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(4);
        layout.setCellSpacing(1);
        if (!isUserLoggedIn()) {
            return layout;
        }

        searchInput = new CompositeText();
        layout.setWidget(0, 0, searchInput);
        layout.getFlexCellFormatter().setRowSpan(0, 0, 2);

        searchBtn = new Button("Search");
        searchBtn.setStyleName("quick_search_btn");
        layout.setWidget(0, 1, searchBtn);
        layout.getFlexCellFormatter().setRowSpan(0, 1, 2);
        layout.setStyleName("float_right");

        return layout;
    }

    public class CompositeText extends Composite {
        private final TextBox box;
        private final Image image;

        public CompositeText() {
            AbsolutePanel panel = new AbsolutePanel();
            box = new TextBox();
            box.setStyleName("quick_search_input");
            panel.add(box);
            image = new Image(Resources.INSTANCE.arrowDown());
            image.setStyleName("search_arrow");
            panel.add(image);
            initWidget(panel);
        }

        public TextBox getTextBox() {
            return this.box;
        }

        public Image getImage() {
            return this.image;
        }
    }

    // TODO : move to controller
    protected boolean isUserLoggedIn() {
        return AppController.sessionId != null;
    }

    /**
     * @return top right hand corner widget. Empty when the user is not logged in
     *         TODO the logic pertaining to setting the names should be moved to a
     *         controller/presenter
     */
    private Widget createLoggedInContents() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setStyleName("float_right");
        panel.addStyleName("font-95em");

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
        String formattedEntries = NumberFormat.getDecimalFormat().format(
            info.getVisibleEntryCount());
        HTML entriesAvailable = new HTML(formattedEntries + " entries available");
        panel.add(entriesAvailable);

        // pipe
        pipe = new HTML("&nbsp;|&nbsp;");
        pipe.addStyleName("color_eee");
        panel.add(pipe);

        // logout link
        logout = new Hyperlink("Log Out", Page.LOGOUT.getLink());
        panel.add(logout);

        return panel;
    }

    public String getSearchInput() {
        return this.searchInput.getTextBox().getText();
    }

    public CompositeText getSearchComposite() {
        return this.searchInput;
    }

    @Override
    public HasClickHandlers getClickHandler() {
        return this.logout;
    }
}
