package org.jbei.ice.web;

import java.util.Calendar;

import javax.servlet.http.Cookie;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;

/**
 * Custom wicket {@link WebSession} for gd-ice.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class IceSession extends WebSession {
    private static final long serialVersionUID = 1L;

    private final String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");

    /**
     * Constructor.
     * 
     * @param request
     */
    public IceSession(Request request) {
        super(request);
    }

    /**
     * Save account id into SessionData, and save into db for persistent
     * token based authentication.
     * 
     * @throws ManagerException
     */
    public void makeSessionPersistent(WebResponse response, SessionData sessionData)
            throws ManagerException {

        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireDate = currentTime + 7776000000L; //90 days

        Cookie cookie = new Cookie(COOKIE_NAME, sessionData.getSessionKey());
        cookie.setPath("/");
        cookie.setMaxAge(7776000);
        response.addCookie(cookie);

        sessionData.setExpireDate(expireDate);
        try {
            PersistentSessionDataWrapper.getInstance().persist(sessionData);
        } catch (ManagerException e) {
            throw e;
        }

    }

    /**
     * Authenticate the user using authentication method specified in the settings file.
     * 
     * @param login
     *            Login name.
     * @param password
     *            Password
     * @return - {@link SessionData} object.
     * @throws IceSessionException
     * @throws InvalidCredentialsException
     */
    public SessionData authenticateUser(String login, String password) throws IceSessionException,
            InvalidCredentialsException {

        SessionData sessionData = null;

        WebClientInfo webClientInfo = (WebClientInfo) getClientInfo();
        String ip = webClientInfo.getProperties().getRemoteAddress();

        try {
            sessionData = AccountController.authenticate(login, password, ip);
        } catch (ControllerException e) {
            Logger.error("Authentication failed!", e);

            return null;
        }

        if (sessionData == null) {
            // User authenticates but this session is not associated.
            String msg = "User is authenticated but this session is not associated";
            Logger.error(msg, new Exception("Error"));
            throw new RuntimeException(msg);
        }

        setSessionKeyCookie(sessionData.getSessionKey());

        return sessionData;
    }

    /**
     * Log out this session.
     */
    public void deAuthenticateUser() {
        clearSavedSession();
    }

    /**
     * Check if this session is authenticated.
     * 
     * @return True if an {@link Account} is associated with this session.
     */
    public boolean isAuthenticated() {
        return (getAccount() == null) ? false : true;
    }

    //getters and setters
    /**
     * Retrieve the session instance.
     * 
     * @return IceSession instance.
     */
    public static IceSession get() {
        return (IceSession) Session.get();
    }

    /**
     * Retrieve the session key.
     * 
     * @return Session key string.
     */
    public String getSessionKey() {
        String result = null;
        result = (getSessionData() != null) ? getSessionData().getSessionKey() : "";

        return result;
    }

    /**
     * Retrieve the {@link SessionData} for this session.
     * 
     * @return SessionData object.
     */
    public SessionData getSessionData() {
        SessionData sessionData = null;
        Cookie userCookie = getRequest().getCookie(COOKIE_NAME);
        if (userCookie != null) {
            String sessionKey = userCookie.getValue();
            try {
                sessionData = PersistentSessionDataWrapper.getInstance().getSessionData(sessionKey);
            } catch (ManagerException e) {
                throw new RuntimeException(e);
            }
        }

        return sessionData;
    }

    /**
     * Set a cookie in the user's browser.
     * 
     * @param sessionKey
     *            session key to set into the cookie.
     */
    private void setSessionKeyCookie(String sessionKey) {
        Cookie cookie = new Cookie(COOKIE_NAME, sessionKey);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        try {
            WebResponse response = (WebResponse) RequestCycle.get().getResponse();
            (response).addCookie(cookie);
        } catch (ClassCastException e) {
            /* This is because of a seeming bug in Wicket.  getResponse() sometimes
                returns a StringResponse instead of WebResponse. Nothing has to be done
                here anyway.
             */
            Logger.debug("Could not cast StringResponse to WebResponse");
        }
    }

    /**
     * Retrieve the {@link Account} for this session.
     * 
     * @return Account object.
     */
    public Account getAccount() {
        Account account = null;
        SessionData sessionData = getSessionData();
        if (sessionData != null) {
            account = sessionData.getAccount();
        }

        return account;
    }

    /**
     * Save the {@link AccountPreferences} into the database.
     * 
     * @param accountPreferences
     *            AccountPreferences.
     */
    public void setAccountPreferences(AccountPreferences accountPreferences) {
        try {
            AccountController.saveAccountPreferences(accountPreferences);
        } catch (ControllerException e) {
            String msg = "Could not setAccountPreferences in IceSession: " + e.toString();
            Logger.error(msg, e);
        }
    }

    /**
     * Retrieve the {@link AccountPreferences} from the database.
     * 
     * @return AccountPreferences.
     */
    public AccountPreferences getAccountPreferences() {
        AccountPreferences result = null;
        try {
            result = AccountController.getAccountPreferences(getAccount());
        } catch (ControllerException e) {
            String msg = "Could not getAccountPreferences in IceSession: " + e.toString();
            Logger.error(msg, e);
        }
        return result;
    }

    //private methods
    /**
     * Clear the current sessionData from the database.
     */
    private void clearSavedSession() {
        try {
            SessionData sessionData = getSessionData();

            if (sessionData != null) {
                PersistentSessionDataWrapper.getInstance().delete(sessionData.getSessionKey());
            }
        } catch (ManagerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the {@link WebRequest} object for this session's {@link WebRequestCycle}.
     * 
     * @return WebRequest.
     */
    private WebRequest getRequest() {

        WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle.get();
        return webRequestCycle.getWebRequest();
    }

    /**
     * Exception class for IceSession.
     * 
     * @author Timothy Ham
     * 
     */
    public class IceSessionException extends Exception {
        private static final long serialVersionUID = 1L;

        public IceSessionException() {
            super();
        }

        public IceSessionException(String message, Throwable cause) {
            super(message, cause);
        }

        public IceSessionException(String message) {
            super(message);
        }

        public IceSessionException(Throwable cause) {
            super(cause);
        }
    }
}
