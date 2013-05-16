package org.jbei.ice.client.event;

import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that is dispatched when a user successfully logs in
 * The session id associated with this is valid (for a period).
 *
 * @author Hector Plahar
 */

public class LoginEvent extends GwtEvent<ILoginEventHandler> {

    public static Type<ILoginEventHandler> TYPE = new Type<ILoginEventHandler>();

    private final boolean rememberUser;
    private AccountInfo info;

    public LoginEvent(AccountInfo info, boolean rememberUser) {
        this.info = info;
        this.rememberUser = rememberUser;
    }

    public String getSessionId() {
        return this.info.getSessionId();
    }

    public boolean isRememberUser() {
        return this.rememberUser;
    }

    public AccountInfo getAccountInfo() {
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
