package org.jbei.ice.lib.dao.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.bulkupload.BulkUpload;
import org.jbei.ice.lib.bulkupload.BulkUploadStatus;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate Data accessor object for retrieving {@link org.jbei.ice.lib.bulkupload.BulkUpload} objects
 * from the database
 *
 * @author Hector Plahar
 */
public class BulkUploadDAO extends HibernateRepository<BulkUpload> {

    @SuppressWarnings("unchecked")
    public ArrayList<BulkUpload> retrieveByAccount(Account account) throws DAOException {
        Session session = currentSession();
        ArrayList<BulkUpload> result;

        try {
            Query query = session.createQuery("from " + BulkUpload.class.getName() + " where account = :account AND "
                    + "status != :status");
            query.setParameter("account", account);
            query.setParameter("status", BulkUploadStatus.PENDING_APPROVAL);
            result = new ArrayList<>(query.list());
            return result;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<BulkUpload> retrieveByStatus(BulkUploadStatus status) throws DAOException {
        Session session = currentSession();
        ArrayList<BulkUpload> result;

        try {
            Query query = session.createQuery("from " + BulkUpload.class.getName() + " where status = :status");
            query.setParameter("status", status);
            result = new ArrayList<>(query.list());
            return result;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int retrieveSavedDraftCount(long draftId) throws DAOException {
        try {
            Number number = (Number) currentSession().createCriteria(BulkUpload.class).add(Restrictions.eq("id", draftId))
                    .createAlias("contents", "entry")
                    .setProjection(Projections.countDistinct("entry.id"))
                    .uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Long> getEntryIds(BulkUpload upload) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(BulkUpload.class)
                    .add(Restrictions.eq("id", upload.getId()))
                    .createAlias("contents", "entry")
                    .setProjection(Projections.property("entry.id"));
            return new ArrayList<>(criteria.list());
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Entry> retrieveDraftEntries(long id, int start, int limit) throws DAOException {
        Query query = currentSession().createQuery("select b.contents as entry from " + BulkUpload.class.getName()
                + " b where b.id=" + id);
        query.setFirstResult(start);
        query.setMaxResults(limit);
        List l = query.list();
        return new ArrayList<>(l);
    }

    @Override
    public BulkUpload get(long id) throws DAOException {
        return super.get(BulkUpload.class, id);
    }
}
