package org.jbei.ice.lib.bulkimport;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Hibernate Data accessor object for retrieving {@link BulkImportDraft} objects
 * from the database
 *
 * @author Hector Plahar
 */
class BulkImportDraftDAO extends HibernateRepository {

    public BulkImportDraft retrieveById(long id) throws DAOException {
        return (BulkImportDraft) super.get(BulkImportDraft.class, id);
    }

    public BulkImportDraft retrieveByIdWithContents(long id) throws DAOException {
        Session session = newSession();

        try {
            session.getTransaction().begin();
            BulkImportDraft result = (BulkImportDraft) session.get(BulkImportDraft.class, id);
            if (result == null) {
                session.getTransaction().rollback();
                throw new DAOException("Could not locate draft with id \"" + id + "\"");
            }
            result.getContents().size();
            session.getTransaction().commit();
            return result;
        } catch (HibernateException e) {
            Logger.error(e);
            session.getTransaction().rollback();
            throw new DAOException(e);
        } finally {
            closeSession(session);
        }
    }

    public BulkImportDraft save(BulkImportDraft draft) throws DAOException {
        return (BulkImportDraft) super.saveOrUpdate(draft);
    }

    public void delete(BulkImportDraft draft) throws DAOException {
        super.delete(draft);
    }

    public BulkImportDraft addEntry(BulkImportDraft draft, Entry entry) throws DAOException {

        Session session = newSession();

        try {
            session.getTransaction().begin();
            BulkImportDraft result = (BulkImportDraft) session.get(BulkImportDraft.class, draft.getId());
            if (result == null) {
                session.getTransaction().rollback();
                throw new DAOException("Could not locate draft with id \"" + draft.getId() + "\"");
            }

            if (result.getContents().contains(entry)) {
                session.getTransaction().commit();
                return result;
            }

            result.getContents().add(entry);
            session.saveOrUpdate(result);
            session.getTransaction().commit();
            return result;
        } catch (HibernateException e) {
            Logger.error(e);
            session.getTransaction().rollback();
            throw new DAOException(e);
        } finally {
            closeSession(session);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<BulkImportDraft> retrieveByAccount(Account account) throws DAOException {
        Session session = null;
        ArrayList<BulkImportDraft> result;

        try {
            session = newSession();
            session.getTransaction().begin();
            Query query = session.createQuery("from " + BulkImportDraft.class.getName() + " where account = :account");
            query.setParameter("account", account);
            result = new ArrayList<BulkImportDraft>(query.list());
            session.getTransaction().commit();
            return result;

        } catch (HibernateException he) {
            if (session != null)
                session.getTransaction().rollback();
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }

    public int retrieveSavedDraftCount(long draftId) throws DAOException {
        Session session = null;

        try {
            session = newSession();
            session.getTransaction().begin();
            Query query = session.createSQLQuery(
                    "select count(*) from bulk_import_draft_entry where bulk_import_draft_id = " + draftId);
            int count = ((BigInteger) query.uniqueResult()).intValue();
            session.getTransaction().commit();
            return count;

        } catch (HibernateException he) {
            Logger.error(he);
            if (session != null) {
                session.getTransaction().rollback();
            }
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }
}
