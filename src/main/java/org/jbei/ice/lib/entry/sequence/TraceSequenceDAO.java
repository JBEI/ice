package org.jbei.ice.lib.entry.sequence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

/**
 * TraceSequence to manipulate {@link TraceSequence} objects.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class TraceSequenceDAO extends HibernateRepository<TraceSequence> {
    private static String traceFilesDirectory = JbeirSettings.getSetting("TRACE_FILES_DIRECTORY");

    /**
     * Create a new {@link TraceSequence} object in the database, and write the file data to disk.
     * 
     * @param traceSequence
     * @param inputStream
     * @return Saved TraceSequence object.
     * @throws DAOException
     */
    public TraceSequence create(TraceSequence traceSequence, InputStream inputStream)
            throws DAOException {
        if (traceSequence == null) {
            throw new DAOException("Couldn't save TraceSequence. TraceSequence is null!");
        }

        TraceSequence result = null;
        try {
            if (getByFileId(traceSequence.getFileId()) != null) {
                throw new DAOException("TraceSequence by this fileId already exists!");
            }

            writeTraceSequenceToFile(traceSequence.getFileId(), inputStream);

            result = (TraceSequence) super.saveOrUpdate(traceSequence);
        } catch (DAOException e) {
            try {
                deleteTraceSequenceToFile(traceSequence);
            } catch (IOException e1) {
                throw new DAOException(e1);
            }

            throw new DAOException("Failed to create TraceSequence!", e);
        } catch (IOException e) {
            throw new DAOException("Failed to save trace file locally!", e);
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
    public TraceSequence save(TraceSequence traceSequence) throws DAOException {

        TraceSequenceAlignment traceSequenceAlignment = traceSequence.getTraceSequenceAlignment();
        Session session = newSession();
        try {
            session.getTransaction().begin();
            session.saveOrUpdate(traceSequenceAlignment);
            traceSequence.setTraceSequenceAlignment(traceSequenceAlignment);
            session.saveOrUpdate(traceSequence);
            session.getTransaction().commit();
            return traceSequence;
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Retrieve the {@link TraceSequence} object by its fileId.
     * 
     * @param fileId
     * @return TraceSequence object.
     * @throws ManagerException
     */
    public static TraceSequence getByFileId(String fileId) throws DAOException {
        TraceSequence traceSequence = null;

        Session session = newSession();
        try {
            Query query = session.createQuery("from " + TraceSequence.class.getName()
                    + " where fileId = :fileId");

            query.setParameter("fileId", fileId);

            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                traceSequence = (TraceSequence) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entry by fileId: " + fileId, e);
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
    public void delete(TraceSequence traceSequence) throws DAOException {
        if (traceSequence == null) {
            throw new DAOException("Failed to delete null Trace Sequence!");
        }

        try {
            super.delete(traceSequence);
            deleteTraceSequenceToFile(traceSequence);
        } catch (IOException e) {
            throw new DAOException("Failed to delete Trace Sequence file!", e);
        }
    }

    /**
     * Write the given file data to disk.
     * 
     * @param fileName Name of file written to disk.
     * @param inputStream File data.
     * @throws IOException
     * @throws ManagerException
     */
    private static void writeTraceSequenceToFile(String fileName, InputStream inputStream)
            throws IOException, DAOException {
        try {
            File file = new File(traceFilesDirectory + File.separator + fileName);

            File fileDir = new File(traceFilesDirectory);

            if (!fileDir.exists()) {
                if (!fileDir.mkdirs()) {
                    throw new DAOException("Could not create trace directory");
                }
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new DAOException("Could not create trace file " + file.getName());
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

    /**
     * Delete the file data on disk associated with the {@link TraceSequence} object given.
     * 
     * @param traceSequence
     * @throws IOException
     * @throws ManagerException
     */
    private void deleteTraceSequenceToFile(TraceSequence traceSequence) throws IOException,
            DAOException {
        try {
            File file = new File(traceFilesDirectory + File.separator + traceSequence.getFileId());

            file.delete();
        } catch (SecurityException e) {
            throw new DAOException(e);
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

        Session session = newSession();
        try {
            String queryString = "from TraceSequence as traceSequence where traceSequence.entry = :entry order by "
                    + "traceSequence.creationTime asc";
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
     * // TODO : move to a common file manager
     * Retrieve the {@link File} object from disk associated with the given {@link TraceSequence}
     * object.
     * 
     * @param traceSequence
     * @return Trace file.
     * @throws ManagerException
     */
    public static File getFile(TraceSequence traceSequence) throws DAOException {
        File file = new File(traceFilesDirectory + File.separator + traceSequence.getFileId());

        if (!file.canRead()) {
            throw new DAOException("Failed to open file for read!");
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
    public long getNumberOfTraceSequences(Entry entry) throws DAOException {
        int result = 0;

        Session session = newSession();

        try {
            String queryString = "from " + TraceSequence.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setParameter("entry", entry);
            List<TraceSequence> traceSequences = query.list();
            result = traceSequences.size();
        } catch (HibernateException e) {
            throw new DAOException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }
}
