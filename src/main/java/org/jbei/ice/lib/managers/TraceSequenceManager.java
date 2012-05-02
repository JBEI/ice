package org.jbei.ice.lib.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;
import org.jbei.ice.lib.utils.JbeirSettings;

/**
 * Manager to manipulate {@link TraceSequence} objects.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public class TraceSequenceManager {
    private static String traceFilesDirectory = JbeirSettings.getSetting("TRACE_FILES_DIRECTORY");

    /**
     * Create a new {@link TraceSequence} object in the database, and write the file data to disk.
     * 
     * @param traceSequence
     * @param inputStream
     * @return Saved TraceSequence object.
     * @throws ManagerException
     */
    public static TraceSequence create(TraceSequence traceSequence, InputStream inputStream)
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

            throw new ManagerException("Failed to create TraceSequence!", e);
        } catch (IOException e) {
            throw new ManagerException("Failed to save trace file locally!", e);
        }

        return result;
    }

    /**
     * Save the given {@link TraceSequence} object in the database.
     * 
     * @param traceSequence
     * @return Saved TraceSequence object.
     * @throws ManagerException
     */
    public static TraceSequence save(TraceSequence traceSequence) throws ManagerException {
        TraceSequence result = null;

        try {
            TraceSequenceAlignment traceSequenceAlignment = (TraceSequenceAlignment) DAO
                    .save(traceSequence.getTraceSequenceAlignment());

            traceSequence.setTraceSequenceAlignment(traceSequenceAlignment);

            result = (TraceSequence) DAO.save(traceSequence);
        } catch (DAOException e) {
            throw new ManagerException("Failed to save TraceSequence!", e);
        }

        return result;
    }

    /**
     * Retrieve the {@link TraceSequence} object by its fileId.
     * 
     * @param fileId
     * @return TraceSequence object.
     * @throws ManagerException
     */
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

    /**
     * Delete the given {@link TraceSequence} object in the database, and remove the file data from
     * disk.
     * 
     * @param traceSequence
     * @throws ManagerException
     */
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

    /**
     * Write the given file data to disk.
     * 
     * @param fileName
     *            Name of file written to disk.
     * @param inputStream
     *            File data.
     * @throws IOException
     * @throws ManagerException
     */
    private static void writeTraceSequenceToFile(String fileName, InputStream inputStream)
            throws IOException, ManagerException {
        try {
            File file = new File(traceFilesDirectory + File.separator + fileName);

            File fileDir = new File(traceFilesDirectory);

            if (!fileDir.exists()) {
                if (!fileDir.mkdirs()) {
                    throw new ManagerException("Could not create trace directory");
                }
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new ManagerException("Could not create trace file " + file.getName());
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
     * Delete the file data on disk associated with the {@link TraceSequence} object given.
     * 
     * @param traceSequence
     * @throws IOException
     * @throws ManagerException
     */
    private static void deleteTraceSequenceToFile(TraceSequence traceSequence) throws IOException,
            ManagerException {
        try {
            File file = new File(traceFilesDirectory + File.separator + traceSequence.getFileId());

            file.delete();
        } catch (SecurityException e) {
            throw new ManagerException(e);
        }
    }

    /**
     * Retrieve all {@link TraceSequence} objects associated with the given {@link Entry} object.
     * 
     * @param entry
     * @return List of TraceSequence objects.
     * @throws ManagerException
     */
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

    /**
     * Retrieve the {@link File} object from disk associated with the given {@link TraceSequence}
     * object.
     * 
     * @param traceSequence
     * @return Trace file.
     * @throws ManagerException
     */
    public static File getFile(TraceSequence traceSequence) throws ManagerException {
        File file = new File(traceFilesDirectory + File.separator + traceSequence.getFileId());

        if (!file.canRead()) {
            throw new ManagerException("Failed to open file for read!");
        }

        return file;
    }

    /**
     * Retrieve the number of {@link TraceSequence} object associated with the given {@link Entry}
     * object.
     * 
     * @param entry
     * @return Number of TraceSequence objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static long getNumberOfTraceSequences(Entry entry) throws ManagerException {
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
}
