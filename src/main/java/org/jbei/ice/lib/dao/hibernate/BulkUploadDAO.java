package org.jbei.ice.lib.dao.hibernate;

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
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.entry.model.Entry;

import java.util.ArrayList;
import java.util.Iterator;
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
            result = new ArrayList<BulkUpload>(query.list());
            return result;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getUploadEntryCount(long id) throws DAOException {
        Number number = (Number) currentSession().createCriteria(BulkUpload.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("id", id))
                .uniqueResult();

        return number.intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<BulkUpload> retrieveByStatus(BulkUploadStatus status) throws DAOException {
        Session session = currentSession();
        ArrayList<BulkUpload> result;

        try {
            Query query = session.createQuery("from " + BulkUpload.class.getName() + " where status = :status");
            query.setParameter("status", status);
            result = new ArrayList<BulkUpload>(query.list());
            return result;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int retrieveSavedDraftCount(long draftId) throws DAOException {
        Session session = currentSession();

        try {
            Query query = session
                    .createSQLQuery("select count(*) from bulk_upload_entry where bulk_upload_id = " + draftId);
            return ((Number) query.uniqueResult()).intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Long> getEntryIds(long importId) throws DAOException {
        Query query = currentSession()
                .createSQLQuery("select entry_id from bulk_upload_entry where bulk_upload_id = " + importId);
        try {
            ArrayList<Long> list = new ArrayList<>();
            ArrayList<Number> queryList = (ArrayList<Number>) query.list();
            for (Number number : queryList)
                list.add(number.longValue());
            return list;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Entry> retrieveDraftEntries(long id, int start, int limit) throws DAOException {
        Query query = currentSession()
                .createSQLQuery("select entry_id from bulk_upload_entry where bulk_upload_id = " + id
                                        + " limit " + limit + " offset " + start);
        List list = query.list();

        try {
            List<Entry> result = DAOFactory.getEntryDAO().getEntriesByIdSet(list);
            if (result != null && result.size() > 0)
                return new ArrayList<>(result);
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }

        BulkUpload bulkUpload = super.get(BulkUpload.class, id);
        Iterator<Entry> iterator = bulkUpload.getContents().iterator();
        int i = -1;
        ArrayList<Entry> results = new ArrayList<>();
        while (iterator.hasNext()) {
            i += 1;
            if (i < start)
                continue;

            if (results.size() == limit)
                return results;

            Entry next = iterator.next();
            results.add(next);
        }

        return results;
    }

    @Override
    public BulkUpload get(long id) throws DAOException {
        return super.get(BulkUpload.class, id);
    }
}
