package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Calendar;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.SessionData;

public class SessionManager {

    public static SessionData get(String sessionKey) throws ManagerException {
        SessionData sessionData = null;
        Session session = DAO.getSession();
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
            String msg = "Could not get SessionData by id " + sessionKey;
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        } finally {

        }

        return sessionData;
    }

    public static SessionData save(SessionData sessionData) throws ManagerException {
        SessionData result = null;
        try {
            result = (SessionData) DAO.save(sessionData);

        } catch (Exception e) {
            String msg = "Could not save SessionData " + sessionData.getSessionKey() + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }

        flush(); //clear expired sessions here because there is no chron mechanism.

        return result;

    }

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
     * Flush the database of expired session's
     */

    public static void flush() {
        Session session = DAO.getSession();
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
        }
    }
}
