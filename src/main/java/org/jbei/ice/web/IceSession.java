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
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;

public class IceSession extends WebSession {
    private static final long serialVersionUID = 1L;

    private String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");

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

    public SessionData authenticateUser(String login, String password) throws IceSessionException,
            InvalidCredentialsException {

        SessionData sessionData = AccountController.authenticate(login, password);

        if (sessionData == null) {
            // User authenticates but this session is not associated.
            String msg = "User is authenticated but this session is not associated";
            Logger.error(msg, new Exception("Error"));
            throw new RuntimeException(msg);
        }
        setCookie(sessionData);
        return sessionData;
    }

    public void deAuthenticateUser() {
        clearSavedSession();
    }

    public boolean isAuthenticated() {
        return (getAccount() == null) ? false : true;
    }

    //getters and setters
    public static IceSession get() {
        return (IceSession) Session.get();
    }

    public String getSessionKey() {
        String result = null;
        result = (getSessionData() != null) ? getSessionData().getSessionKey() : "";

        return result;
    }

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
        if (sessionData != null) {
            setCookie(sessionData);
        }

        return sessionData;
    }

    private void setCookie(SessionData sessionData) {
        Cookie cookie = new Cookie(COOKIE_NAME, sessionData.getSessionKey());
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

    public Account getAccount() {
        Account account = null;
        SessionData sessionData = getSessionData();
        if (sessionData != null) {
            account = sessionData.getAccount();
        }

        return account;
    }

    public void setAccountPreferences(AccountPreferences accountPreferences) {
        try {
            AccountController.saveAccountPreferences(accountPreferences);
        } catch (ControllerException e) {
            String msg = "Could not setAccountPreferences in IceSession: " + e.toString();
            Logger.error(msg, e);
        }
    }

    public void saveAccountPreferences() {
        try {
            AccountController.saveAccountPreferences(getAccountPreferences());
        } catch (ControllerException e) {
            String msg = "Could not save accountPreferences in IceSession";
            Logger.error(msg, e);
        }
    }

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
    private void clearSavedSession() {
        PersistentSessionDataWrapper.getInstance().delete(getSessionData().getSessionKey());
    }

    private WebRequest getRequest() {

        WebRequestCycle webRequestCycle = (WebRequestCycle) WebRequestCycle.get();
        return webRequestCycle.getWebRequest();
    }

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
