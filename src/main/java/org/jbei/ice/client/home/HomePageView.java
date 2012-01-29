package org.jbei.ice.client.home;

import org.jbei.ice.client.common.AbstractLayout;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class HomePageView extends AbstractLayout implements IHomePageView {

    private ArchiveMenu menu;
    private FlexTable contentTable;
    //    private final VerticalPanel news;
    private FlexTable newsAdd;
    private TextBox title;
    private TextArea area;
    private Button submitNews;
    private Button cancelNews;

    public HomePageView() {
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        menu = new ArchiveMenu();
        contentTable = new FlexTable();
        submitNews = new Button("Submit");
        cancelNews = new Button("Cancel");

        newsAdd = new FlexTable();
        newsAdd.setHTML(0, 0, "Title");
        TextBox title = new TextBox();
        newsAdd.setWidget(0, 1, title);
        newsAdd.setHTML(1, 0, "Body");
        TextArea area = new TextArea();
        newsAdd.setWidget(1, 1, area);
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

        return table;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void addNewsItem(String id, String string, String header, String body) {

    }
}
