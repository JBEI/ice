package org.jbei.ice.lib.entry.attachment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * Manager to manipulate {@link Attachment} objects in the database.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class AttachmentDAO extends HibernateRepository<Attachment> {

    public Attachment save(File attDir, Attachment attachment, InputStream inputStream) throws DAOException {
        Session session = newSession();
        try {
            session.saveOrUpdate(attachment);
            if (inputStream != null)
                writeFile(attDir, attachment.getFileId(), inputStream);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }

        return attachment;
    }

    /**
     * Write the given {@link InputStream} to the file with the given fileName.
     *
     * @param fileName
     * @param inputStream
     * @throws java.io.IOException
     * @throws DAOException
     */
    private void writeFile(File attDir, String fileName, InputStream inputStream)
            throws IOException, DAOException {
        try {
            File file = new File(attDir + File.separator + fileName);

            if (!attDir.exists()) {
                if (!attDir.mkdirs()) {
                    throw new DAOException("Could not create attachment directory");
                }
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new DAOException("Could not create attachment file " + file.getName());
                }
            }

            FileOutputStream outputStream = new FileOutputStream(file);

            try {
                IOUtils.copy(inputStream, outputStream);
            } finally {
                outputStream.close();
            }
        } catch (SecurityException e) {
            throw new DAOException(e);
        }
    }

    public void delete(File attDir, Attachment attachment) throws DAOException {
        Session session = newSession();

        try {
            session.delete(attachment);
            deleteFile(attDir, attachment);
        } catch (HibernateException e) {
            throw new DAOException("dbDelete failed!", e);
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Unknown exception ", e);
        } finally {
            closeSession(session);
        }

    }

    /**
     * Retrieve all {@link Attachment}s associated with the given {@link Entry}.
     *
     * @param entry
     * @return ArrayList of Attachments.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Attachment> getByEntry(Entry entry) throws DAOException {
        ArrayList<Attachment> attachments = null;

        Session session = newSession();
        try {
            String queryString = "from " + Attachment.class.getName()
                    + " as attachment where attachment.entry = :entry order by attachment.id desc";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                attachments = (ArrayList<Attachment>) list;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve attachment by entry: " + entry.getId(), e);
        } finally {
            closeSession(session);
        }

        return attachments;
    }

    public boolean hasAttachment(Entry entry) throws DAOException {
        Session session = newSession();
        try {

            Number itemCount = (Number) session.createCriteria(Attachment.class)
                                               .setProjection(Projections.countDistinct("id"))
                                               .add(Restrictions.eq("entry", entry)).uniqueResult();

            return itemCount.longValue() > 0;
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve attachment by entry: " + entry.getId(),
                                   e);
        } finally {
            closeSession(session);
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

        Session session = newSession();
        try {
            Query query = session.createQuery("from " + Attachment.class.getName() + " where fileId = :fileId");
            query.setParameter("fileId", fileId);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                attachment = (Attachment) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve attachment by fileId: " + fileId, e);
        } finally {
            closeSession(session);
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
        if (!file.canRead()) {
            throw new DAOException("Failed to open file for read!");
        }

        return file;
    }

    public void deleteFile(File attDir, Attachment attachment) throws DAOException {
        File file = new File(attDir + File.separator + attachment.getFileId());
        try {
            file.delete();
        } catch (Exception ioe) {
            throw new DAOException(ioe);
        }
    }
}
