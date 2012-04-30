package org.jbei.ice.client.home;

import java.util.ArrayList;

import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class ArchiveMenu extends Composite {

    private final FlexTable panel;
    private final ArchiveMenuPresenter presenter;

    public ArchiveMenu() {
        panel = new FlexTable();
        panel.setCellSpacing(12);
        panel.setStyleName("news_archive");
        panel.setWidth("350px");
        initWidget(panel);
        presenter = new ArchiveMenuPresenter();
    }

    public void setContents(ArrayList<NewsItem> contents) {
        int row = 0;
        for (NewsItem item : contents) {
            panel.setWidget(row, 0, createNewsWidget(item));
            row += 1;
        }
    }

    protected Widget createNewsWidget(final NewsItem item) {
        Label label = new Label(item.getHeader());
        label.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                presenter.setSelected(item);
            }
        });
        label.setStyleName("news_archive_header");
        HTMLPanel panel = new HTMLPanel(
                "<span id=\"news_title\"></span><span style=\"font-size: 0.75em; color: #333\">"
                        + DateUtilities.formatDate(item.getCreationDate()) + "</span>");
        panel.add(label, "news_title");
        return panel;
    }

    public SingleSelectionModel<NewsItem> getSelectionModel() {
        return presenter.getSelectionModel();
    }
}
