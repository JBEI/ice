package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.SequenceHistoryModel;

/**
 * Hibernate Data Accessor Object for {@link SequenceHistoryModel}
 *
 * @author Hector Plahar
 */
public class SequenceHistoryDAO extends HibernateRepository<SequenceHistoryModel> {

    @Override
    public SequenceHistoryModel get(long id) {
        return get(SequenceHistoryModel.class, id);
    }
}
