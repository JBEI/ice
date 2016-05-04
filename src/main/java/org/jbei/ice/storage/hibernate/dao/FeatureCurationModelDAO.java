package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.FeatureCurationModel;

/**
 * @author Hector Plahar
 */
public class FeatureCurationModelDAO extends HibernateRepository<FeatureCurationModel> {

    @Override
    public FeatureCurationModel get(long id) {
        return super.get(FeatureCurationModel.class, id);
    }
}
