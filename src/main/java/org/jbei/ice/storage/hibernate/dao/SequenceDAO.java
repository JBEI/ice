package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.Optional;

/**
 * Manipulate {@link Sequence} and associated objects in the database.
 *
 * @author Hector Plahar
 */
public class SequenceDAO extends HibernateRepository<Sequence> {

    /**
     * Retrieve the {@link Sequence} object associated with the given {@link Entry} object.
     *
     * @param entry entry associated with sequence
     * @return Sequence object.
     */
    public Sequence getByEntry(Entry entry) {
        try {
            CriteriaQuery<Sequence> query = getBuilder().createQuery(Sequence.class);
            Root<Sequence> from = query.from(Sequence.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            Optional<Sequence> sequence = currentSession().createQuery(query).uniqueResultOptional();
            return sequence.orElse(null);
//            return sequence.map(SequenceUtil::normalizeAnnotationLocations).orElse(null);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve sequence by entry: " + entry.getId(), e);
        }
    }

    /**
     * Retrieves the sequence format of the specified entry, if there is one available.
     * Typically, the format is not available if there is no sequence associated with the entry
     *
     * @param entryId unique identifier for entry
     * @return container containing sequence format, if one is found, or null otherwise
     * @throws DAOException on hibernate exception
     */
    public Optional<SequenceFormat> getSequenceFormat(long entryId) {
        try {
            CriteriaQuery<SequenceFormat> query = getBuilder().createQuery(SequenceFormat.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(from.get("format")).where(getBuilder().equal(entry.get("id"), entryId));
            return currentSession().createQuery(query).uniqueResultOptional();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Optional<String> getSequenceString(Entry entry) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Sequence> from = query.from(Sequence.class);
            query.select(from.get("sequence")).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResultOptional();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public boolean hasSequence(long entryId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(getBuilder().countDistinct(from.get("id"))).where(
                    getBuilder().equal(entry.get("id"), entryId)).distinct(true);
            return currentSession().createQuery(query).setMaxResults(1).uniqueResult() > 0;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public String getSequenceFilename(Entry entry) {
        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Sequence> from = query.from(Sequence.class);
            query.select(from.get("fileName")).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).getSingleResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Determines if the user uploaded a sequence file and associated it with an entry
     *
     * @param entryId unique identifier for entry
     * @return true if there is a sequence file that was originally uploaded by user, false otherwise
     * @throws DAOException on HibernateException
     */
    public boolean hasOriginalSequence(long entryId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(getBuilder().countDistinct(from.get("id"))).where(
                    getBuilder().equal(entry.get("id"), entryId),
                    getBuilder().notEqual(from.get("sequenceUser"), ""),
                    getBuilder().isNotNull(from.get("sequenceUser")));
            return currentSession().createQuery(query).uniqueResult() > 0;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Enables retrieving sequences in the database without loading everything in memory
     * <p/>
     * Expected usage is
     * <code>
     * long count = getSequenceCount();
     * int offset = 0;
     * while( offset < count ) {
     * Sequence sequence = dao.getSequence(offset);
     * // do something with sequence
     * }
     * </code>
     *
     * @return Sequence at the specified offset
     * @throws DAOException on Hibernate Exception
     */
    public Sequence getSequence(int offset) {
        try {
            CriteriaQuery<Sequence> query = getBuilder().createQuery(Sequence.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.where(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            return currentSession().createQuery(query).setFirstResult(offset).setMaxResults(1).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * @return number of sequences available for all valid (visibility=9) entry object
     */
    public int getSequenceCount() {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sequence> from = query.from(Sequence.class);
            Join<Sequence, Entry> entry = from.join("entry");
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(entry.get("visibility"), Visibility.OK.getValue()));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Override
    public Sequence get(long id) {
        return super.get(Sequence.class, id);
    }
}
