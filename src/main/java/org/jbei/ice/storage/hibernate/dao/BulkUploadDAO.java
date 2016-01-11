package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.bulkupload.BulkUploadStatus;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.BulkUpload;
import org.jbei.ice.storage.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate Data accessor object for retrieving {@link BulkUpload} objects
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

    @SuppressWarnings("unchecked")
    public int setEntryStatus(BulkUpload upload, Visibility status) {
        // get all entries (and linked)
        try {
            Criteria criteria = currentSession().createCriteria(BulkUpload.class)
                    .add(Restrictions.eq("id", upload.getId()))
                    .createAlias("contents", "entry")
                    .setProjection(Projections.property("entry.id"));

            List<Long> entryIds = criteria.list();

            Criteria c = currentSession().createCriteria(Entry.class)
                    .add(Restrictions.in("id", entryIds))
                    .createAlias("linkedEntries", "links")
                    .setProjection(Projections.property("links.id"));
            entryIds.addAll(c.list());

            String hql = "update " + Entry.class.getName() + " set visibility=:v where id in :ids";
            return currentSession().createQuery(hql)
                    .setParameter("v", status.getValue())
                    .setParameterList("ids", entryIds)
                    .executeUpdate();

        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    @Override
    public BulkUpload get(long id) throws DAOException {
        return super.get(BulkUpload.class, id);
    }
}
