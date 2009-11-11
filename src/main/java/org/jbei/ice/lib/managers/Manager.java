package org.jbei.ice.lib.managers;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class Manager {
	protected static Session session = HibernateHelper.getSession();
	
	public static void dbDelete(Object obj) throws Exception {
		
		try {
			Transaction tx = session.beginTransaction();
			session.delete(obj);
			tx.commit();
		} catch(Exception e) {
			throw e;
		}
		
	}

	public static Object dbSave(Object obj) throws ManagerException {
		Object result = null;
		try {
			Transaction tx = session.beginTransaction();
			Serializable generatedId = session.save(obj);
			tx.commit();
			result = session.load(obj.getClass(), generatedId);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ManagerException("dbSave exception", e);
		}
	
		return result;
	}
	
	public static Object dbGet(Class theClass, int id) throws Exception {
		Object result = null;
		try {
			result = session.load(theClass, id);
		} catch (Exception e) {
			throw e;
		}	
			return result;
		
	}
	

}
