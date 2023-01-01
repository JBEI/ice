package org.jbei.ice.storage.hibernate.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.jbei.ice.dto.entry.CustomField;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.ParameterModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Data accessor object for handling {@link ParameterModel}s
 *
 * @author Hector Plahar
 */
public class ParameterDAO extends HibernateRepository<ParameterModel> {

    @Override
    public ParameterModel get(long id) {
        return super.get(ParameterModel.class, id);
    }

    // filter by key value pairs
    // todo : this needs to be looked at
    public List<Entry> filter(List<CustomField> fields) {
        List<Entry> entries = new ArrayList<>();
        boolean flag = false;

        for (CustomField field : fields) {
            if (flag && entries.isEmpty())
                return entries;

            CriteriaQuery<Entry> query = getBuilder().createQuery(Entry.class);
            Root<ParameterModel> from = query.from(ParameterModel.class);
            query.select(from.get("entry")).where(
                    getBuilder().equal(getBuilder().lower(from.get("key")), field.getName().toLowerCase()),
                    getBuilder().equal(getBuilder().lower(from.get("value")), field.getValue().toLowerCase())
            );

            if (flag)
                query.getRestriction().getExpressions().add(from.get("entry").in(entries));
            else
                flag = true;
            entries = currentSession().createQuery(query).list();
        }

        return entries;
    }
}
