package org.jbei.ice.lib.managers;

import org.hibernate.Query;
import org.jbei.ice.web.SessionData;
import org.jbei.ice.lib.logging.Logger;

public class SessionManager extends Manager {
	public static SessionData get(String sessionKey) throws ManagerException {
		SessionData sessionData = null;
		try {
			String queryString = "from SessionData where sessionKey = :sessionKey";
			Query query = HibernateHelper.getSession().createQuery(queryString);
			query.setEntity("sessionKey", sessionKey);
			
			sessionData = (SessionData) query.uniqueResult();
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
			String msg = "Could not save SessionData " + sessionData.getSessionKey();
			Logger.error(msg);
			throw new ManagerException(msg, e);
		}
		return result;
		
	}

}
