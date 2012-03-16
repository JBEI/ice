package org.jbei.ice.client.news;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public interface INewsView {

    Widget asWidget();

    String getNewsTitle();

    String getNewsBody();

    Button getSubmitButton();

    void addNewsItem(String id, String date, String header, String body);

    void setAddNewsVisibility(boolean visible);

    void setAddNewsButtonVisibilty(boolean visible);
}
