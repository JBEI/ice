package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Feature;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Hibernate Data accessor object for {@link SequenceFeature}s
 *
 * @author Hector Plahar
 */
public class SequenceFeatureDAO extends HibernateRepository<SequenceFeature> {

    @Override
    public SequenceFeature get(long id) {
        return get(SequenceFeature.class, id);
    }

    public List<Long> getEntryIdsByFeature(Feature feature) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<SequenceFeature> from = query.from(SequenceFeature.class);
            Join<SequenceFeature, Sequence> sequence = from.join("sequence");
            Join<Sequence, Entry> entry = sequence.join("entry");
            query.select(entry.get("id")).where(getBuilder().equal(from.get("feature"), feature));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<SequenceFeature> getByFeature(Feature feature) {
        try {
            CriteriaQuery<SequenceFeature> query = getBuilder().createQuery(SequenceFeature.class);
            Root<SequenceFeature> from = query.from(SequenceFeature.class);
            query.where(getBuilder().equal(from.get("feature"), feature));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getFeatureCount(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<SequenceFeature> from = query.from(SequenceFeature.class);
            Join<SequenceFeature, Sequence> sequence = from.join("sequence");
            query.select(getBuilder().countDistinct(from.get("id")))
                    .where(getBuilder().equal(sequence.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<SequenceFeature> getEntrySequenceFeatures(Entry entry) {
        try {
            CriteriaQuery<SequenceFeature> query = getBuilder().createQuery(SequenceFeature.class);
            Root<SequenceFeature> from = query.from(SequenceFeature.class);
            Join<SequenceFeature, Sequence> sequence = from.join("sequence");
            query.where(getBuilder().equal(sequence.get("entry"), entry)).distinct(true);
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<SequenceFeature> getSequenceFeatures(String userId, String nameFilter, int start, int limit) {
        try {
            CriteriaQuery<SequenceFeature> query = getBuilder().createQuery(SequenceFeature.class);
            Root<SequenceFeature> from = query.from(SequenceFeature.class);
            Join<SequenceFeature, Sequence> sequence = from.join("sequence");
            Join<Sequence, Entry> entry = sequence.join("entry");

            query.where(getBuilder().equal(entry.get("ownerEmail"), userId),
                    getBuilder().like(getBuilder().lower(from.get("name")), "%" + nameFilter.toLowerCase() + "%"));
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getSequenceFeaturesCount(String userId, String nameFilter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<SequenceFeature> from = query.from(SequenceFeature.class);
            Join<SequenceFeature, Sequence> sequence = from.join("sequence");
            Join<Sequence, Entry> entry = sequence.join("entry");

            query.select(getBuilder().countDistinct(from.get("id"))).where(
                    getBuilder().equal(entry.get("ownerEmail"), userId),
                    getBuilder().like(getBuilder().lower(from.get("name")), "%" + nameFilter.toLowerCase() + "%"));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
