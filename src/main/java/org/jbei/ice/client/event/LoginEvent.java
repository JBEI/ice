package org.jbei.ice.client.event;

import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that is dispatched when a user successfully logs in
 * The session id associated with this is valid (for a period).
 *
 * @author Hector Plahar
 */

public class LoginEvent extends GwtEvent<ILoginEventHandler> {

    public static final Type<ILoginEventHandler> TYPE = new Type<ILoginEventHandler>();

    private final boolean rememberUser;
    private User info;

    public LoginEvent(User info, boolean rememberUser) {
        this.info = info;
        this.rememberUser = rememberUser;
    }

    public String getSessionId() {
        return this.info.getSessionId();
    }

    public boolean isRememberUser() {
        return this.rememberUser;
    }

    public User getAccountInfo() {
        return this.info;
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
