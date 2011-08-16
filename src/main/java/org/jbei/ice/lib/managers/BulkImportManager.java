package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.BulkImport;

public class BulkImportManager {

    public static BulkImport createBulkImportRecord(BulkImport data) throws ManagerException {

        if (data == null)
            throw new ManagerException("Cannot create record from null data");

        try {
            Date creationDate = new Date(System.currentTimeMillis());
            data.setCreationTime(creationDate);
            return (BulkImport) DAO.save(data);
        } catch (DAOException e) {
            throw new ManagerException("Exception saving bulkImport record", e);
        }
    }

    public static void delete(BulkImport bulkImport) throws ManagerException {
        try {
            DAO.delete(bulkImport);
        } catch (DAOException e) {

            throw new ManagerException("Failed to delete bulk import", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<BulkImport> retrieveAll() throws ManagerException {
        Session session = DAO.newSession();
        List<BulkImport> list = new ArrayList<BulkImport>();

        try {
            Query query = session.createQuery("from " + BulkImport.class.getName());
            list.addAll(query.list());
        } catch (HibernateException he) {
            throw new ManagerException("Error retrieving list of bulk imports", he);
        } finally {
            if (session.isOpen())
                session.close();
        }

        return list;
    }

    public static BulkImport retrieveById(long importId) throws ManagerException {
        Session session = DAO.newSession();
        Query query = session.createQuery("from " + BulkImport.class.getName() + " where id = :id");
        query.setLong("id", importId);
        try {
            return (BulkImport) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Error retrieving bulk import record", e);
        } finally {

            if (session != null && session.isOpen())
                session.close();
        }
    }

    public static String retrieveType(long id) throws ManagerException {
        Session session = DAO.newSession();
        try {
            SQLQuery query = session.createSQLQuery("select type from bulk_import where id = :id ");
            query.setLong("id", id);
            return (String) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Error retrieving bulk import record type", e);
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }
}
