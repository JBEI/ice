package org.jbei.ice.storage.hibernate.dao;

import com.google.common.io.ByteStreams;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Data accessor for {@link TraceSequence}s
 *
 * @author Hector Plahar
 */
public class TraceSequenceDAO extends HibernateRepository<TraceSequence> {

    /**
     * Create a new {@link TraceSequence} object in the database, and write the file data to disk.
     *
     * @param traceDir      storage directory for the new trace sequence file
     * @param traceSequence information about the new file to store, including a unique identifier
     * @param inputStream   byte stream for file
     * @return Saved TraceSequence object.
     * @throws DAOException if the filename for the new trace sequence already exists or if there is an error saving
     */
    public TraceSequence create(Path traceDir, TraceSequence traceSequence, InputStream inputStream) {
        try {
            if (getByFileId(traceSequence.getFileId()).isPresent()) {
                throw new DAOException("TraceSequence with fileId " + traceSequence.getFileId() + " already exists!");
            }

            writeTraceSequenceToFile(traceDir, traceSequence.getFileId(), inputStream);
            return super.create(traceSequence);
        } catch (Exception e) {
            Logger.error(e);

            try {
                // attempt to clean up by deleting the saved file
                Path path = Paths.get(traceDir.toString(), traceSequence.getFileId());
                if (!Files.deleteIfExists(path))
                    Logger.error("Could not delete trace sequence: " + path);
            } catch (IOException e1) {
                throw new DAOException(e1);
            }

            throw new DAOException("Failed to create trace sequence", e);
        }
    }

    /**
     * Save the given {@link TraceSequence} object in the database.
     *
     * @param traceSequence object to save
     * @return Saved TraceSequence object.
     * @throws DAOException on hibernate exception saving
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
            throw new DAOException("Error saving trace sequence", e);
        }
    }

    /**
     * Retrieve the {@link TraceSequence} object by its fileId.
     *
     * @param fileId unique file identifier
     * @return {@link Optional} container that contains the trace sequence if found.
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
     * Delete the trace file from the referenced directory and if that succeeds, deletes the given
     * {@link TraceSequence} object in the database
     *
     * @param tracesDir     directory for trace files
     * @param traceSequence trace object to delete from database
     * @throws DAOException on hibernate or IO Exception when attempting to save the trace sequence object
     *                      or
     */
    public void delete(Path tracesDir, TraceSequence traceSequence) {
        try {
            Path tracePath = Paths.get(tracesDir.toString(), traceSequence.getFileId());
            if (Files.deleteIfExists(tracePath))
                super.delete(traceSequence);
            else
                Logger.error("Couldn't delete trace : " + tracePath.toString());
        } catch (HibernateException | IOException e) {
            throw new DAOException("Failed to delete Trace Sequence file", e);
        }
    }

    /**
     * Write the given file data to disk.
     *
     * @param fileName    Name of file written to disk.
     * @param inputStream File data.
     * @throws DAOException
     */
    private void writeTraceSequenceToFile(Path tracesDir, String fileName, InputStream inputStream) throws IOException {
        Files.createDirectories(tracesDir);
        Files.write(Paths.get(tracesDir.toString(), fileName), ByteStreams.toByteArray(inputStream));
    }

    /**
     * Retrieve all {@link TraceSequence} objects associated with the given {@link Entry} object
     * using the paging parameters, ordered by creation time in ascending order
     *
     * @param entry entry whose trace sequences are being retrieved
     * @param start start for traces retrieval
     * @param limit maximum number of trace records to retrieve
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

    /**
     * Get number of trace sequences available for specified entry
     *
     * @param entry entry whose entry count is being retrieved
     * @return number of traces available for the specified entry
     */
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
