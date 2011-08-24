package org.jbei.ice.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ILogoutEventHandler extends EventHandler {
    void onLogout(LogoutEvent event);
}
