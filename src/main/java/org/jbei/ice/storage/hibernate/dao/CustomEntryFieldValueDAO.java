package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.CustomEntryFieldValueModel;
import org.jbei.ice.storage.model.Entry;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class CustomEntryFieldValueDAO extends HibernateRepository<CustomEntryFieldValueModel> {

    @Override
    public CustomEntryFieldValueModel get(long id) {
        return super.get(CustomEntryFieldValueModel.class, id);
    }

    public List<CustomEntryFieldValueModel> getByEntry(Entry entry) {
        try {
            CriteriaQuery<CustomEntryFieldValueModel> query = getBuilder().createQuery(CustomEntryFieldValueModel.class);
            Root<CustomEntryFieldValueModel> from = query.from(CustomEntryFieldValueModel.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).list();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }
}
