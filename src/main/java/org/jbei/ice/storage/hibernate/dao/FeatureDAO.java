package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hibernate data accessor object for {@link Feature}s
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
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

    public List<Feature> getFeatures(int offset, int size) {
        return currentSession().createCriteria(Feature.class)
                .add(Restrictions.neOrIsNotNull("name", ""))
                .setFirstResult(offset)
                .setMaxResults(size)
                .list();
    }

    public long getFeaturesGroupByCount() {
        Number number = (Number) currentSession().createCriteria(Feature.class)
                .add(Restrictions.neOrIsNotNull("name", ""))
                .setProjection(Projections.countDistinct("name"))
                .uniqueResult();
        return number.longValue();
    }

    public Map<String, List<Feature>> getFeaturesGroupBy(int offset, int size) {
        // get unique names
        ScrollableResults scrollableResults = currentSession().createCriteria(Feature.class)
                .add(Restrictions.neOrIsNotNull("name", ""))
                .setProjection(Projections.distinct(Projections.property("name")))
                .setFirstResult(offset)
                .setMaxResults(size)
                .addOrder(Order.asc("name"))
                .scroll();

        Map<String, List<Feature>> results = new HashMap<>();

        // get all features that match
        while (scrollableResults.next()) {
            Object[] result = scrollableResults.get();
            String name = (String) result[0];
            List list = currentSession().createCriteria(Feature.class)
                    .add(Restrictions.eq("name", name).ignoreCase())
                    .list();
            results.put(name, list);
        }
        return results;
    }
}
