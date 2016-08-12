package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Feature;
import org.jbei.ice.storage.model.SequenceFeature;

import java.util.List;

/**
 * Hibernate Data accessor object for {@link SequenceFeature}s
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class SequenceFeatureDAO extends HibernateRepository<SequenceFeature> {
    @Override
    public SequenceFeature get(long id) {
        return get(SequenceFeature.class, id);
    }

    public List<Long> getEntryIdsByFeature(Feature feature) {
        return currentSession().createCriteria(SequenceFeature.class)
                .createAlias("sequence", "sequence")
                .add(Restrictions.eq("feature", feature))
                .setProjection(Projections.property("sequence.entry.id"))
                .list();
    }

    public List<SequenceFeature> getByFeature(Feature feature) {
        return currentSession().createCriteria(SequenceFeature.class)
                .add(Restrictions.eq("feature", feature))
                .list();
    }

    public int getFeatureCount(Entry entry) {
        Number number = (Number) currentSession().createCriteria(SequenceFeature.class)
                .createAlias("sequence", "sequence")
                .add(Restrictions.eq("sequence.entry", entry))
                .setProjection(Projections.countDistinct("id"))
                .uniqueResult();
        if (number != null)
            return number.intValue();
        return -1;
    }

    public List<SequenceFeature> getEntrySequenceFeatures(Entry entry) {
        return currentSession().createCriteria(SequenceFeature.class)
                .createAlias("sequence", "sequence")
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("sequence.entry", entry))
                .list();
    }
}
