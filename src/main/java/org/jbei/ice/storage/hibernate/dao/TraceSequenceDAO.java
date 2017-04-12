package org.jbei.ice.storage.hibernate.dao;

import org.apache.commons.io.IOUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.TraceSequence;
import org.jbei.ice.storage.model.TraceSequenceAlignment;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Data accessor for {@link TraceSequence}s
 *
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */
public class TraceSequenceDAO extends HibernateRepository<TraceSequence> {

    /**
     * Create a new {@link TraceSequence} object in the database, and write the file data to disk.
     *
     * @param traceSequence
     * @param inputStream
     * @return Saved TraceSequence object.
     * @throws DAOException
     */
    public TraceSequence create(File tracesFile, TraceSequence traceSequence, InputStream inputStream) {
        try {
            if (getByFileId(traceSequence.getFileId()).isPresent()) {
                throw new DAOException("TraceSequence with fileId " + traceSequence.getFileId() + " already exists!");
            }

            writeTraceSequenceToFile(tracesFile, traceSequence.getFileId(), inputStream);
            return super.create(traceSequence);
        } catch (DAOException e) {
            try {
                deleteTraceSequenceToFile(tracesFile, traceSequence);
            } catch (IOException e1) {
                throw new DAOException(e1);
            }

            throw new DAOException("Failed to create TraceSequence!", e);
        } catch (IOException e) {
            throw new DAOException("Failed to save trace file locally!", e);
        }
    }

    /**
     * Save the given {@link TraceSequence} object in the database.
     *
     * @param traceSequence
     * @return Saved TraceSequence object.
     * @throws DAOException
     */
    public TraceSequence save(TraceSequence traceSequence) {
        TraceSequenceAlignment traceSequenceAlignment = traceSequence.getTraceSequenceAlignment();
        Session session = currentSession();
        try {
            session.saveOrUpdate(traceSequenceAlignment);
            traceSequence.setTraceSequenceAlignment(traceSequenceAlignment);
            session.saveOrUpdate(traceSequence);
            return traceSequence;
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            throw new DAOException("Unknown database exception ", e1);
        }
    }

    /**
     * Retrieve the {@link TraceSequence} object by its fileId.
     *
     * @param fileId  unique file identifier
     * @return TraceSequence object.
     * @throws DAOException
     */
    public Optional<TraceSequence> getByFileId(String fileId) {
        try {
            CriteriaQuery<TraceSequence> query = getBuilder().createQuery(TraceSequence.class);
            Root<TraceSequence> from = query.from(TraceSequence.class);
            query.where(getBuilder().equal(from.get("fileId"), fileId));
            return currentSession().createQuery(query).uniqueResultOptional();
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entry by fileId: " + fileId, e);
        }
    }

    /**
     * Delete the given {@link TraceSequence} object in the database, and remove the file data from
     * disk.
     *
     * @param traceSequence
     * @throws DAOException
     */
    public void delete(File tracesFile, TraceSequence traceSequence) {
        if (traceSequence == null) {
            throw new DAOException("Failed to delete null Trace Sequence!");
        }

        try {
            super.delete(traceSequence);
            deleteTraceSequenceToFile(tracesFile, traceSequence);
        } catch (IOException e) {
            throw new DAOException("Failed to delete Trace Sequence file!", e);
        }
    }

    /**
     * Write the given file data to disk.
     *
     * @param fileName    Name of file written to disk.
     * @param inputStream File data.
     * @throws IOException
     * @throws DAOException
     */
    private void writeTraceSequenceToFile(File traceFilesDirectory, String fileName, InputStream inputStream)
            throws IOException, DAOException {
        try {
            File file = new File(traceFilesDirectory + File.separator + fileName);

            if (!traceFilesDirectory.exists()) {
                if (!traceFilesDirectory.mkdirs()) {
                    throw new DAOException("Could not create trace directory");
                }
            }

            if (!file.exists() && !file.createNewFile()) {
                throw new DAOException("Could not create trace file " + file.getName());
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                IOUtils.copy(inputStream, outputStream);
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
     * @throws DAOException
     */
    private void deleteTraceSequenceToFile(File traceFilesDirectory, TraceSequence traceSequence) throws IOException {
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
     * @throws DAOException
     */
    public List<TraceSequence> getByEntry(Entry entry, int start, int limit) {
        try {
            CriteriaQuery<TraceSequence> query = getBuilder().createQuery(TraceSequence.class);
            Root<TraceSequence> from = query.from(TraceSequence.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            query.orderBy(getBuilder().asc(from.get("creationTime")));
            return currentSession().createQuery(query).setMaxResults(limit).setFirstResult(start).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getCountByEntry(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<TraceSequence> from = query.from(TraceSequence.class);
            query.select(getBuilder().countDistinct(from.get("id"))).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Override
    public TraceSequence get(long id) {
        return super.get(TraceSequence.class, id);
    }
}
