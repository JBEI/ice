package org.jbei.ice.web;

import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.jbei.ice.lib.authentication.IAuthenticationBackend;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;

public class IceSession extends WebSession {

    private static final long serialVersionUID = 1L;
    private IAuthenticationBackend authenticator = null;
    private String COOKIE_NAME = JbeirSettings.getSetting("COOKIE_NAME");

    public IceSession(Request request, Response response, IAuthenticationBackend authenticator2) {
        super(request);

        this.authenticator = authenticator2;
    }

    /**
     * Save account id into SessionData, and save into db for persistent
     * token based authentication.
     * 
     * @throws ManagerException
     */
    public void makeSessionPersistent(WebResponse response) throws ManagerException {
        SessionData savedSession = getSessionData();
        HashMap<String, Object> data = savedSession.getData();
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        data.put("accountId", getAccount().getId());
        savedSession.setData(data);

        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expireDate = currentTime + 7776000000L; //90 days

        Cookie cookie = new Cookie(COOKIE_NAME, savedSession.getSessionKey());
        cookie.setPath("/");
        cookie.setMaxAge(7776000);
        response.addCookie(cookie);

        savedSession.setExpireDate(expireDate);
        try {
            PersistentSessionDataWrapper.getInstance().persist(savedSession);
        } catch (ManagerException e) {
            throw e;
        }

    }

    public boolean authenticateUser(String login, String password) {
        Account account = null;
        boolean result = false;
        try {
            account = authenticator.authenticate(login, password);

            if (account != null) {
                AccountPreferences accountPreferences = AccountManager
                        .getAccountPreferences(account);
                if (accountPreferences == null) {
                    accountPreferences = new AccountPreferences();
                    accountPreferences.setAccount(account);
                    setAccountPreferences(accountPreferences);
                }

                SessionData sessionData = getSessionData();
                if (sessionData == null) {
                    // User authenticates but this session is not associated.
                    while (true) {
                        sessionData = getSessionData();
                        if (sessionData != null) {
                            break;
                        }
                    }
                }
                sessionData.getData().put("accountId", account.getId());
                PersistentSessionDataWrapper.getInstance().persist(sessionData);
                result = true;
            }
        } catch (Exception e) {
            Logger.warn("Could not authenticate user " + login + ": " + e.toString());
            e.printStackTrace();
        }
        return result;
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
        SessionData sessionData = PersistentSessionDataWrapper.getInstance().getSessionData(
                getRequest());
        return sessionData;
    }

    public Account getAccount() {
        Account account = null;
        SessionData sessionData = PersistentSessionDataWrapper.getInstance().getSessionData(
                getRequest());
        if (sessionData != null) {
            HashMap<String, Object> data = sessionData.getData();
            if (data.containsKey("accountId")) {
                Integer accountId = (Integer) data.get("accountId");
                try {
                    account = AccountManager.get(accountId);
                } catch (ManagerException e) {
                    String msg = "Could not getAccount from IceSession: " + e.toString();
                    Logger.error(msg);
                }
            }
        }

        return account;
    }

    public void setAccountPreferences(AccountPreferences accountPreferences) {
        try {
            AccountManager.save(accountPreferences);
        } catch (ManagerException e) {
            String msg = "Could not setAccountPreferences in IceSession: " + e.toString();
            Logger.error(msg);
        }
    }

    public AccountPreferences getAccountPreferences() {
        AccountPreferences result = null;
        try {
            result = AccountManager.getAccountPreferences(getAccount());
        } catch (ManagerException e) {
            String msg = "Could not getAccountPreferences in IceSession: " + e.toString();
            Logger.error(msg);
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

}
