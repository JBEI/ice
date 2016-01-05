package org.jbei.ice.storage.hibernate.dao;

import org.apache.commons.io.IOUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.TraceSequence;
import org.jbei.ice.storage.model.TraceSequenceAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
    public TraceSequence create(File tracesFile, TraceSequence traceSequence, InputStream inputStream)
            throws DAOException {
        TraceSequence result;
        try {
            if (getByFileId(traceSequence.getFileId()) != null) {
                throw new DAOException("TraceSequence with fileId " + traceSequence.getFileId() + " already exists!");
            }

            writeTraceSequenceToFile(tracesFile, traceSequence.getFileId(), inputStream);
            result = super.create(traceSequence);
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

        return result;
    }

    /**
     * Save the given {@link TraceSequence} object in the database.
     *
     * @param traceSequence
     * @return Saved TraceSequence object.
     * @throws DAOException
     */
    public TraceSequence save(TraceSequence traceSequence) throws DAOException {
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
     * @param fileId
     * @return TraceSequence object.
     * @throws DAOException
     */
    public TraceSequence getByFileId(String fileId) throws DAOException {
        TraceSequence traceSequence = null;

        Session session = currentSession();
        try {
            Query query = session.createQuery("from " + TraceSequence.class.getName() + " where fileId = :fileId");
            query.setParameter("fileId", fileId);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                traceSequence = (TraceSequence) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to retrieve entry by fileId: " + fileId, e);
        }

        return traceSequence;
    }

    /**
     * Delete the given {@link TraceSequence} object in the database, and remove the file data from
     * disk.
     *
     * @param traceSequence
     * @throws DAOException
     */
    public void delete(File tracesFile, TraceSequence traceSequence) throws DAOException {
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
    private void deleteTraceSequenceToFile(File traceFilesDirectory, TraceSequence traceSequence) throws IOException,
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
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<TraceSequence> getByEntry(Entry entry, int start, int limit) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(TraceSequence.class.getName())
                    .add(Restrictions.eq("entry", entry));
            criteria.addOrder(Order.asc("creationTime"));
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
            return criteria.list();
        } catch (HibernateException e) {
            throw new DAOException("Failed to get trace sequence by entry!", e);
        }
    }

    public int getCountByEntry(Entry entry) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(TraceSequence.class.getName())
                    .add(Restrictions.eq("entry", entry))
                    .setProjection(Projections.countDistinct("id"));
            Number number = (Number) criteria.uniqueResult();
            if (number == null)
                return 0;
            return number.intValue();
        } catch (HibernateException e) {
            throw new DAOException("Failed to get trace sequence by entry!", e);
        }
    }

    public int getTraceSequenceCount(Entry entry) {
        Number itemCount = (Number) currentSession().createCriteria(TraceSequence.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("entry", entry)).uniqueResult();
        return itemCount.intValue();
    }


    @Override
    public TraceSequence get(long id) {
        return super.get(TraceSequence.class, id);
    }
}
