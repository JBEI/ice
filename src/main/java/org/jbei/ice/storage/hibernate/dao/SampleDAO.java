package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sample;
import org.jbei.ice.storage.model.Storage;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class SampleDAO extends HibernateRepository<Sample> {

    public Sample get(long id) {
        return super.get(Sample.class, id);
    }

    public boolean hasSample(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sample> from = query.from(Sample.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).setMaxResults(1).uniqueResult() > 0;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public int getSampleCount(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Sample> from = query.from(Sample.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public List<Sample> getSamplesByEntry(Entry entry) {
        try {
            CriteriaQuery<Sample> query = getBuilder().createQuery(Sample.class);
            Root<Sample> from = query.from(Sample.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve {@link Sample} objects associated with the given {@link Storage} object.
     *
     * @param storage
     * @return ArrayList of Sample objects.
     * @throws DAOException
     */
    public List<Sample> getSamplesByStorage(Storage storage) {
        try {
            CriteriaQuery<Sample> query = getBuilder().createQuery(Sample.class);
            Root<Sample> from = query.from(Sample.class);
            query.where(getBuilder().equal(from.get("storage"), storage));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to retrieve sample by storage id: " + storage.getId(), e);
        }
    }
}
