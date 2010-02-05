package org.jbei.ice.lib.managers;

import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class Manager {
    protected static Session session = HibernateHelper.getSession();

    public static void dbDelete(Object obj) throws ManagerException {
        try {
            Transaction tx = session.beginTransaction();
            obj = session.merge(obj);
            session.delete(obj);

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("dbDelete exception", e);
        }

    }

    public static Object dbSave(Object obj) throws ManagerException {
        Object result = null;
        try {
            Transaction tx = session.beginTransaction();
            obj = session.merge(obj);
            session.saveOrUpdate(obj);

            tx.commit();

            result = obj;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ManagerException("dbSave exception", e);
        }

        return result;
    }

    public static Object dbGet(Class<? extends Object> theClass, int id) throws Exception {
        Object result = null;
        try {
            result = session.load(theClass, id);
        } catch (Exception e) {
            throw e;
        }
        return result;
    }
}
