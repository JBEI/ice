package org.jbei.ice.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ILoginEventHandler extends EventHandler {

    void onLogin(LoginEvent event);
}
