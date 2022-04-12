package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.Entry;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.util.List;

/**
 * Data accessor object for {@link Attachment}s
 *
 * @author Hector Plahar
 */
public class AttachmentDAO extends HibernateRepository<Attachment> {

    public void delete(File attDir, Attachment attachment) {
        try {
            delete(attachment);
            deleteFile(attDir, attachment);
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Unknown exception ", e);
        }
    }

    /**
     * Retrieve all {@link Attachment}s associated with the given {@link Entry}.
     *
     * @param entry Entry whose attachments are desired
     * @return ArrayList of Attachments.
     * @throws DAOException
     */
    public List<Attachment> getByEntry(Entry entry) {
        try {
            CriteriaQuery<Attachment> query = getBuilder().createQuery(Attachment.class);
            Root<Attachment> from = query.from(Attachment.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            query.orderBy(getBuilder().desc(from.get("id")));
            return currentSession().createQuery(query).list();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve attachment by entry: " + entry.getId(), e);
        }
    }

    public boolean hasAttachment(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Attachment> from = query.from(Attachment.class);
            query.select(getBuilder().countDistinct(from.get("id"))).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult() > 0;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve attachment by entry: " + entry.getId(), e);
        }
    }

    /**
     * Retrieves attachment referenced by a unique file identifier
     *
     * @param fileId unique file identifier
     * @return retrieved attachment; null if none is found or there is a problem retrieving
     * the attachment
     */
    public Attachment getByFileId(String fileId) {
        try {
            CriteriaQuery<Attachment> query = getBuilder().createQuery(Attachment.class);
            Root<Attachment> from = query.from(Attachment.class);
            query.where(getBuilder().equal(from.get("fileId"), fileId));
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve attachment by fileId: " + fileId, e);
        }
    }

    public boolean deleteFile(File attDir, Attachment attachment) {
        File file = new File(attDir + File.separator + attachment.getFileId());
        try {
            return file.delete();
        } catch (Exception ioe) {
            Logger.error(ioe.getMessage());
            return false;
        }
    }

    @Override
    public Attachment get(long id) {
        return super.get(Attachment.class, id);
    }
}
