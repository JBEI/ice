package org.jbei.ice.client.news;

import java.util.ArrayList;

import org.jbei.ice.client.common.AbstractLayout;
import org.jbei.ice.client.home.ArchiveMenu;
import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class NewsView extends AbstractLayout implements INewsView {

    private ArchiveMenu menu;
    private FlexTable contentTable;
    private FlexTable newsAdd;
    private TextBox title;
    private TextArea area;
    private Button submitNews;
    private Button cancelNews;
    private Button addNewButton;
    private FlexTable newsContent;

    @Override
    protected void initComponents() {
        super.initComponents();

        menu = new ArchiveMenu();
        contentTable = new FlexTable();

        submitNews = new Button("Submit");
        cancelNews = new Button("Cancel");

        newsAdd = new FlexTable();
        newsAdd.setHTML(0, 0, "<span class=\"font-85em font-bold\">Title</span>");
        title = new TextBox();
        title.setStyleName("input_box");
        title.setWidth("350px");
        newsAdd.setWidget(0, 1, title);

        newsAdd.setHTML(1, 0, "<span class=\"font-85em font-bold\">Contents</span>");
        newsAdd.getFlexCellFormatter().setVerticalAlignment(1, 0, HasAlignment.ALIGN_TOP);
        area = new TextArea();
        area.setStyleName("input_box");
        area.setSize("350px", "120px");
        newsAdd.setWidget(1, 1, area);

        newsAdd.setWidget(2, 0, submitNews);
        newsAdd.setWidget(2, 1, cancelNews);
        newsAdd.setVisible(false);
    }

    @Override
    protected Widget createContents() {

        contentTable.setWidth("100%");
        contentTable.setWidget(0, 0, menu);
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        contentTable.getFlexCellFormatter().setWidth(0, 0, "380px");

        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        return contentTable;
    }

    private Button createAddButton() {
        addNewButton = new Button("Add");
        addNewButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                newsAdd.setVisible(!newsAdd.isVisible());
            }
        });
        return addNewButton;
    }

    private Widget createMainContent() {
        newsContent = new FlexTable();
        newsContent.setWidth("100%");
        newsContent.setWidget(0, 0, createAddButton());
        newsContent.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);

        newsContent.setWidget(1, 0, newsAdd);
        newsContent.setHTML(2, 0, "&nbsp;");

        return newsContent;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void addNewsItem(String id, String date, String header, String body) {
        // TODO move styles to css
        String html = "<div><span style=\"color: #211f19; font-size: 1.40em\">"
                + header
                + "</span><br><span style=\"font-size: 0.70em; color: #919191; text-transform: uppercase\">"
                + date
                + "</span><br><div class=\"font-80em\" style=\"line-height: 1.4; margin: .75em 0 0;\">"
                + body + "</div></div>";
        HTMLPanel panel = new HTMLPanel(html);
        newsContent.setWidget(2, 0, panel);
    }

    @Override
    public void setArchiveContents(ArrayList<NewsItem> contents) {
        menu.setContents(contents);
    }

    @Override
    public String getNewsTitle() {
        return this.title.getText();
    }

    @Override
    public String getNewsBody() {
        return this.area.getText();
    }

    @Override
    public Button getSubmitButton() {
        return this.submitNews;
    }

    @Override
    public void setAddNewsVisibility(boolean visible) {
        this.newsAdd.setVisible(visible);
    }

    @Override
    public void setAddNewsButtonVisibilty(boolean visible) {
        this.addNewButton.setVisible(visible);
    }

    @Override
    public SingleSelectionModel<NewsItem> getArchiveSelectionModel() {
        return menu.getSelectionModel();
    }
}
