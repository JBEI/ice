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

    private final String sessionId;
    private final boolean rememberUser;
    private final String userName;
    private final String userId;
    private AccountInfo info;

    public LoginEvent(AccountInfo info, boolean rememberUser) {
        this(info.getSessionId(), (info.getFirstName() + " " + info.getLastName()),
                info.getEmail(), rememberUser);
        this.info = info;
    }

    /**
     * @deprecated
     * @param sid
     * @param userName
     * @param userId
     * @param rememberUser
     */
    public LoginEvent(String sid, String userName, String userId, boolean rememberUser) {
        this.sessionId = sid;
        this.userName = userName;
        this.userId = userId;
        this.rememberUser = rememberUser;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public boolean isRememberUser() {
        return this.rememberUser;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUserId() {
        return this.userId;
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
