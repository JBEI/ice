package org.jbei.ice.lib.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

/**
 * Manager to manipulate {@link Attachment} objects in the database.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class AttachmentManager {
    private static String attachmentDirectory = JbeirSettings.getSetting("ATTACHMENTS_DIRECTORY");

    /**
     * Save the {@link Attachment} in the databse, and {@link InputStream} to the disk.
     * 
     * @param attachment
     * @param inputStream
     * @return Saved Attachment.
     * @throws ManagerException
     */
    public static Attachment save(Attachment attachment, InputStream inputStream)
            throws ManagerException {
        if (attachment == null) {
            throw new ManagerException("Failed to save null attachment!");
        }

        if (attachment.getFileId() == null || attachment.getFileId() == "") {
            String fileId = Utils.generateUUID();
            attachment.setFileId(fileId);
        }

        Attachment result = null;

        try {
            writeAttachmentToFile(attachment.getFileId(), inputStream);

            result = (Attachment) DAO.save(attachment);
        } catch (IOException e) {
            throw new ManagerException("Failed to create attachment file!", e);
        } catch (DAOException e) {
            try {
                deleteAttachmentFile(attachment);
            } catch (IOException e1) {
                throw new ManagerException(e);
            }

            throw new ManagerException("Failed to save attachment!", e);
        }

        return result;
    }

    /**
     * Delete the given {@link Attachment} from the database, and the file from the disk.
     * 
     * @param attachment
     * @throws ManagerException
     */
    public static void delete(Attachment attachment) throws ManagerException {
        if (attachment == null) {
            throw new ManagerException("Failed to delete null attachment!");
        }

        try {
            DAO.delete(attachment);

            deleteAttachmentFile(attachment);
        } catch (IOException e) {
            throw new ManagerException("Failed to delete attachment file!", e);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete attachment!", e);
        }
    }

    /**
     * Retrieve all {@link Attachment}s associated with the given {@link Entry}.
     * 
     * @param entry
     * @return ArrayList of Attachments.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Attachment> getByEntry(Entry entry) throws ManagerException {
        ArrayList<Attachment> attachments = null;

        Session session = DAO.newSession();
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
            throw new ManagerException("Failed to retrieve attachment by entry: " + entry.getId(),
                    e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return attachments;
    }

    //    public static boolean hasAttachment(Entry entry) throws ManagerException {
    //        Session session = DAO.newSession();
    //        try {
    //
    //        } catch (HibernateException e) {
    //            throw new ManagerException("Failed to retrieve attachment by entry: " + entry.getId(),
    //                    e);
    //        } finally {
    //            if (session.isOpen()) {
    //                session.close();
    //            }
    //        }
    //    }

    /**
     * Retrieves attachment referenced by a unique file identifier
     * 
     * @param fileId
     *            unique file identifier
     * @return retrieved attachment; null if none is found or there is a problem retrieving
     *         attachment
     * @throws ManagerException
     *             on Hibernate exception
     */
    public static Attachment getByFileId(String fileId) throws ManagerException {
        Attachment attachment = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Attachment.class.getName()
                    + " where fileId = :fileId");

            query.setParameter("fileId", fileId);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                attachment = (Attachment) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve attachment by fileId: " + fileId, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return attachment;
    }

    /**
     * Retrieve the {@link File} from the disk of the given {@link Attachment}.
     * 
     * @param attachment
     * @return File
     * @throws ManagerException
     */
    public static File getFile(Attachment attachment) throws ManagerException {
        File file = new File(attachmentDirectory + File.separator + attachment.getFileId());

        if (!file.canRead()) {
            throw new ManagerException("Failed to open file for read!");
        }

        return file;
    }

    /**
     * Write the given {@link InputStream} to the file with the given fileName.
     * 
     * @param fileName
     * @param inputStream
     * @throws IOException
     * @throws ManagerException
     */
    private static void writeAttachmentToFile(String fileName, InputStream inputStream)
            throws IOException, ManagerException {
        try {
            File file = new File(attachmentDirectory + File.separator + fileName);

            File fileDir = new File(attachmentDirectory);

            if (!fileDir.exists()) {
                if (!fileDir.mkdirs()) {
                    throw new ManagerException("Could not create attachment directory");
                }
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new ManagerException("Could not create attachment file " + file.getName());
                }
            }

            FileOutputStream outputStream = new FileOutputStream(file);

            try {
                IOUtils.copy(inputStream, outputStream);
            } finally {
                outputStream.close();
            }
        } catch (SecurityException e) {
            throw new ManagerException(e);
        }
    }

    /**
     * Delete the file on disk associated with the {@link Attachment}.
     * 
     * @param attachment
     * @throws IOException
     * @throws ManagerException
     */
    private static void deleteAttachmentFile(Attachment attachment) throws IOException,
            ManagerException {
        try {
            File file = new File(attachmentDirectory + File.separator + attachment.getFileId());

            file.delete();
        } catch (SecurityException e) {
            throw new ManagerException(e);
        }
    }

}
