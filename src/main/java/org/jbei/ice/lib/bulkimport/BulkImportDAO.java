package org.jbei.ice.lib.bulkimport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

/**
 * Manage {@link BulkImport} objects in the database.
 * 
 * @author Hector Plahar
 * 
 */
class BulkImportDAO extends HibernateRepository {

    @SuppressWarnings("unchecked")
    public ArrayList<BulkImport> retrieveByUser(Account account) throws DAOException {

        Session session = DAO.newSession();
        Query query = session.createQuery("from " + BulkImport.class.getName()
                + " where account = :account");
        query.setEntity("account", account);

        try {
            ArrayList<BulkImport> result = new ArrayList<BulkImport>(query.list());
            return result;
        } catch (Exception e) {
            throw new DAOException("Error retrieving bulk import record", e);
        } finally {

            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Create a new {@link BulkImport} object in the database.
     * 
     * @param data
     * @return Saved BulkImport
     * @throws ManagerException
     */
    public BulkImport createBulkImportRecord(BulkImport data) throws DAOException {

        if (data == null) {
            throw new DAOException("Cannot create record from null data");
        }

        try {
            Date creationDate = new Date(System.currentTimeMillis());
            data.setCreationTime(creationDate);
            return (BulkImport) DAO.save(data);
        } catch (DAOException e) {
            throw new DAOException("Exception saving bulkImport record", e);
        }
    }

    /**
     * Saves bulk import in preparation for verification
     * 
     * @param data
     *            bulk import data to save
     * @return saved bulk import record
     * @throws ManagerException
     */
    public void submitBulkImportForVerification(BulkImport data) throws DAOException {
        if (data == null)
            throw new DAOException("Cannot submit null data");

        try {
            AccountController controller = new AccountController();
            Account account = controller.getSystemAccount();
            data.setAccount(account);
            createBulkImportRecord(data);
        } catch (ControllerException e) {
            throw new DAOException(e);
        }
    }

    public BulkImport updateBulkImportRecord(long id, BulkImport data) throws DAOException {
        if (data == null) {
            throw new DAOException("Cannot create record from null data");
        }

        BulkImport current = retrieveById(id);
        if (current == null)
            throw new DAOException("Record with id " + id + " does not exist");

        try {
            data.setId(current.getId());
            data.setCreationTime(current.getCreationTime());
            return (BulkImport) DAO.save(data);
        } catch (DAOException e) {
            throw new DAOException("Exception updating bulk import record " + id, e);
        }
    }

    /**
     * Delete the given {@link BulkImport} object in the database.
     * 
     * @param bulkImport
     * @throws ManagerException
     */
    public void delete(BulkImport bulkImport) throws DAOException {
        super.delete(bulkImport);
    }

    /**
     * Retrieve all {@link BulkImport} objects in the database.
     * 
     * @return List of BulkImport objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public List<BulkImport> retrieveAll() throws DAOException {
        List<BulkImport> list = new ArrayList<BulkImport>();
        list.addAll(super.retrieveAll(BulkImport.class));
        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<BulkImport> retrieveSavedDrafts() throws DAOException {
        Session session = DAO.newSession();
        List<BulkImport> list = new ArrayList<BulkImport>();

        try {
            session.beginTransaction();
            Query query = session.createQuery("from " + BulkImport.class.getName()
                    + " where isDraft = false");
            list.addAll(query.list());
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException("Error retrieving list of saved bulk import drafts", he);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return list;
    }

    /**
     * Retrieve a {@link BulkImport} object by its id.
     * 
     * @param importId
     *            BulkImport's id.
     * @return BulkImport object.
     * @throws ManagerException
     */
    public BulkImport retrieveById(long importId) throws DAOException {
        return (BulkImport) super.get(BulkImport.class, importId);
    }

    /**
     * Retrieve a {@link BulkImport} type by its id.
     * 
     * @param id
     *            BulkImport's id.
     * @return Type
     * @throws ManagerException
     */
    public String retrieveType(long id) throws DAOException {
        Session session = DAO.newSession();
        try {
            SQLQuery query = session.createSQLQuery("select type from bulk_import where id = :id ");
            query.setLong("id", id);
            return (String) query.uniqueResult();
        } catch (Exception e) {
            throw new DAOException("Error retrieving bulk import record type", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
