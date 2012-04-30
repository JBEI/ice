package org.jbei.ice.client.news;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.NewsItem;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public interface INewsView {

    Widget asWidget();

    String getNewsTitle();

    String getNewsBody();

    Button getSubmitButton();

    void addNewsItem(String id, String date, String header, String body);

    void setAddNewsVisibility(boolean visible);

    void setAddNewsButtonVisibilty(boolean visible);

    void setArchiveContents(ArrayList<NewsItem> contents);

    SingleSelectionModel<NewsItem> getArchiveSelectionModel();
}
