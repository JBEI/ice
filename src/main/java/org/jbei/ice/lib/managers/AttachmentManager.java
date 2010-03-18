package org.jbei.ice.lib.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.io.Streams;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class AttachmentManager {
    private static String attachmentDirectory = JbeirSettings.getSetting("ATTACHMENTS_DIRECTORY");

    public static Attachment save(Attachment attachment, InputStream inputStream)
            throws ManagerException {
        if (attachment == null) {
            throw new ManagerException("Failed to save null attachment!");
        }

        String fileId = Utils.generateUUID();

        attachment.setFileId(fileId);

        Attachment result = null;

        try {
            writeAttachmentToFile(fileId, inputStream);

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

    public static void delete(Attachment attachment) throws ManagerException {
        if (attachment == null) {
            throw new ManagerException("Failed to delete null attachment!");
        }

        try {
            DAO.delete(attachment);

            deleteAttachmentFile(attachment);
        } catch (IOException e) {
            throw new ManagerException("Failed to delete attachment file!");
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete attachment!");
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Attachment> getByEntry(Entry entry) throws ManagerException {
        ArrayList<Attachment> attachments = null;

        Session session = DAO.getSession();
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
            throw new ManagerException("Failed to retrieve attachment by entry: " + entry.getId(),
                    e);
        }

        return attachments;
    }

    public static File getFile(Attachment attachment) throws ManagerException {
        File file = new File(attachmentDirectory + File.separator + attachment.getFileId());

        if (!file.canRead()) {
            throw new ManagerException("Failed to open file for read!");
        }

        return file;
    }

    private static void writeAttachmentToFile(String fileName, InputStream inputStream)
            throws IOException, ManagerException {
        try {
            File file = new File(attachmentDirectory + File.separator + fileName);

            File fileDir = new File(attachmentDirectory);

            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(file);

            try {
                Streams.copy(inputStream, outputStream, 4096);
            } finally {
                outputStream.close();
            }
        } catch (SecurityException e) {
            throw new ManagerException(e);
        }
    }

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
