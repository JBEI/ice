package org.jbei.ice.client.common.header;

import java.util.LinkedHashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderView extends Composite {

    interface Resources extends ClientBundle {

        static Resources INSTANCE = GWT.create(Resources.class);

        @Source("org/jbei/ice/client/resource/image/logo.gif")
        ImageResource logo();
    }

    private SearchCompositeBox searchInput;
    private Button searchBtn;
    private final AdvancedSearchWidget widgetAdvanced;
    private final HeaderPresenter presenter;
    private FlexTable loggedInContentsPanel;

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

        // search Option
        widgetAdvanced = new AdvancedSearchWidget();
        widgetAdvanced.setWidth("401px");
        widgetAdvanced.setHeight("150px");

        presenter = new HeaderPresenter(this);
    }

    public String getSelectedFilterValue() {
        return widgetAdvanced.getSelectedFilter();
    }

    public String[] getSelectedSearchType() {
        return widgetAdvanced.getSelectedEntrySearch();
    }

    // handler for clicking search
    public void addSearchClickHandler(ClickHandler handler) {
        searchBtn.addClickHandler(handler);
    }

    public void setSearchButtonEnable(boolean enable) {
        searchBtn.setEnabled(enable);
    }

    public void setEntryTypeChangeHandler(ChangeHandler handler) {
//        final ListBox entryTypeOptions = widgetAdvanced.getEntryTypeOptions();
//        entryTypeOptions.addChangeHandler(handler);
    }

    public void setAddFilterHandler(ClickHandler handler) {
        widgetAdvanced.getAddFilter().addClickHandler(handler);
    }

    public void setSearchOptions(LinkedHashMap<String, String> options) {
        widgetAdvanced.initializeWidget(options);
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

    protected boolean isUserLoggedIn() {
        return AppController.sessionId != null;
    }

    /**
     * @return top right hand corner widget. Empty when the user is not logged in
     *         TODO the logic pertaining to setting the names should be moved to a
     *         controller/presenter
     */
    private Widget createLoggedInContents() {
        loggedInContentsPanel = new FlexTable();
        loggedInContentsPanel.setCellPadding(0);
        loggedInContentsPanel.setCellSpacing(0);
        loggedInContentsPanel.setStyleName("float_right");
        loggedInContentsPanel.addStyleName("font-80em");
        loggedInContentsPanel.addStyleName("pad-right-10");

        if (AppController.accountInfo == null) {
            loggedInContentsPanel.add(new HTML(SafeHtmlUtils.EMPTY_SAFE_HTML));
            return loggedInContentsPanel;
        }

        final AccountInfo info = AppController.accountInfo;

        // user
        HTML label = new HTML(info.getEmail());
        label.setStyleName("pull_down_as_link");
        label.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                History.newItem(Page.PROFILE.getLink() + ";id=" + info.getId());
            }
        });
        loggedInContentsPanel.setWidget(0, 0, label);

        // pipe
        HTML pipe = new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;");
        pipe.addStyleName("color_eee");
        loggedInContentsPanel.setWidget(0, 1, pipe);

        // messages
        loggedInContentsPanel.setWidget(0, 2, new HTML("<span class=\"badge\">2</span>"));

        // pipe
        HTML pipe3 = new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;");
        pipe3.addStyleName("color_eee");
        loggedInContentsPanel.setWidget(0, 3, pipe3);

        // logout link
        loggedInContentsPanel.setWidget(0, 4, new Icon(FAIconType.SIGNOUT));
        loggedInContentsPanel.setWidget(0, 5, new HTML("&nbsp;"));
        Hyperlink logout = new Hyperlink("Log Out", Page.LOGOUT.getLink());
        loggedInContentsPanel.setWidget(0, 6, logout);

        return loggedInContentsPanel;
    }

    public void setNewMessages(int newMessageCount) {

    }

    public String getSearchInput() {
        return this.searchInput.getTextBox().getText();
    }

    public SearchCompositeBox getSearchComposite() {
        return this.searchInput;
    }

    public SearchFilterInfo getBlastInfo() {
        return presenter.getBlastInfo();
    }

    public void setFilterChangeHandler(ChangeHandler handler) {
        widgetAdvanced.setOptionChangeHandler(handler);
    }

    public void createPullDownHandler() {
        if (this.searchInput != null) {
            PopupHandler handler = new PopupHandler(widgetAdvanced, this.searchInput.getPullDownAreaElement(),
                                                    false);
            this.searchInput.setPullDownClickhandler(handler);
        }
    }

    public void setFilterOperands(FilterWidget currentSelected) {
        widgetAdvanced.setFilterOperands(currentSelected);
    }
}
