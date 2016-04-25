package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Feature;

import java.util.List;

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

    public long getFeatureCount() {
        Number number = (Number) currentSession().createCriteria(Feature.class)
                .add(Restrictions.neOrIsNotNull("name", ""))
                .setProjection(Projections.count("id"))
                .uniqueResult();
        return number.longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Feature> getFeatures(int offset, int size) {
        return currentSession().createCriteria(Feature.class)
                .add(Restrictions.neOrIsNotNull("name", ""))
                .setFirstResult(offset)
                .setMaxResults(size)
                .list();
    }
}
