package org.jbei.ice.lib.bulkimport;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO to manage {@link BulkImport} objects in the database.
 *
 * @author Hector Plahar
 */
class BulkImportDAO extends HibernateRepository {

    @SuppressWarnings("unchecked")
    public ArrayList<BulkImport> retrieveByUser(Account account) throws DAOException {

        Session session = newSession();
        session.getTransaction().begin();
        Query query = session.createQuery("from " + BulkImport.class.getName() + " where account = :account");
        query.setEntity("account", account);

        try {
            ArrayList<BulkImport> result = new ArrayList<BulkImport>(query.list());
            session.getTransaction().commit();
            return result;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
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
     * @throws DAOException
     */
    public BulkImport saveBulkImport(BulkImport data) throws DAOException {

        if (data == null) {
            throw new DAOException("Cannot create record from null data");
        }

        try {
            return (BulkImport) super.saveOrUpdate(data);
        } catch (DAOException e) {
            throw new DAOException("Exception saving bulkImport record", e);
        }
    }

    /**
     * Saves bulk import in preparation for verification
     *
     * @param data bulk import data to save
     * @throws DAOException
     */
    public void submitBulkImportForVerification(BulkImport data) throws DAOException {
        if (data == null)
            throw new DAOException("Cannot submit null data");

        try {
            AccountController controller = new AccountController();
            Account account = controller.getSystemAccount();
            data.setAccount(account);
            saveBulkImport(data);
        } catch (ControllerException e) {
            throw new DAOException(e);
        }
    }

    /**
     * Delete the given {@link BulkImport} object in the database.
     *
     * @param bulkImport {@link BulkImport} to delete
     * @throws DAOException
     */
    public void delete(BulkImport bulkImport) throws DAOException {
        super.delete(bulkImport);
    }

    /**
     * Retrieve all {@link BulkImport} objects in the database.
     *
     * @return List of BulkImport objects.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<BulkImport> retrieveAll() throws DAOException {
        List<BulkImport> list = new ArrayList<BulkImport>();
        list.addAll(super.retrieveAll(BulkImport.class));
        return list;
    }

    /**
     * Retrieve a {@link BulkImport} object by its id.
     *
     * @param importId unique identifier (typically synthetic id) for bulk import object
     * @return BulkImport object.
     * @throws DAOException
     */
    public BulkImport retrieveById(long importId) throws DAOException {
        Session session = newSession();
        session.getTransaction().begin();
        Query query = session.createQuery("from " + BulkImport.class.getName() + " where id = :id");
        query.setParameter("id", importId);

        try {
            BulkImport result = (BulkImport) query.uniqueResult();
            session.getTransaction().commit();
            return result;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("Error retrieving bulk import record", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

//        return (BulkImport) super.get(BulkImport.class, importId);
    }

    /**
     * Retrieve a {@link BulkImport} type by its id.
     *
     * @param id unique identifier (typically synthetic id) for bulk import object
     * @return Type
     * @throws DAOException
     */
    public String retrieveType(long id) throws DAOException {
        BulkImport record = (BulkImport) super.get(BulkImport.class, id);
        if (record == null)
            throw new DAOException("Could not retrieve bulk import record with id " + id);

        return record.getType();
    }
}
