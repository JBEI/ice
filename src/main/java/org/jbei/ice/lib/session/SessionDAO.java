package org.jbei.ice.lib.session;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Manipulate {@link SessionData} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class SessionDAO extends HibernateRepository {
    /**
     * Retrieve {@link SessionData} object by its sessionKey.
     *
     * @param sessionKey
     * @return SessionData object.
     * @throws DAOException
     */
    public SessionData get(String sessionKey) throws DAOException {
        SessionData sessionData = null;
        Session session = newSession();
        try {
            String queryString = "from " + SessionData.class.getName() + " where sessionKey = :sessionKey";
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
            throw new DAOException(msg, e);
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
     * @throws DAOException
     */
    public SessionData get(Account account) throws DAOException {
        SessionData sessionData = null;
        Session session = newSession();
        try {
            String queryString = "from " + SessionData.class.getName() + " where account = :account";
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
            throw new DAOException(msg, e);
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
    public SessionData save(SessionData sessionData) throws ManagerException {
        SessionData result = null;
        try {
            result = (SessionData) super.saveOrUpdate(sessionData);

        } catch (Exception e) {
            String msg = "Could not save SessionData " + sessionData.getSessionKey() + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }

        deleteExpiredSessions(); //TODO: Move deleteExpiredSessions mechanism into cron mechanism.

        return result;
    }

    /**
     * Delete the given {@link SessionData} object in the database.
     *
     * @param sessionData
     * @throws ManagerException
     */
    public void delete(SessionData sessionData) throws DAOException {
        super.delete(sessionData);
    }

    /**
     * Flush the database of expired sessions.
     */
    public void deleteExpiredSessions() {
        Session session = newSession();
        try {
            String queryString = "from SessionData sessionData where sessionData.expireDate < :now";
            Query query = session.createQuery(queryString);
            query.setLong("now", Calendar.getInstance().getTimeInMillis());
            @SuppressWarnings("unchecked")
            ArrayList<SessionData> result = new ArrayList<SessionData>(query.list());
            for (SessionData sessionData : result) {
                delete(sessionData);
            }
        } catch (Exception e) {
            String msg = "Could not delete expired sessions: " + e.toString();
            Logger.error(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}
