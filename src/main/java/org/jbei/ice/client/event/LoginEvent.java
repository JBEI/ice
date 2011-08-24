package org.jbei.ice.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that is dispatched when a user successfully logs in
 * The session id associated with this is valid (for a period).
 * 
 * @author Hector Plahar
 */

public class LoginEvent extends GwtEvent<ILoginEventHandler> {

    public static Type<ILoginEventHandler> TYPE = new Type<ILoginEventHandler>();

    private final String sessionId;
    private final boolean rememberUser;

    public LoginEvent(String sid, boolean rememberUser) {
        this.sessionId = sid;
        this.rememberUser = rememberUser;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public boolean isRememberUser() {
        return this.rememberUser;
    }

    @Override
    public Type<ILoginEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ILoginEventHandler handler) {
        handler.onLogin(this);
    }
}
