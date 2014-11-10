package org.jbei.ice.lib.dao.hibernate;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.utils.FileUtils;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class AttachmentDAO extends HibernateRepository<Attachment> {

    public Attachment save(File attDir, Attachment attachment, InputStream inputStream) throws DAOException {
        try {
            create(attachment);
            if (inputStream != null)
                FileUtils.writeFile(attDir, attachment.getFileId(), inputStream);
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        }

        return attachment;
    }

    public void delete(File attDir, Attachment attachment) throws DAOException {
        try {
            delete(attachment);
            deleteFile(attDir, attachment);
        } catch (HibernateException e) {
            throw new DAOException("dbDelete failed!", e);
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
    @SuppressWarnings("unchecked, rawtypes")
    public ArrayList<Attachment> getByEntry(Entry entry) throws DAOException {
        ArrayList<Attachment> attachments = null;

        Session session = currentSession();
        try {
            String queryString = "from " + Attachment.class.getName()
                    + " as attachment where attachment.entry = :entry order by attachment.id desc";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            List list = query.list();
            if (list != null) {
                attachments = (ArrayList<Attachment>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve attachment by entry: " + entry.getId(), e);
        }

        return attachments;
    }

    public boolean hasAttachment(Entry entry) throws DAOException {
        Session session = currentSession();
        try {
            Number itemCount = (Number) session.createCriteria(Attachment.class)
                                               .setProjection(Projections.countDistinct("id"))
                                               .add(Restrictions.eq("entry", entry)).uniqueResult();
            return itemCount.longValue() > 0;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve attachment by entry: " + entry.getId(), e);
        }
    }

    /**
     * Retrieves attachment referenced by a unique file identifier
     *
     * @param fileId unique file identifier
     * @return retrieved attachment; null if none is found or there is a problem retrieving
     *         attachment
     * @throws DAOException on Hibernate exception
     */
    public Attachment getByFileId(String fileId) throws DAOException {
        Attachment attachment = null;
        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + Attachment.class.getName() + " where fileId = :fileId");
            query.setParameter("fileId", fileId);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                attachment = (Attachment) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve attachment by fileId: " + fileId, e);
        }

        return attachment;
    }

    /**
     * Retrieve the {@link File} from the disk of the given {@link Attachment}.
     *
     * @param attachment attachment whose physical file is to be retrieved
     * @return File physical attachment file
     * @throws DAOException
     */
    public File getFile(File attDir, Attachment attachment) throws DAOException {
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
