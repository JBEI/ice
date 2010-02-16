package org.jbei.ice.lib.managers;

import java.util.Calendar;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.SessionData;

public class SessionManager extends Manager {

    public static SessionData get(String sessionKey) throws ManagerException {
        SessionData sessionData = null;
        try {
            String queryString = "from SessionData where sessionKey = :sessionKey";
            Query query = HibernateHelper.getSession().createQuery(queryString);
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
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }

        return sessionData;
    }

    public static SessionData save(SessionData sessionData) throws ManagerException {
        SessionData result = null;
        try {
            result = (SessionData) dbSave(sessionData);

        } catch (Exception e) {
            String msg = "Could not save SessionData " + sessionData.getSessionKey() + e.toString();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }

        flush(); //clear expired sessions here because there is no chron mechanism.

        return result;

    }

    public static void delete(SessionData sessionData) throws ManagerException {
        try {
            dbDelete(sessionData);
        } catch (Exception e) {
            String msg = "Could not delete session " + sessionData.getSessionKey();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }
    }

    /**
     * Flush the database of expired session's
     */
    public static void flush() {

        try {
            String queryString = "delete SessionData sessionData where sessionData.expireDate < :now";
            Transaction tx = getSession().beginTransaction();
            Query query = getSession().createQuery(queryString);
            query.setLong("now", Calendar.getInstance().getTimeInMillis());
            query.executeUpdate();
            tx.commit();

        } catch (Exception e) {
            String msg = "Could not flush expired sessions: " + e.toString();
            Logger.error(msg);
        }
    }

}
