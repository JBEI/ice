package org.jbei.ice.lib.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.wicket.util.io.Streams;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;
import org.jbei.ice.lib.utils.JbeirSettings;

public class TraceSequenceManager {
    private static String traceFilesDirectory = JbeirSettings.getSetting("TRACE_FILES_DIRECTORY");

    public static TraceSequence save(TraceSequence traceSequence, InputStream inputStream)
            throws ManagerException {
        if (traceSequence == null) {
            throw new ManagerException("Couldn't save TraceSequence. TraceSequence is null!");
        }

        TraceSequence result = null;
        try {
            if (getByFileId(traceSequence.getFileId()) != null) {
                throw new ManagerException("TraceSequence by this fileId already exists!");
            }

            writeTraceSequenceToFile(traceSequence.getFileId(), inputStream);

            result = (TraceSequence) DAO.save(traceSequence);
        } catch (DAOException e) {
            try {
                deleteTraceSequenceToFile(traceSequence);
            } catch (IOException e1) {
                throw new ManagerException(e1);
            }

            throw new ManagerException("Failed to save TraceSequence!", e);
        } catch (IOException e) {
            throw new ManagerException("Failed to save trace file locally!", e);
        }

        return result;
    }

    public static TraceSequence getByFileId(String fileId) throws ManagerException {
        TraceSequence traceSequence = null;

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + TraceSequence.class.getName()
                    + " where fileId = :fileId");

            query.setParameter("fileId", fileId);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                traceSequence = (TraceSequence) queryResult;
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to retrieve entry by fileId: " + fileId, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return traceSequence;
    }

    public static void delete(TraceSequence traceSequence) throws ManagerException {
        if (traceSequence == null) {
            throw new ManagerException("Failed to delete null Trace Sequence!");
        }

        try {
            DAO.delete(traceSequence);

            deleteTraceSequenceToFile(traceSequence);
        } catch (IOException e) {
            throw new ManagerException("Failed to delete Trace Sequence file!", e);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete Trace Sequence!", e);
        }
    }

    private static void writeTraceSequenceToFile(String fileName, InputStream inputStream)
            throws IOException, ManagerException {
        try {
            File file = new File(traceFilesDirectory + File.separator + fileName);

            File fileDir = new File(traceFilesDirectory);

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

    private static void deleteTraceSequenceToFile(TraceSequence traceSequence) throws IOException,
            ManagerException {
        try {
            File file = new File(traceFilesDirectory + File.separator + traceSequence.getFileId());

            file.delete();
        } catch (SecurityException e) {
            throw new ManagerException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<TraceSequence> getByEntry(Entry entry) throws ManagerException {
        if (entry == null) {
            throw new ManagerException("Failed to get TraceSequences for null entry!");
        }

        List<TraceSequence> result = null;

        Session session = DAO.newSession();
        try {
            String queryString = "from TraceSequence as traceSequence where traceSequence.entry = :entry order by traceSequence.creationTime asc";
            Query query = session.createQuery(queryString);

            query.setEntity("entry", entry);

            Object queryResult = query.list();

            if (queryResult != null) {
                result = query.list();
            }
        } catch (HibernateException e) {
            throw new ManagerException("Failed to get trace sequence by entry!", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static File getFile(TraceSequence traceSequence) throws ManagerException {
        File file = new File(traceFilesDirectory + File.separator + traceSequence.getFileId());

        if (!file.canRead()) {
            throw new ManagerException("Failed to open file for read!");
        }

        return file;
    }

    @SuppressWarnings("unchecked")
    public static int getNumberOfTraceSequences(Entry entry) throws ManagerException {
        int result = 0;

        Session session = DAO.newSession();

        try {
            String queryString = "from " + TraceSequence.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List<TraceSequence> traceSequences = query.list();
            result = traceSequences.size();
        } catch (HibernateException e) {
            throw new ManagerException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static TraceSequenceAlignment saveAlignment(TraceSequenceAlignment traceSequenceAlignment)
            throws ManagerException {
        if (traceSequenceAlignment == null) {
            throw new ManagerException(
                    "Couldn't save TraceSequenceAlignment. TraceSequenceAlignment is null!");
        }

        TraceSequenceAlignment result;
        try {
            result = (TraceSequenceAlignment) DAO.save(traceSequenceAlignment);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save TraceSequenceAlignment!", e);
        }

        return result;
    }

    public static void deleteAlignment(TraceSequenceAlignment traceSequenceAlignment)
            throws ManagerException {
        if (traceSequenceAlignment == null) {
            throw new ManagerException(
                    "Couldn't delete TraceSequenceAlignment. TraceSequenceAlignment is null!");
        }

        try {
            DAO.delete(traceSequenceAlignment);
        } catch (DAOException e) {
            throw new ManagerException("Failed to delete TraceSequenceAlignment!", e);
        }
    }

    public static TraceSequenceAlignment getAlignment(TraceSequence traceSequence)
            throws ManagerException {
        TraceSequenceAlignment traceSequenceAlignment = null;

        Session session = DAO.newSession();

        try {
            String queryString = "from " + TraceSequenceAlignment.class.getName()
                    + " where traceSequence = :traceSequence";
            Query query = session.createQuery(queryString);
            query.setParameter("traceSequence", traceSequence);

            traceSequenceAlignment = (TraceSequenceAlignment) query.uniqueResult();
        } catch (HibernateException e) {
            throw new ManagerException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return traceSequenceAlignment;
    }
}
