package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.bulkupload.BulkUploadStatus;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.BulkUpload;
import org.jbei.ice.storage.model.Entry;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Hibernate Data accessor object for retrieving {@link BulkUpload} objects
 * from the database
 *
 * @author Hector Plahar
 */
public class BulkUploadDAO extends HibernateRepository<BulkUpload> {

    public List<BulkUpload> retrieveByAccount(Account account) {
        try {
            CriteriaQuery<BulkUpload> query = getBuilder().createQuery(BulkUpload.class);
            Root<BulkUpload> from = query.from(BulkUpload.class);
            query.where(getBuilder().equal(from.get("account"), account),
                    getBuilder().notEqual(from.get("status"), BulkUploadStatus.PENDING_APPROVAL));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<BulkUpload> retrieveByStatus(BulkUploadStatus status) {
        try {
            CriteriaQuery<BulkUpload> query = getBuilder().createQuery(BulkUpload.class);
            Root<BulkUpload> from = query.from(BulkUpload.class);
            query.where(getBuilder().equal(from.get("status"), status));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int retrieveSavedDraftCount(long draftId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<BulkUpload> from = query.from(BulkUpload.class);
            Join<BulkUpload, Entry> contents = from.join("contents");

            query.select(getBuilder().countDistinct(contents.get("id"))).where(getBuilder()
                    .equal(from.get("id"), draftId));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Long> getEntryIds(BulkUpload upload) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<BulkUpload> from = query.from(BulkUpload.class);
            Join<BulkUpload, Entry> contents = from.join("contents");

            query.select(contents.get("id")).where(getBuilder().equal(from.get("id"), upload.getId()));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Entry> retrieveDraftEntries(long id, int start, int limit) {
        try {
            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<BulkUpload> from = query.from(BulkUpload.class);
            Join<BulkUpload, Entry> contents = from.join("contents");
            query.select(contents)
                    .where(getBuilder().equal(from.get("id"), id))
                    .orderBy(getBuilder().asc(contents.get("id")));
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public int setEntryStatus(BulkUpload upload, Visibility status) {
        try {
            List<Long> entryIds = getEntryIds(upload);
            if (entryIds.isEmpty())
                return 0;

            // include linked entries
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Entry> from = query.from(Entry.class);
            Join<Entry, Entry> linked = from.join("linkedEntries");
            query.select(linked.get("id")).where(from.get("id").in(entryIds));
            List<Long> linkedIds = currentSession().createQuery(query).list();
            entryIds.addAll(linkedIds);

            CriteriaUpdate<Entry> update = getBuilder().createCriteriaUpdate(Entry.class);
            Root<Entry> root = update.from(Entry.class);
            update.set(root.get("visibility"), status.getValue());
            update.where(root.get("id").in(entryIds));
            return currentSession().createQuery(update).executeUpdate();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    @Override
    public BulkUpload get(long id) {
        return super.get(BulkUpload.class, id);
    }
}
