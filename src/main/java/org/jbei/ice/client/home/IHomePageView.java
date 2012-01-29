package org.jbei.ice.client.home;

import com.google.gwt.user.client.ui.Widget;

public interface IHomePageView {
    Widget asWidget();

    void addNewsItem(String id, String string, String header, String body);

    //    ILogoutHandler getLogoutHandler();
}
