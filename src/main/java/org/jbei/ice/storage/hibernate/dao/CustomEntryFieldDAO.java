package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.CustomEntryFieldModel;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class CustomEntryFieldDAO extends HibernateRepository<CustomEntryFieldModel> {

    @Override
    public CustomEntryFieldModel get(long id) {
        return super.get(CustomEntryFieldModel.class, id);
    }

    public List<CustomEntryFieldModel> getFieldsForType(EntryType type) {
        CriteriaQuery<CustomEntryFieldModel> query = getBuilder().createQuery(CustomEntryFieldModel.class);
        Root<CustomEntryFieldModel> from = query.from(CustomEntryFieldModel.class);
        query.where(getBuilder().equal(from.get("entryType"), type));
        return currentSession().createQuery(query).list();
    }

    /**
     * Searches for any <code>CustomEntryFieldModel</code> whose type and label matches
     * those specified in the parameters
     *
     * @param type  entry type
     * @param label field label
     * @return containing that contains result of null if no matches
     */
    public Optional<CustomEntryFieldModel> getLabelForType(EntryType type, String label) {
        CriteriaQuery<CustomEntryFieldModel> query = getBuilder().createQuery(CustomEntryFieldModel.class);
        Root<CustomEntryFieldModel> from = query.from(CustomEntryFieldModel.class);
        query.where(getBuilder().equal(from.get("entryType"), type), getBuilder().equal(from.get("label"), label));
        return currentSession().createQuery(query).uniqueResultOptional();
    }
}
