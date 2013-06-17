package org.jbei.ice.client.common.header;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.search.SearchQuery;

public class HeaderView extends Composite {

    private SearchCompositeBox searchInput;
    private Button searchBtn;
    private final AdvancedSearchWidget widgetAdvanced;
    private final FlexTable loggedInContentsPanel;
    private static final HeaderView INSTANCE = new HeaderView();
    private final HeaderMenu headerMenu;
    private ServiceDelegate<SearchQuery> queryServiceDelegate;

    public static HeaderView getInstance() {
        return INSTANCE;
    }

    private HeaderView() {
        Widget searchPanel = createSearchPanel();

        FlexTable table = new FlexTable();
        table.setStyleName("margin-bottom-20");
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");
        initWidget(table);

        loggedInContentsPanel = new FlexTable();
        loggedInContentsPanel.setCellPadding(0);
        loggedInContentsPanel.setCellSpacing(0);
        loggedInContentsPanel.setStyleName("float_right");
        loggedInContentsPanel.addStyleName("font-80em");
        loggedInContentsPanel.addStyleName("pad-right-10");

        headerMenu = new HeaderMenu();
        table.setHTML(0, 0, "<img src=\"static/images/logo.png\" height=\"80px\"/>");
        table.setWidget(0, 1, headerMenu);
        table.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_BOTTOM);

        String html = "<span id=\"logged_in_info\"></span><br><span id=\"search_panel\"></span>";
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(searchPanel, "search_panel");
        panel.add(loggedInContentsPanel, "logged_in_info");
        table.setWidget(0, 2, panel);

        // search Option
        widgetAdvanced = new AdvancedSearchWidget(searchInput);
        widgetAdvanced.setWidth("394px");
        widgetAdvanced.setHeight("150px");

        createHandlers();
    }

    public void resetSearchBox() {
        widgetAdvanced.reset();
    }

    public void setSearchBox(String box) {
        searchInput.setSearch(box);
    }

    protected Widget createSearchPanel() {
        FlexTable layout = new FlexTable();
        layout.setCellPadding(4);
        layout.setCellSpacing(1);

        if (!userIsLoggedIn()) {
            return layout;
        }

        searchInput = new SearchCompositeBox();
        searchInput.addTextBoxKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() != KeyCodes.KEY_ENTER)
                    return;
                searchBtn.click();
            }
        });

        layout.setWidget(0, 0, searchInput);
        layout.getFlexCellFormatter().setRowSpan(0, 0, 2);

        searchBtn = new Button("Search");
        searchBtn.setStyleName("quick_search_btn");
        layout.setWidget(0, 1, searchBtn);
        layout.getFlexCellFormatter().setRowSpan(0, 1, 2);
        layout.setStyleName("float_right");
        return layout;
    }

    protected boolean userIsLoggedIn() {
        return ClientController.sessionId != null;
    }

    /**
     * @return top right hand corner widget. Empty when the user is not logged in
     *         controller/presenter
     */
    private Widget createLoggedInContents(final AccountInfo info) {
        if (info == null) {
            loggedInContentsPanel.setHTML(0, 0, SafeHtmlUtils.EMPTY_SAFE_HTML);
            return loggedInContentsPanel;
        }

        // user
        loggedInContentsPanel.setHTML(0, 0, "<a href=\"#" + Page.PROFILE.getLink() + ";id=" + info.getId()
                + ";s=profile\">" + info.getEmail() + "</a>");

        // messages
        loggedInContentsPanel.setHTML(0, 1, "");

        // pipe
        HTML pipe3 = new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;");
        pipe3.addStyleName("color_eee");
        loggedInContentsPanel.setWidget(0, 2, pipe3);

        // logout link
        loggedInContentsPanel.setWidget(0, 3, new Icon(FAIconType.SIGNOUT));
        loggedInContentsPanel.setWidget(0, 4, new HTML("&nbsp;"));
        Hyperlink logout = new Hyperlink("Log Out", Page.LOGOUT.getLink());
        loggedInContentsPanel.setWidget(0, 5, logout);
        return loggedInContentsPanel;
    }

    public void setNewMessages(int newMessageCount) {
        if (newMessageCount <= 0) {
            loggedInContentsPanel.setHTML(0, 1, "");
            return;
        }

        final HTML emailBadge = new HTML("&nbsp;&nbsp;<span style=\"color: #EEE\">|</span>&nbsp;&nbsp;"
                + "<span class=\"badge\">" + newMessageCount + "</span>");
        String title = "You have " + newMessageCount + " new message";
        title += newMessageCount != 1 ? "s" : "";
        emailBadge.setTitle(title);
        emailBadge.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                emailBadge.setVisible(false);
                emailBadge.setHTML("");
                loggedInContentsPanel.setHTML(0, 1, "");
                History.newItem(Page.PROFILE.getLink() + ";id=" + ClientController.account.getId() + ";s=messages");
            }
        });

        loggedInContentsPanel.setWidget(0, 1, emailBadge);
    }

    public void createHandlers() {
        if (this.searchInput != null) {
            PopupHandler handler = new PopupHandler(widgetAdvanced, this.searchInput.getPullDownAreaElement(), false);
            handler.addAutoHidePartner(searchInput.getTextBoxElement());
            handler.setCloseHandler(searchInput.getCloseHandler());
            this.searchInput.setPullDownClickHandler(handler);
        }

        ClickHandler searchHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                searchInput.advancedWidgetClosed();
                widgetAdvanced.parseSearchOptions(queryServiceDelegate);
            }
        };

        searchBtn.addClickHandler(searchHandler);
        widgetAdvanced.addSearchHandler(searchHandler);
    }

    public void setHeader(Page page) {
        headerMenu.setSelected(page);
    }

    public void setHeaderData(AccountInfo account) {
        createLoggedInContents(account);
        setNewMessages(account.getNewMessageCount());
    }

    public void setQueryDelegate(ServiceDelegate<SearchQuery> queryDelegate) {
        queryServiceDelegate = queryDelegate;
    }
}
