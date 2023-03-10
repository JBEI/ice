package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.EntryFieldValueModel;

/**
 * DAO for managing entry field value models
 *
 * @author Hector Plahar
 */
public class EntryFieldValueModelDAO extends HibernateRepository<EntryFieldValueModel> {
    @Override
    public EntryFieldValueModel get(long id) {
        return super.get(EntryFieldValueModel.class, id);
    }
}
