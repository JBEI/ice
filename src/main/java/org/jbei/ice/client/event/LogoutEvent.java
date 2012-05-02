package org.jbei.ice.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Dispatched when user logs out
 * 
 * @author hector
 * 
 */
public class LogoutEvent extends GwtEvent<ILogoutEventHandler> {

    public static Type<ILogoutEventHandler> TYPE = new Type<ILogoutEventHandler>();

    @Override
    public Type<ILogoutEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ILogoutEventHandler handler) {
        handler.onLogout(this);
    }

}
