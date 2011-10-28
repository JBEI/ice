package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Calendar;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.SessionData;

/**
 * Manipulate {@link SessionData} objects in the database.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class SessionManager {
    /**
     * Retrieve {@link SessionData} object by its sessionKey.
     * 
     * @param sessionKey
     * @return SessionData object.
     * @throws ManagerException
     */
    public static SessionData get(String sessionKey) throws ManagerException {
        SessionData sessionData = null;
        Session session = DAO.newSession();
        try {
            String queryString = "from SessionData where sessionKey = :sessionKey";
            Query query = session.createQuery(queryString);
            query.setString("sessionKey", sessionKey);

            sessionData = (SessionData) query.uniqueResult();

            //stop expired sessions right here
            if (sessionData != null) {
                if (sessionData.getExpireDate() < Calendar.getInstance().getTimeInMillis()) {
                    delete(sessionData);
                    sessionData = null;
                }
            }

        } catch (Exception e) {
            String msg = "Could not get SessionData by sessionKey: " + sessionKey;
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return sessionData;
    }

    /**
     * Retrieve the {@link SessionData} object associated with the given {@link Account}.
     * 
     * @param account
     * @return SessionData object. Null if the session does not exist, the user has logged out, or
     *         session has expired.
     * @throws ManagerException
     */
    public static SessionData get(Account account) throws ManagerException {
        SessionData sessionData = null;
        Session session = DAO.newSession();
        try {
            String queryString = "from SessionData where account = :account";
            Query query = session.createQuery(queryString);
            query.setParameter("account", account);

            sessionData = (SessionData) query.uniqueResult();

            //stop expired sessions right here
            if (sessionData != null) {
                if (sessionData.getExpireDate() < Calendar.getInstance().getTimeInMillis()) {
                    delete(sessionData);
                    sessionData = null;
                }
            }

        } catch (Exception e) {
            String msg = "Could not get SessionData by account: " + account.getEmail();
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return sessionData;
    }

    /**
     * Save the given {@link SessionData} object in the database.
     * 
     * @param sessionData
     * @return Saved SessionData object.
     * @throws ManagerException
     */
    public static SessionData save(SessionData sessionData) throws ManagerException {
        SessionData result = null;
        try {
            result = (SessionData) DAO.save(sessionData);

        } catch (Exception e) {
            String msg = "Could not save SessionData " + sessionData.getSessionKey() + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }

        flush(); //TODO: Move flush mechanism into cron mechanism.

        return result;

    }

    /**
     * Delete the given {@link SessionData} object in the database.
     * 
     * @param sessionData
     * @throws ManagerException
     */
    public static void delete(SessionData sessionData) throws ManagerException {
        try {
            DAO.delete(sessionData);
        } catch (Exception e) {
            String msg = "Could not delete session " + sessionData.getSessionKey();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    /**
     * Flush the database of expired sessions.
     */
    public static void flush() {
        Session session = DAO.newSession();
        try {
            String queryString = "from SessionData sessionData where sessionData.expireDate < :now";
            Query query = session.createQuery(queryString);
            query.setLong("now", Calendar.getInstance().getTimeInMillis());
            @SuppressWarnings("unchecked")
            ArrayList<SessionData> result = new ArrayList<SessionData>(query.list());
            for (SessionData sessionData : result) {
                DAO.delete(sessionData);
            }
        } catch (Exception e) {
            String msg = "Could not flush expired sessions: " + e.toString();
            Logger.error(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}
