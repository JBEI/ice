package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate Data accessor object for retrieving {@link BulkUpload} objects
 * from the database
 *
 * @author Hector Plahar
 */
class BulkUploadDAO extends HibernateRepository<BulkUpload> {

    public BulkUpload retrieveById(long id) throws DAOException {
        return super.get(BulkUpload.class, id);
    }

    public BulkUpload retrieveByIdWithContents(long id) throws DAOException {
        Session session = currentSession();

        try {
            BulkUpload result = (BulkUpload) session.get(BulkUpload.class, id);
            if (result == null) {
                throw new DAOException("Could not locate draft with id \"" + id + "\"");
            }
            result.getContents().size();
            return result;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        } finally {
            closeSession();
        }
    }

    public BulkUpload save(BulkUpload draft) throws DAOException {
        return super.save(draft);
    }

    public BulkUpload update(BulkUpload draft) throws DAOException {
        return super.saveOrUpdate(draft);
    }

    public void delete(BulkUpload draft) throws DAOException {
        super.delete(draft);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<BulkUpload> retrieveByAccount(Account account) throws DAOException {
        Session session = null;
        ArrayList<BulkUpload> result;

        try {
            session = currentSession();
            Query query = session.createQuery("from " + BulkUpload.class.getName() + " where account = :account");
            query.setParameter("account", account);
            result = new ArrayList<BulkUpload>(query.list());
            return result;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession();
        }
    }

    public int retrieveSavedDraftCount(long draftId) throws DAOException {
        Session session = null;

        try {
            session = currentSession();
            Query query = session
                    .createSQLQuery("select count(*) from bulk_upload_entry where bulk_upload_id = " + draftId);
            int count = ((Number) query.uniqueResult()).intValue();
            return count;

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession();
        }
    }
}
