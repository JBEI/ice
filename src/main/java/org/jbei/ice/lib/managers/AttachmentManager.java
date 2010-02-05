package org.jbei.ice.lib.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Base64String;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class AttachmentManager extends Manager {
    public static String attachmentDirectory = JbeirSettings.getSetting("ATTACHMENTS_DIRECTORY")
            + "/";

    public static Attachment create(Attachment attachment) throws ManagerException {
        attachment.setFileId(Utils.generateUUID());
        Attachment result = null;
        try {
            writeFileData(attachment);

            result = (Attachment) dbSave(attachment);

        } catch (IOException e) {

            throw new ManagerException("Could not write file: " + e.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
            try {
                deleteFile(attachment);
            } catch (IOException e1) {
                throw new ManagerException("Could not delete file: " + e1.toString());
            }
            throw new ManagerException("Could not create Attachment in db");
        }

        return result;
    }

    public static void delete(Attachment attachment) throws ManagerException {
        try {

            deleteFile(attachment);
            try {
                dbDelete(attachment);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new ManagerException("Could not delete attachment in db: " + e.toString());
            }

        } catch (IOException e) {
            String msg = "Could not delete file: " + attachment.getFileName();
            Logger.error(msg);
        } catch (HibernateException e) {
            String msg = "Could not remove entry from database." + attachment.getFileName();
            Logger.error(msg);
        }
    }

    public static Attachment get(int id) throws ManagerException {
        Attachment attachment = null;
        try {
            attachment = (Attachment) dbGet(Attachment.class, id);
            attachment = readFileData(attachment);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerException("Error reading file: " + e.toString());
        } catch (Exception e) {
            throw new ManagerException("Failed loading attachment from db: " + e.toString());
        }
        return attachment;
    }

    public static Attachment getByFileId(String fileId) throws ManagerException {
        Query query = session.createQuery("from " + Attachment.class.getName()
                + " where file_id = :fileId");
        query.setString("fileId", fileId);
        Attachment attachment = null;
        try {
            attachment = (Attachment) query.uniqueResult();
        } catch (Exception e) {
            throw new ManagerException("Could not retrieve Attachment by FileId");
        }
        if (attachment == null) {
            throw new ManagerException("No such fileId found");
        } else {

            try {
                attachment = readFileData(attachment);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ManagerException("Could not read file: " + e.toString());
            }
        }
        return attachment;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Attachment> getByEntry(Entry entry) throws ManagerException {
        ArrayList<Attachment> attachments;
        Query query = session.createQuery("from " + Attachment.class.getName()
                + " where entries_id = :entryId");
        query.setInteger("entryId", entry.getId());
        attachments = (ArrayList<Attachment>) query.list();
        for (Attachment at : attachments) {
            try {
                readFileData(at);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ManagerException("Error reading file " + at.getFileId());
            }
        }

        return attachments;
    }

    @SuppressWarnings("unchecked")
    public static boolean hasAttachment(Entry entry) {
        boolean result = false;
        try {
            String queryString = "from " + Attachment.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List attachments = query.list();
            if (attachments.size() > 0) {
                result = true;
            }
        } catch (Exception e) {
            String msg = "Could not determine if entry has attachments: " + entry.getRecordId();
            Logger.error(msg);

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static int getNumberOfAttachments(Entry entry) {
        int result = 0;
        try {
            String queryString = "from " + Attachment.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List attachments = query.list();
            result = attachments.size();
        } catch (Exception e) {
            String msg = "Could not determine if entry has attachments: " + entry.getRecordId();
            Logger.error(msg);
        }
        return result;
    }

    public static void main(String[] args) throws IOException, ManagerException {

    }

    public static File readFile(Attachment attachment) throws IOException {
        File file = new File(attachmentDirectory + attachment.getFileId());
        if (file.canRead()) {

        } else {
            throw new IOException("could not read file: " + attachment.getFileId());
        }
        return file;
    }

    protected static Attachment readFileData(Attachment attachment) throws IOException {

        File file = new File(attachmentDirectory + attachment.getFileId());
        InputStream inputStream = new FileInputStream(file);
        long fileLength = file.length();
        if (fileLength > 524288000) { // 500 MegaBytes
            throw new IOException("File size limit reached (500MB)");
        } else {
            byte[] bytes = new byte[(int) fileLength];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                throw new IOException("Could not read all of file " + file.getName());
            }
            inputStream.close();
            Base64String b64 = new Base64String();
            b64.putBytes(bytes);
            attachment.setData(b64);

            return attachment;

        }
    }

    protected static Attachment writeFileData(Attachment attachment) throws IOException {
        File file = new File(attachmentDirectory + attachment.getFileId());
        File fileDir = file.getParentFile();
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] bytes = attachment.getData().getBytes();

        if (bytes.length > 524288000) {
            throw new IOException("File size limit reached (500MB)");
        } else {
            outputStream.write(bytes);
        }

        outputStream.close();

        return attachment;

    }

    protected static void deleteFile(Attachment attachment) throws IOException {
        File file = new File(attachmentDirectory + attachment.getFileId());
        file.delete();
    }
}
