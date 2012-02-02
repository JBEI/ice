package org.jbei.ice.client.home;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends AbstractLayout implements IHomePageView {

    private ArchiveMenu menu;
    private FlexTable contentTable;
    private VerticalPanel news;
    private FlexTable newsAdd;
    private TextBox title;
    private TextArea area;
    private Button submitNews;
    private Button cancelNews;

    @Override
    protected void initComponents() {
        super.initComponents();

        news = new VerticalPanel();
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

        // TODO : middle sliver goes here
        contentTable.setWidget(0, 1, createMainContent());
        contentTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        return contentTable;
    }

    private Button createAddButton() {
        Button button = new Button("Add");
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                newsAdd.setVisible(!newsAdd.isVisible());
            }
        });
        return button;
    }

    private Widget createMainContent() {
        FlexTable table = new FlexTable();
        table.setWidth("100%");
        table.setWidget(0, 0, createAddButton());
        table.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_RIGHT);

        table.setWidget(1, 0, newsAdd);
        table.setWidget(2, 0, news);

        return table;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void addNewsItem(String id, String date, String header, String body) {
        // TODO move styles to css
        String html = "<div style=\"border-bottom: 1px solid #f1f1f1; margin-bottom: 10px; padding: 6px;\"><b style=\"color: #211f19; font-size: 1.20em\">"
                + header
                + "</b><br><span style=\"font-size: 0.75em; color: #a1a1a1\">"
                + date
                + "</span><br><br><div class=\"font-80em\">" + body + "</div></div>";
        HTMLPanel panel = new HTMLPanel(html);
        news.insert(panel, 0);
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
}
