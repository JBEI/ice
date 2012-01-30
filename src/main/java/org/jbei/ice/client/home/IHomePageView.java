package org.jbei.ice.client.home;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public interface IHomePageView {
    Widget asWidget();

    String getNewsTitle();

    String getNewsBody();

    Button getSubmitButton();

    void addNewsItem(String id, String date, String header, String body);

    void setAddNewsVisibility(boolean visible);

    //    ILogoutHandler getLogoutHandler();
}
