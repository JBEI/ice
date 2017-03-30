package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Feature;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hibernate data accessor object for {@link Feature}s
 *
 * @author Hector Plahar
 */
public class FeatureDAO extends HibernateRepository<Feature> {

    @Override
    public Feature get(long id) {
        return super.get(Feature.class, id);
    }

    public long getFeatureCount(String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Feature> from = query.from(Feature.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            if (filter != null && !filter.isEmpty())
                query.where(getBuilder().like(getBuilder().lower(from.get("name")), "%" + filter.toLowerCase() + "%"));
            else
                query.where(getBuilder().isNotNull(from.get("name")), getBuilder().notEqual(from.get("name"), ""));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Feature> getFeatures(int offset, int size, String filter) {
        try {
            CriteriaQuery<Feature> query = getBuilder().createQuery(Feature.class);
            Root<Feature> from = query.from(Feature.class);
            if (filter != null && !filter.isEmpty())
                query.where(getBuilder().like(getBuilder().lower(from.get("name")), "%" + filter.toLowerCase() + "%"));
            else
                query.where(getBuilder().isNotNull(from.get("name")), getBuilder().notEqual(from.get("name"), ""));
            return currentSession().createQuery(query).setFirstResult(offset).setMaxResults(size).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public long getFeaturesGroupByCount() {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Feature> from = query.from(Feature.class);
            query.select(getBuilder().countDistinct(from.get("name"))).where(getBuilder().isNotNull(from.get("name")),
                    getBuilder().notEqual(from.get("name"), ""));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Map<String, List<Feature>> getFeaturesGroupBy(int offset, int size) {
        Map<String, List<Feature>> results = new HashMap<>();

        try {
            CriteriaQuery<String> query = getBuilder().createQuery(String.class);
            Root<Feature> from = query.from(Feature.class);
            query.select(from.get("name")).distinct(true);
            query.where(getBuilder().isNotNull(from.get("name")), getBuilder().notEqual(from.get("name"), ""));
            query.orderBy(getBuilder().asc(from.get("name")));
            List<String> names = currentSession().createQuery(query).setFirstResult(offset).setMaxResults(size).list();

            for (String name : names) {
                CriteriaQuery<Feature> featureQuery = getBuilder().createQuery(Feature.class);
                Root<Feature> root = featureQuery.from(Feature.class);
                featureQuery.where(getBuilder().equal(getBuilder().lower(root.get("name")), name.toLowerCase()));
                List<Feature> result = currentSession().createQuery(featureQuery).list();
                results.put(name, result);
            }

            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
