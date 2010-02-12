package org.jbei.ice.lib.managers;

import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;

public abstract class Manager {

    protected static Session getSession() {
        return HibernateHelper.getSession();
    }

    public static void dbDelete(Object obj) throws ManagerException {
        try {
            // Do not assign transaction to value
            getSession().beginTransaction();
            obj = getSession().merge(obj);
            getSession().delete(obj);
            getSession().getTransaction().commit();

        } catch (Exception e) {
            getSession().getTransaction().rollback();
            if (getSession().getTransaction().wasRolledBack()) {
                Logger.error("I think rollback is not working!");
            }

            e.printStackTrace();

            throw new ManagerException("dbDelete exception", e);
        }
    }

    public static Object dbSave(Object obj) throws ManagerException {
        Object result = null;

        try {
            // Do not assign transaction to value
            getSession().beginTransaction();

            // never merge(), no matter how tempting. E.g.: 
            // obj = getSession().merge(obj);
            getSession().saveOrUpdate(obj);
            getSession().getTransaction().commit();

            result = obj;
        } catch (Exception e) {

            getSession().getTransaction().rollback();
            if (getSession().getTransaction().wasRolledBack()) {
                Logger.error("I think rollback is not working!");
            }

            throw new ManagerException("dbSave exception", e);
        }

        return result;
    }

    public static Object dbGet(Class<? extends Object> theClass, int id) throws ManagerException {
        Object result = null;
        try {
            result = getSession().load(theClass, id);
        } catch (Exception e) {
            throw new ManagerException("dbGet failed: ", e);
        }
        return result;
    }

    public static Object dbMerge(Object obj) throws ManagerException {
        Object result = null;
        try {
            result = getSession().merge(obj);
        } catch (Exception e) {
            throw new ManagerException("Merge failed: ", e);
        }
        return result;
    }
}
