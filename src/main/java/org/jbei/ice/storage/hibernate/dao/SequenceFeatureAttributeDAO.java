package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.SequenceFeatureAttribute;

public class SequenceFeatureAttributeDAO extends HibernateRepository<SequenceFeatureAttribute> {

    @Override
    public SequenceFeatureAttribute get(long id) {
        return super.get(SequenceFeatureAttribute.class, id);
    }
}
