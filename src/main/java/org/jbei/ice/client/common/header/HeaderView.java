package org.jbei.ice.client.common.header;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

/**
 * View widget for the page header. Shared across all instances of pages
 *
 * @author Hector Plahar
 */
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
        widgetAdvanced.setWidth("395px");
        widgetAdvanced.setHeight("150px");

        createHandlers();
    }

    public void resetSearchBox() {
        widgetAdvanced.reset();
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
    private Widget createLoggedInContents(final User info) {
        if (info == null) {
            loggedInContentsPanel.setHTML(0, 0, SafeHtmlUtils.EMPTY_SAFE_HTML);
            return loggedInContentsPanel;
        }

        // user
        String htmlStr = "<i style=\"color: #757575\" class=\"" + FAIconType.USER.getStyleName() + "\"></i> ";
        SafeHtml profileHTML = SafeHtmlUtils.fromSafeConstant(htmlStr + info.getEmail());
        Hyperlink profile = new Hyperlink(profileHTML, Page.PROFILE.getLink() + ";id=" + info.getId() + ";s=profile");
        loggedInContentsPanel.setWidget(0, 0, profile);

        // messages
        loggedInContentsPanel.setHTML(0, 1, "");

        // pipe
        loggedInContentsPanel.setHTML(0, 2, "<span style=\"color: #969696\">&nbsp;&nbsp;|&nbsp;&nbsp;</span>");

        // logout link
        String logoutHtmlStr = "<i style=\"color: #757575\" class=\"" + FAIconType.SIGN_OUT.getStyleName();
        SafeHtml html = SafeHtmlUtils.fromSafeConstant(logoutHtmlStr + "\"></i> Log Out");
        Hyperlink logout = new Hyperlink(html, Page.LOGOUT.getLink());
        loggedInContentsPanel.setWidget(0, 5, logout);
        loggedInContentsPanel.setHTML(0, 6, "<span style=\"color: #969696\">&nbsp;&nbsp;|&nbsp;&nbsp;</span>");
        String helpStr = "<i style=\"color: #757575\" class=\"" + FAIconType.BOOK.getStyleName() + "\"></i> Help";
        SafeHtml helpHtml = SafeHtmlUtils.fromSafeConstant(helpStr);
        Anchor anchor = new Anchor(helpHtml, "https://public-registry.jbei.org/static/help.htm");
        loggedInContentsPanel.setWidget(0, 7, anchor);

        return loggedInContentsPanel;
    }

    public void setNewMessages(int newMessageCount) {
        if (newMessageCount <= 0) {
            loggedInContentsPanel.setHTML(0, 1, "");
            return;
        }

        final HTML emailBadge = new HTML("&nbsp;&nbsp;<span style=\"color: #969696\">|</span>&nbsp;&nbsp;"
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
            final PopupHandler handler = new PopupHandler(widgetAdvanced, this.searchInput.getPullDownAreaElement(),
                                                          false, searchInput);
            handler.addAutoHidePartner(searchInput.getTextBoxElement());
            handler.setCloseHandler(searchInput.getCloseHandler());
            this.searchInput.setPullDownClickHandler(handler);

            this.searchInput.setFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    if (!widgetAdvanced.isDefaultState()) {
                        handler.showRelativeTo(searchInput);
                    }
                }
            });
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

    public void setHeaderData(User account) {
        createLoggedInContents(account);
        setNewMessages(account.getNewMessageCount());
    }

    public void setQueryDelegate(ServiceDelegate<SearchQuery> queryDelegate) {
        queryServiceDelegate = queryDelegate;
    }
}
