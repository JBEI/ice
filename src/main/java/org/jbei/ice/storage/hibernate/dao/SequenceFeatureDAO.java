package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.SequenceFeature;

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

    @SuppressWarnings("unchecked")
    public List<SequenceFeature> getAll() {
        return currentSession().createCriteria(SequenceFeature.class)
                .list();
    }
}
