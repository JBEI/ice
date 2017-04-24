package org.jbei.ice.storage.hibernate.dao;

import com.google.common.io.ByteStreams;
import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.Entry;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Data accessor object for {@link Attachment}s
 *
 * @author Hector Plahar
 */
public class AttachmentDAO extends HibernateRepository<Attachment> {

    public Attachment save(File attDir, Attachment attachment, InputStream inputStream) {
        try {
            attachment = create(attachment);
            if (inputStream != null) {
                Files.write(Paths.get(attDir.getAbsolutePath(), attachment.getFileId()), ByteStreams.toByteArray(inputStream));
            }
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Exception writing attachment file ", e1);
        }

        return attachment;
    }

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
        } catch (HibernateException e) {
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
        } catch (HibernateException e) {
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
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve attachment by fileId: " + fileId, e);
        }
    }

    /**
     * Retrieve the {@link File} from the disk of the given {@link Attachment}.
     *
     * @param attachment attachment whose physical file is to be retrieved
     * @return File physical attachment file
     * @throws DAOException
     */
    public File getFile(File attDir, Attachment attachment) {
        File file = new File(attDir + File.separator + attachment.getFileId());
        if (!file.exists()) {
            throw new DAOException("Attachment file " + file.getAbsolutePath() + " does not exist");
        }
        return file;
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
