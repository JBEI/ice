package org.jbei.ice.client.home;

import org.jbei.ice.client.ILogoutHandler;

import com.google.gwt.user.client.ui.Widget;

public interface IHomePageView {
    Widget asWidget();

    ILogoutHandler getLogoutHandler();
}
