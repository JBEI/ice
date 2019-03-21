package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.SequenceHistoryModel;

public class SequenceHistoryModelDAO extends HibernateRepository<SequenceHistoryModel> {

    @Override
    public SequenceHistoryModel get(long id) {
        return super.get(SequenceHistoryModel.class, id);
    }
}
