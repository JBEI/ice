package org.jbei.ice.lib.managers;

import org.hibernate.Session;
import org.jbei.ice.lib.logging.Logger;

public abstract class Manager {

    protected static Session getSession() {
        return HibernateHelper.getSession();
    }

    public static void dbDelete(Object obj) throws ManagerException {
        Session session = getSession();
        try {
            // Do not assign transaction to value

            session.beginTransaction();
            try {
                session.delete(obj);
                session.getTransaction().commit();
            } catch (org.hibernate.NonUniqueObjectException e) {
                // This is the correct situation to merge objects.
                String msg = "Merging persistence object: " + obj.toString();
                Logger.info(msg);
                obj = session.merge(obj);
                session.delete(obj);
                session.getTransaction().commit();
            } catch (Exception e) {
                throw e;
            }

        } catch (Exception e) {
            session.getTransaction().rollback();
            if (session.getTransaction().wasRolledBack()) {
                Logger.error("I think rollback is not working!");
            }

            e.printStackTrace();

            throw new ManagerException("dbDelete exception", e);
        } finally {

        }

    }

    public static Object dbSave(Object obj) throws ManagerException {
        Object result = null;

        Session session = getSession();
        try {
            // Do not assign transaction to value
            session.beginTransaction();

            try {
                session.saveOrUpdate(obj);
                session.getTransaction().commit();
            } catch (org.hibernate.NonUniqueObjectException e) {
                // This is the correct situation to merge objects.
                String msg = "Merging persistence object: " + obj.toString();
                Logger.info(msg);
                session.merge(obj);
                session.getTransaction().commit();
            } catch (Exception e) {
                throw e;
            }

            result = obj;
        } catch (Exception e) {

            session.getTransaction().rollback();
            if (session.getTransaction().wasRolledBack()) {
                Logger.error("I think rollback is not working!");
            }
            String msg = "dbSave exception: " + e.toString();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        } finally {

        }

        return result;
    }

    public static Object dbGet(Class<? extends Object> theClass, int id) throws ManagerException {
        Object result = null;
        Session tempSession = getSession();
        try {
            result = tempSession.load(theClass, id);
        } catch (Exception e) {
            throw new ManagerException("dbGet failed: ", e);
        } finally {

        }
        return result;
    }

    public static Object dbMerge(Object obj) throws ManagerException {
        Object result = null;
        Session session = getSession();
        try {
            String msg = "Merging persistence object: " + obj.toString();
            Logger.info(msg);
            result = session.merge(obj);
        } catch (Exception e) {
            throw new ManagerException("Merge failed: ", e);
        } finally {

        }
        return result;
    }
}
